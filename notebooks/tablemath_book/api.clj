;; # API Documentation

;; ## Setup

(ns tablemath-book.api
  (:require [scicloj.tablemath.v1.api :as tm]
            [tablemath-book.utils :as utils]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.dataset.modelling :as ds-mod]))

;; ## Reference

(utils/include-fnvar-as-section #'tm/with)

;; #### Examples

(tm/with {:x 3 :y 9}
         '(+ x y))

(tm/with (tc/dataset {:x (range 4)
                      :y 9})
         '(tcc/+ x y))


(utils/include-fnvar-as-section #'tm/columns-with)

;; #### Examples

;; Note the naming of the resulting columns,
;; and note they can sequentially depend on each other.

(tm/columns-with (tc/dataset {"w" [:A :B :C]
                              :x (range 3)
                              :y (reverse (range 3))})
                 ["w"
                  :x
                  '(tcc/+ x y)
                  [:z '(tcc/+ x y)]
                  [:z1000 '(tcc/* z 1000)]
                  '((juxt tcc/+ tcc/*) x y)
                  [:p '((juxt tcc/+ tcc/*) x y)]
                  '{:a (tcc/+ x y)
                    :b (tcc/* x y)}
                  [:p '{:a (tcc/+ x y)
                        :b (tcc/* x y)}]
                  '[(tcc/column (tcc/+ x y) {:name :c})
                    (tcc/column (tcc/* x y) {:name :d})]
                  [:p '[(tcc/column (tcc/+ x y) {:name :c})
                        (tcc/column (tcc/* x y) {:name :d})]]])


(utils/include-fnvar-as-section #'tm/design)

;; #### Examples

(tm/design (tc/dataset {"w" [:A :B :C]
                        :x (range 3)
                        :y (reverse (range 3))})
           [:y]
           ["w"
            :x
            '(tcc/+ x y)
            [:z '(tcc/+ x y)]
            [:z1000 '(tcc/* z 1000)]
            '((juxt tcc/+ tcc/*) x y)
            [:p '((juxt tcc/+ tcc/*) x y)]
            '{:a (tcc/+ x y)
              :b (tcc/* x y)}
            [:p '{:a (tcc/+ x y)
                  :b (tcc/* x y)}]
            '[(tcc/column (tcc/+ x y)
                          {:name :c})
              (tcc/column (tcc/* x y)
                          {:name :d})]
            [:p '[(tcc/column (tcc/+ x y)
                              {:name :c})
                  (tcc/column (tcc/* x y)
                              {:name :d})]]])

(utils/include-fnvar-as-section #'tm/polynomial)

;; #### Examples

(-> [1 2 3]
    (tcc/column {:name :x})
    (tm/polynomial 4))

(utils/include-fnvar-as-section #'tm/one-hot)

;; #### Examples

(tm/one-hot (tcc/column [:B :A :A :B :B :C]
                        {:name :x}))

(tm/one-hot (tcc/column [:B :A :A :B :B :C]
                        {:name :x})
            {:values [:A :B :C]})

(tm/one-hot (tcc/column [:B :A :A :B :B :C]
                        {:name :x})
            {:values [:A :B :C]
             :include-last true})

(utils/include-fnvar-as-section #'tm/lm)

;; #### Examples:

;; ##### Linear relationship

(-> {:x (range 9)}
    tc/dataset
    (tc/map-columns :y [:x] (fn [x]
                              (+ (* 2 x) -3)))
    (ds-mod/set-inference-target [:y])
    tm/lm
    tm/summary)

