(ns overload-middleware.schemas
  (:require [schema.core :as s]
            [overload-middleware.utils :refer [get-time]]))

(def LatencyAlgorithmParameters
  {:dec-factor s/Number
   ;; Multiplicative factor controlling how the rate is decreased.
   :dec-threshold s/Number
   ;; The required % difference between the average latency and the target latency for the bucket rate to be reduced.
   :inc-factor s/Number
   ;; Additive factor controlling how much the rate is increased.
   :inc-threshold s/Number
   ;; The required % difference between the average latency and the
   ;; target latency for the bucket rate to be reduced.
   :inc-weight s/Number
   ;; Weight affecting how much impact the % difference has on the change in the bucket rate.
   :min-rate s/Number ; Minimum bucket rate.
   :max-rate s/Number ; Maximum bucket rate.
   :nreq s/Number
   :timeout s/Number
   ;; The rate is changed after :nreq requests have been processed or :timeout ms have passed, whichever comes first.
   :alpha s/Number
   ;; The 90th-percentile latency is tracked as a smoothed average - this is the smoothing factor.
   })

(def default-latency-algorithm-parameters
  {:dec-factor 1.2,
   :dec-threshold 0.0,
   :inc-factor 2,
   :inc-threshold -0.005, ; -0.5%
   :inc-weight 0.1,
   :min-rate 5,
   :max-rate 5000,
   :nreq 100,
   :timeout 1000,
   :alpha 0.7})

(def TokenBucketParameters
  {:tokens s/Number
   ;; The number of tokens currently available in this bucket (each of which allows one request to be processed).
   :max s/Number ; The maximum number of tokens this bucket can hold.
   :rate s/Number
   ;; The rate at which this bucket is refilled (tokens per second). Varied by the middleware based on latency.
   :last-time s/Number
   ;; The last time this bucket was refilled (a timestamp in milliseconds)
   })


(def default-token-bucket-parameters
  {:tokens 100,
   :max 5000,
   :last-time -1 ; The wrapper sets it to the current timestamp when created
   :rate 10})
