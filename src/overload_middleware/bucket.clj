(ns overload-middleware.bucket
  (:require [overload-middleware.schemas :refer [TokenBucketParameters]]
            [overload-middleware.utils :refer [get-time]]
            [schema.core :as s]))

(s/defn replenish-underlying [bucket :- TokenBucketParameters]
  (let [now (get-time)
        difference-in-secs (/ (- now (:last-time bucket)) 1000)
        increase (* (:rate bucket) difference-in-secs)
        new-tokens (min (:max bucket) (+ (:tokens bucket) increase))]
    (-> bucket
        (assoc :tokens new-tokens)
        (assoc :last-time now))))

(s/defn replenish [bucket-ref :- clojure.lang.Ref]
  (dosync (alter bucket-ref replenish-underlying)))

(s/defn dec-token [bucket :- TokenBucketParameters]
  (assoc bucket :tokens (dec (:tokens bucket))))

(s/defn get-token [bucket-ref :- clojure.lang.Ref]
  (dosync
   (if (>= 0 (:tokens @bucket-ref))
     nil
     (commute bucket-ref dec-token))))

(s/defn get-token-with-replenishment [bucket-ref :- clojure.lang.Ref]
  (replenish bucket-ref)
  (get-token bucket-ref))
