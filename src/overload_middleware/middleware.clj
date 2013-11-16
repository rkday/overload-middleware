(ns overload-middleware.middleware
  (:require [overload-middleware.bucket :refer :all]
            [overload-middleware.latency :refer :all]
            [overload-middleware.utils :refer :all]
            [overload-middleware.schemas :refer :all]))

(declare register-new-latency)

(defn wrap-overload [app
                     {:keys [target-latency
                             override-algorithm-parameters
                             override-bucket-parameters]}]
  {:pre [(number? target-latency) (pos? target-latency)]}
  (let [algorithm-parameters (merge default-latency-algorithm-parameters override-algorithm-parameters)
        bucket-parameters (assoc
                              (merge default-token-bucket-parameters override-bucket-parameters)
                            :last-time (get-time))
        bucket (ref bucket-parameters)
        latency-info (ref {:target-latency target-latency
                           :smoothed-estimate target-latency
                           :recent-latencies []
                           :recent-overloads false
                           :last-calculation (get-time)})]
    (fn [req]
      (if (get-token-with-replenishment bucket)
        (let [[latency {:keys [status] :as result}] (with-latency app req)]
          (if (= 503 status) (dosync (alter latency-info assoc :recent-overloads true)))
          (register-new-latency latency-info algorithm-parameters bucket latency)
          result)
        {:status 503 :body "Overloaded"}))))

(defn register-new-latency [latency-info algorithm-parameters bucket new-latency]
  (let [[do-update ninetieth-percentile] (add-latency-sample latency-info
                                                             algorithm-parameters
                                                             new-latency)]
    (if do-update
      (dosync (update-latency-estimate latency-info algorithm-parameters ninetieth-percentile)
          (let [target (:target-latency @latency-info)
                err (-> (:smoothed-estimate @latency-info)
                 (- target)
                 (/ target))]
            (alter bucket assoc :rate (calculate-new-rate (:rate @bucket) algorithm-parameters err (:recent-overloads @latency-info))))
          (alter latency-info assoc :recent-overloads false)))))
