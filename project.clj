(defproject overload-middleware "0.1.0"
  :description "Clojure/Ring middleware handler to automatically apply overload control based on a target latency"
  :url "https://github.com/rkday/overload-middleware"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [midje "1.5.1"]
                 [prismatic/schema "0.1.6"]]
  :plugins [[lein-marginalia "0.7.1"]])
