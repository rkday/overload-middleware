(ns overload-middleware.latency
  (:require [overload-middleware.utils :refer [abs]]
            [overload-middleware.schemas :refer [LatencyAlgorithmParameters]]
            [schema.core :as s]
            ))

(s/defn sensible-parameters? [{:keys [dec-factor dec-threshold
                                     inc-factor inc-threshold inc-weight
                                     min-rate max-rate
                                     nreq timeout
                                     alpha]} :- LatencyAlgorithmParameters]
  (and
   (< 1 dec-factor)
   (pos? inc-factor)
   (>= dec-threshold 0)
   (neg? inc-threshold)
   (<= inc-weight 0)
   (> max-rate min-rate)
   (pos? min-rate)
   (>= alpha 0)
   (= 0 (rem nreq 10))
   (pos? timeout)))

(s/defn calculate-new-rate [current-rate :- s/Number
                          {:keys [dec-factor dec-threshold
                                  inc-factor inc-threshold inc-weight
                                  min-rate max-rate] :as parameters} :- LatencyAlgorithmParameters
                          error :- s/Number
                          overloads :- java.lang.Boolean]
  {:pre [(sensible-parameters? parameters)]}
  (let [decreased-rate (-> current-rate
                           (/ dec-factor)
                           (max min-rate))
        increased-rate (-> error
                           (- inc-weight)
                           (abs)
                           (* inc-factor)
                           (+ current-rate)
                           (min max-rate))]
    (cond
     (or overloads (> error dec-threshold))
     decreased-rate
     (< error inc-threshold)
     increased-rate
     :else
     current-rate)))

(s/defn add-latency-sample [latency-ref :- clojure.lang.Ref
                            parameters :- LatencyAlgorithmParameters
                            sample :- s/Number]
  {:pre [(sensible-parameters? parameters)
         (>= sample 0)]
   :post [vector? #(= 2 (count %))]}
  (dosync
   (let [new-recent-latencies (conj (:recent-latencies @latency-ref) sample)
         nreq (count new-recent-latencies)
         nintieth-percentile-index (max 0 (dec (int (* 0.9 nreq))))
         result (nth (sort new-recent-latencies) nintieth-percentile-index)
         nreq-reached (= (:nreq parameters) nreq)
         timer-popped (< (- (System/currentTimeMillis) 1000) (:last-calculation @latency-ref))]
     (if (or nreq-reached timer-popped)
       (do
         (alter latency-ref assoc :recent-latencies [])
         [true result])
       (do
         (alter latency-ref assoc :recent-latencies new-recent-latencies)
         [false nil])))))

(s/defn update-latency-estimate [latency-ref :- clojure.lang.Ref
                               parameters :- LatencyAlgorithmParameters
                               new-data :- s/Number]
  {:pre [(sensible-parameters? parameters)
         (>= new-data 0)]}
  (dosync
   (let [{:keys [alpha]} parameters
         target (:target-latency @latency-ref)
         smoothed-estimate (-> (:smoothed-estimate @latency-ref)
                               (* alpha)
                               (+ (* (- 1 alpha) new-data)))
         ]
     (alter latency-ref assoc :smoothed-estimate smoothed-estimate))))

