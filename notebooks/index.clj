(ns index
  (:require [tablecloth.api :as tc]
            [scicloj.tablemath.v1.design-matrix :as dm]
            [scicloj.tablemath.v1.api :as tm]))


(-> {:x (range 9)}
    tc/dataset
    (tc/map-columns :y [:x] (fn [x]
                              (+ -9
                                 (* 4 x)
                                 (- 2 (* x x))
                                 (+ (* x x))
                                 (rand))))
    (dm/create-design-matrix [:y]
                             [[:x '(identity x)]
                              [:x2 '(* x x)]
                              [:x3 '(* x x x)]])
    (tm/lm {})
    tm/summary)

