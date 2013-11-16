(ns overload-middleware.bucket-test
  (:require [midje.sweet :refer :all]
            [overload-middleware.bucket :refer :all]
            [overload-middleware.utils :refer [get-time]]))

(def a-constant-value 3)
(def fixed-time (System/currentTimeMillis))
(def default-bucket {:tokens 100
                     :rate 7
                     :max 150
                     :last-time fixed-time})

(def empty-bucket (assoc default-bucket :tokens 0))

(def non-refilling-bucket (assoc default-bucket :rate 0))

(def small-rapidly-refilling-bucket
  (assoc default-bucket :rate 10000000 :max 3))

(facts "ABOUT get-token"
       (fact "it removes a token from the bucket"
             (:tokens (get-token (ref default-bucket)))
             => (dec (:tokens default-bucket)))
       (fact "it returns nil if a bucket has run out of tokens"
             (get-token (ref empty-bucket)) => nil))


(facts "ABOUT replenish"
       (fact "it adds more tokens to a bucket at the specified rate"
             (replenish (ref default-bucket))
             => #(> (:tokens %) (:tokens default-bucket))
             (replenish (ref non-refilling-bucket))
             => #(= (:tokens %) (:tokens non-refilling-bucket)))

       (fact "it won't fill a bucket past the maximum"
             (replenish (ref small-rapidly-refilling-bucket))
             => #(= (:tokens %) (:max small-rapidly-refilling-bucket))))

(facts "ABOUT get-token-with-replenishment"
       (fact "it replenishes the bucket and tries to get a token"
             (get-token-with-replenishment (ref default-bucket))
             =not=> #(= (dec (:tokens default-bucket))
                        (:tokens %))
             (provided (get-time) => (+ 500 fixed-time))

             (get-token-with-replenishment (ref default-bucket))
             => #(= (dec (:tokens (replenish (ref default-bucket))))
                    (:tokens %))
             (provided (get-time) => (+ 500 fixed-time)))

       (fact "it replenishes the bucket before getting the token"
             (get-token-with-replenishment (ref empty-bucket)) =not=> nil
             (provided (get-time) => (+ 500 fixed-time))))
