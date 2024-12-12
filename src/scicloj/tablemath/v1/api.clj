(ns scicloj.tablemath.v1.api
  (:require [tablecloth.api :as tc]
            [fastmath.vector :as vec]
            [fastmath.ml.regression :as reg]
            [tech.v3.dataset.modelling :as ds-mod]))

(defn lm
  "Linear model"
  ([dataset]
   (lm dataset nil))
  ([dataset options]
   (let [inference-column-name (-> dataset
                                   ds-mod/inference-target-column-names
                                   first)
         ds-without-target (-> dataset
                               (tc/drop-columns [inference-column-name]))]
     (reg/lm
      ;; ys
      (get dataset inference-column-name)
      ;; xss
      (tc/rows ds-without-target)
      ;; options
      (merge {:names (-> ds-without-target
                         tc/column-names
                         vec)}
             options)))))
