(ns scicloj.tablemath.v1.protocols
  (:require [scicloj.kindly.v4.kind :as kind]
            [clojure.string :as str]))

(defprotocol Summarizable
  (summary [this]))

(extend-protocol Summarizable
  fastmath.ml.regression.LMData
  (summary [model]
    (kind/code
     (with-out-str
       (println model)))))

