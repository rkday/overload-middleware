(ns overload-middleware.latency-test
  (:require [midje.sweet :refer :all]
            [overload-middleware.latency :refer :all]))

(def algorithm-parameters {:dec-factor 2
                           :inc-factor 2
                           :dec-threshold 0
                           :inc-threshold -0.5
                           :inc-weight 0.1
                           :min-rate 3
                           :max-rate 30})

(facts "ABOUT calculate-new-rate"
       (facts "WHEN there are 0 overloads"
              (fact "it increases the rate when the error is below the inc-threshold parameter")
              (fact "it decreases the rate when the error is above the dec-threshold parameter")
              (fact "it keeps the rate the same when the error is between the two thresholds"))
       (fact "when there are more than 0 overloads, it always decreases the rate")
       (facts "WHEN it increases the rate, the increase depends on these factors:"
              (fact "the error")
              (fact "the inc-weight parameter")
              (fact "the inc-factor parameter"))
       (fact "the rate never increases beyond the max-rate parameter")
       (fact "the decrease in the rate depends on the dec-factor")
       (fact "the rate never decreases beyond the min-rate parameter")
       )

(facts "ABOUT add-latency-sample"
       (fact "it adds the given sample to the vector of latencies in the latency ref")
       (fact "it returns a two-element vector")
       (fact "the first element of the vector indicates whether it has sampled a new 90th-percentile latency")
       (facts "WHEN the number of requests in the nreq parameter have gone through"
              (fact "it samples a new 90th-percentile latency")
              (fact "it empties the vector of latencies"))
       (facts "WHEN the maximum time since the last sample has passed"
              (fact "it samples a new 90th-percentile latency")
              (fact "it empties the vector of latencies")))

(facts "ABOUT update-latency-estimate"
       (fact "it updates the ref passed as the first argument using the new data")
       (fact "it keeps a smoothed estimate rather than jut replacing the estimate")
       (fact "the smoothing-out of the estimate depends on the alpha parameter"))
