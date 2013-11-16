(ns overload-middleware.middleware-test
  (:require [overload-middleware.middleware :refer :all]
            [midje.sweet :refer :all]
            [schema.core :as s]))

(def small-bucket {:tokens 20})
(def overrides {:override-algorithm-parameters {:nreq 2 :min-rate 2} :override-bucket-parameters small-bucket})
(def high-latency-target (assoc overrides :target-latency 100))
(def low-latency-target (assoc overrides :target-latency 0.1))
(defn app [req]
  (Thread/sleep 30)
  {:status 200})
(defn app-reporting-overload [req]
  (Thread/sleep 30)
  (if (odd? req)
    {:status 503 :body "Application Overloaded"}
    {:status 200}))
(def requests-per-test 80)

(s/with-fn-validation
  (fact "the wrapper does not return overload errors when hitting its latency targets"
        (let [wrapped (wrap-overload app high-latency-target)
              results (map wrapped (range requests-per-test))
              number-overloads (count (filter #{503} (map :status results)))]
          number-overloads => zero?))

  (fact "the wrapper returns overload errors when not hitting its latency targets"
        (let [wrapped (wrap-overload app low-latency-target)
              results (map wrapped (range requests-per-test))
              number-overloads (count (filter #{503} (map :status results)))]
          number-overloads =not=> zero?))

  (fact "a system with high latency still allows a small minimum number of requests through"
        (let [wrapped (wrap-overload app low-latency-target)
              results (map wrapped (range requests-per-test))
              number-overloads (count (filter #{503} (map :status results)))]
          number-overloads =not=> zero?
          (do
            (Thread/sleep 1000)
            (wrapped 1)) => {:status 200}))

  (fact "even at low latencies, the wrapper starts rejecting requests if the application is reporting overload"
        (let [wrapped (wrap-overload app-reporting-overload high-latency-target)
              app-overloads (/ requests-per-test 2)
              results (map wrapped (range requests-per-test))
              number-overloads (count (filter #{503} (map :status results)))]
          number-overloads => #(> % app-overloads))))
