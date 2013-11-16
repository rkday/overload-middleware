(ns overload-middleware.utils)

(defn with-latency [f & args]
  (let [start (System/currentTimeMillis)
        result (apply f args)
        end (System/currentTimeMillis)]
    [(- end start) result]))

(defn with-nano-latency [f]
  (let [start (System/nanoTime)
        result (f)
        end (System/nanoTime)]
    [(- end start) result]))

(defn abs [num]
  (if (neg? num) (- 0 num) num))

(defn get-time [] (System/currentTimeMillis))
