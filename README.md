# overload-middleware

Clojure/Ring middleware that automatically rejects some requests with
"503 Service Not Available" in order to keep latency below a specific
target.

This implements the algorithm given in
["Adaptive Overload Control For Busy Internet Servers"](http://www.eecs.harvard.edu/~mdw/papers/control-usits03.pdf)
- requests are accepted or rejected based on a token bucket, and the
rate at which that bucket refills is regularly changed based on the
difference between the target latency and the current 90th-percentile
latency.

One enhancement which this library makes over that algorithm is that,
if the application reports a 503 error (for example, because a
database is reporting overload), the middleware recognises that and
treats it as high-latency, rather than low-latency (which would have
the effect of allowing more requests). This is based on a similar
approach taken by [the Crest HTTP server](https://github.com/Metaswitch/crest/).

## Usage


    (ns my-ring-app.core
        (:require [overload-middleware.middleware :refer wrap-overload]))

    (defn myapp [req] {:status 200 :body "Hello World" :headers {}})

    ;; Aim to have 90% of requests served in 100ms or less
    (def wrapped-app (wrap-overload app {:target-latency 100}))

## License

Copyright © 2013 Robert K. Day.

Distributed under the MIT license.
