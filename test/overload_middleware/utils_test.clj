(ns overload-middleware.utils-test
  (:require
   [midje.sweet :refer [fact]]
   [overload-middleware.utils :refer :all]))

(fact "abs leaves non-negative numbers unchanged"
      (abs 3) => 3
      (abs 108) => 108
      (abs 0) => 0)

(fact "abs makes negative numbers positive"
      (abs -3) => 3
      (abs -93) => 93)

(fact "with-latency returns a vector"
      (vector? (with-latency identity 6)) => true)

(fact "the first element of the vector is the latency in milliseconds"
      (-> (first (with-latency #(Thread/sleep %) 4))
          (>= 4)) => true
      (first (with-latency identity 6)) => 0)

(fact "the second element of the vector is the function's result"
      (second (with-latency #(Thread/sleep %) 4)) => nil
      (second (with-latency identity 6)) => 6
      (second (with-latency assoc {:a [1 2 3]} :b '(4 5 [7]))) => {:a [1 2 3] :b '(4 5 [7])})
