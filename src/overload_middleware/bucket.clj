(ns overload-middleware.bucket)

(defn get-time [] (System/currentTimeMillis))

(defn replenish-underlying [bucket]
  (let [now (get-time)
        difference-in-secs (/ (- now (:last-time bucket)) 1000)
        increase (* (:rate bucket) difference-in-secs)
        new-tokens (min (:max bucket) (+ (:tokens bucket) increase))]
    (-> bucket
        (assoc :tokens new-tokens)
        (assoc :last-time now))))

(defn replenish [bucket-ref]
  (dosync (alter bucket-ref replenish-underlying)))

(defn- dec-token [bucket]
  (assoc bucket :tokens (dec (:tokens bucket))))

(defn get-token [bucket-ref]
  (dosync
   (if (>= 0 (:tokens @bucket-ref))
     nil
     (commute bucket-ref dec-token))))

(defn get-token-with-replenishment [bucket-ref]
  (replenish bucket-ref)
  (get-token bucket-ref))
