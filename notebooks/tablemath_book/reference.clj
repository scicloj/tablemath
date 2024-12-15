;; # API reference

;; ## Setup

;; In this notebook, we will use
;; [Tablecloth](https://scicloj.github.io/tablecloth/)
;; and [Tableplot](https://scicloj.github.io/tableplot/)
;; for code examples, alongside Tablemath.

(ns tablemath-book.reference
  (:require [scicloj.tablemath.v1.api :as tm]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [scicloj.tableplot.v1.plotly :as plotly]
            [tablemath-book.utils :as utils]))

;; ## Reference

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
             :include-first true})

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

(tm/columns-with (tc/dataset {"v" [4 5 6]
                              :w [:A :B :C]
                              :x (range 3)
                              :y (reverse (range 3))})
                 [:v
                  :w
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

(tm/design (tc/dataset {"v" [4 5 6]
                        :w [:A :B :C]
                        :x (range 3)
                        :y (reverse (range 3))})
           [:y]
           [:v
            :w
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

(utils/include-fnvar-as-section #'tm/lm)

;; #### Examples

;; ##### Linear relationship

(def linear-toydata
  (-> {:x (range 9)}
      tc/dataset
      (tc/map-columns :y
                      [:x]
                      (fn [x]
                        (+ (* 2 x)
                           -3
                           (* 3 (rand)))))))

(-> linear-toydata
    plotly/layer-point)

;; Note how the coefficients fit the way we generated the data:

(-> linear-toydata
    (tm/design [:y]
               [:x])
    tm/lm
    tm/summary)

;; ##### Cubic relationship

(def cubic-toydata
  (-> {:x (range 9)}
      tc/dataset
      (tc/map-columns :y
                      [:x]
                      (fn [x]
                        (+ 50
                           (* 4 x)
                           (* -9 x x)
                           (* x x x)
                           (* 3 (rand)))))))

(-> cubic-toydata
    plotly/layer-point)

;; Note how the coefficients fit the way we generated the data:

(-> cubic-toydata
    (tm/design [:y]
               ['(tm/polynomial x 3)])
    tm/lm
    tm/summary)

;; ##### Categorical relationship

(def days-of-week
  [:Mon :Tue :Wed :Thu :Fri :Sat :Sun])

(def categorical-toydata
  (-> {:t (range 18)
       :day-of-week (->> days-of-week
                         (repeat 3)
                         (apply concat))}
      tc/dataset
      (tc/map-columns :traffic
                      [:day-of-week]
                      (fn [dow]
                        (+ (case dow
                             :Sat 50
                             :Sun 50
                             60)
                           (* 5 (rand)))))))

(-> categorical-toydata
    (plotly/layer-point {:=x :t
                         :=y :traffic
                         :=color :day-of-week
                         :=mark-size 10})
    (plotly/layer-line {:=x :t
                        :=y :traffic}))

;; A model with all days except for one,
;; dropping one category to avoid multicolinearity
;; (note we begin with Thursday due to the order of appearance):

(-> categorical-toydata
    (tm/design [:traffic]
               ['(tm/one-hot day-of-week)])
    tm/lm
    tm/summary)

;; A model with all days except for one,
;; dropping one category to avoid multicolinearity,
;; and speciftying the order of encoded values:

(-> categorical-toydata
    (tm/design [:traffic]
               ['(tm/one-hot day-of-week
                             {:values days-of-week})])
    tm/lm
    tm/summary)

;; A model with all days and no intercept,
;; dropping the intercept to avoid multicolinearity
;; and have an easier interpretation of the coefficients:

;; Note how the coefficients fit the way we generated the data:

(-> categorical-toydata
    (tm/design [:traffic]
               ['(tm/one-hot day-of-week
                             {:values days-of-week
                              :include-first true})])
    (tm/lm {:intercept? false})
    tm/summary)
