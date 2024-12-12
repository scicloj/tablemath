(ns scicloj.tablemath.v1.api
  (:require [tablecloth.api :as tc]
            [fastmath.vector :as vec]
            [fastmath.ml.regression :as reg]
            [tech.v3.dataset.modelling :as ds-mod]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.tablemath.v1.design-matrix :as dm]))

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


(defn summary [model]
  (kind/code
   (with-out-str
     (println model))))

(defn create-design-matrix [ds targets-specs feature-specs]
  (dm/create-design-matrix ds targets-specs feature-specs))
