(ns scicloj.tablemath.v1.api
  (:require
   [fastmath.ml.regression :as reg]
   [scicloj.kindly.v4.kind :as kind]
   [scicloj.tablemath.v1.protocols :as proto]
   [tablecloth.api :as tc]
   [tablecloth.column.api :as tcc]
   [tech.v3.dataset.column :as ds-col]
   [tech.v3.dataset.modelling :as ds-mod]
   [tech.v3.dataset.categorical :as ds-cat]
   [tech.v3.dataset :as ds]
   [scicloj.tablemath.v1.api :as tm]))

(defn polynomial
  "Given a `column` and an integer `degree`,
  return a vector of columns with all its powers
  up to that degree, named appropriately."
  [column degree]
  (->> (range 1 (inc degree))
       (mapv (fn [i]
               (let [nam (ds-col/column-name column)]
                 (if (= i 1)
                   column
                   (-> (apply tcc/* (repeat i column))
                       (tcc/column {:name (concat-names nam (str i))}))))))))

(defn one-hot
  "Given a `column`, create a vector of integer binary columns,
  each encoding the presence of absence of one of its values.

  E.g., if the `column` name is `:x`, and one of the values is
  `:A`, then a resulting binary column will have 1 in all the rows
  where `column` has `:A`.

  The sequence of values to generate the binary columns is defined
  as follows: either the value provided for the `:values` key if present,
  or the distinct values in `column` in their order of appearance.
  If the value of the option key `:include-first` is `false` (which is the default),
  then the first value is ommitted.
  This is handy for avoiding [multicollinearity](https://en.wikipedia.org/wiki/Multicollinearity)
  in linear regression.

  Supported options:
  - `:values` - the values to encode as columns - default `nil`
  - `:include-first` - should the first value be included - default `false`
  "
  ([column]
   (one-hot column nil))
  ([column {:keys [values
                   include-first]
            :or {values (distinct column)
                 include-first false}}]
   (let [nam (ds-col/column-name column)]
     (-> values
         (cond-> (not include-first) rest)
         (->> (mapv (fn [value]
                      (-> column
                          (tcc/eq value)
                          ;; convert boolean to int
                          (tcc/* 1)
                          (tcc/column
                           {:name (concat-names nam (str "=" value))})))))))))


(defn with
  "Evaluate expression `expr` in the context of destructuring
  all the keys of map `m`."
  [m expr]
  (let [ks (->> m
                keys
                (filter keyword?)
                vec)]
    ((eval `(fn [m#]
              (let [{:keys ~ks} m#]
                ~expr)))
     m)))

(defn- update-colname [col f]
  (if (tcc/column? col)
    (-> col
        (vary-meta update :name f))
    (tcc/column col {:name (f)})))

(defn- concat-names [& names]
  (-> (->> names
           (map #(some-> % name))
           (apply str))
      (cond-> (some keyword? names)
        keyword)))

(defn- keyword-or-string? [v]
  (or (keyword? v)
      (string? v)))

(defn- columns-with-spec
  [dataset spec]
  (if (or (keyword? spec)
          (string? spec))
    [(dataset spec)]
    ;; else
    (let [[nam
           column-or-columns] (if (and (vector? spec)
                                       (-> spec count (= 2))
                                       (-> spec first keyword-or-string?))
                                [(first spec)
                                 (with dataset (second spec))]
                                ;; else
                                [nil
                                 (with dataset spec)])
          columns (cond
                    ;; a map from column names to columns or their data
                    (map? column-or-columns)
                    (->> column-or-columns
                         (map (fn [[k col]]
                                (tcc/column col {:name k}))))
                    ;; a sequential of columns or thir data
                    (-> column-or-columns first sequential?)
                    column-or-columns
                    ;; else, a single column or its data - wrap it
                    :else
                    [column-or-columns])
          more-than-one (-> columns
                            count
                            (> 1))]
      (->> columns
           (map-indexed (fn [i col]
                          (let [suffix (when more-than-one
                                         (str "_" i))]
                            (update-colname
                             col
                             (if nam
                               #(concat-names nam (or % suffix))
                               #(or % (str spec suffix)))))))
           doall))))


(defn- columns-as-a-map [columns map-fn]
  (->> columns
       (mapcat (fn [col]
                 [(ds-col/column-name col)
                  col]))
       (apply map-fn)))

(defn columns-with
  "Compute a sequence of named columns by a given sequence of `specs`
  in the context of a given `dataset`.

  Each spec is one of the following:

  * (1) a keyword or string - in that case, we just take the corresponding
  column of the original dataset.

  * (2) a vector of two elements `[nam expr]`, where the first is a string or a keyword.
  In that case, `nam` is interpreted as a name or a name-prefix
  for the resulting columns, and `expr` is handled as an expression as in (3).

  * (3) any other Clojure form - in that case, we treat it as an expression, and
  evaluate it while destructuring the column names of `dataset`
  as well as all the columns created by previous specs;
  the evaluation is expected to return one of the following:
    * a column (or the data to create a column (e.g., a vector of numbers))
    * a sequential of columns
    * a map from column names to columns

  In any case, the result of the spec is turned into a sequence of named columns,
  which is conctenated to the columns from the previous specs.
  Some default naming mechanisms are invoked if column names are missing.

  Eventually, the sequence of all resulting columns is returned.
  "
  [dataset specs]
  (second
   (reduce (fn [[dataset columns] spec]
             (let [new-columns (columns-with-spec dataset spec)]
               [(tc/add-columns dataset (columns-as-a-map new-columns hash-map))
                (concat columns new-columns)]))
           [dataset []]
           specs)))


(defn design
  "Given a `dataset` and sequences `target-specs`, `feature-specs`,
  generate a new dataset from the columns generated by `columns-with`
  from these two sequences.
  The columns from `target-specs` will be marked as targets
  for modelling (e.g., regression, classification).

  (Inspired by [metamorph.ml.design-matrix](https://github.com/scicloj/metamorph.ml/blob/main/src/scicloj/metamorph/ml/design_matrix.clj)
  but adapted for columnwise computation.)"
  [dataset target-specs feature-specs]
  (let [target-columns (columns-with dataset target-specs)
        feature-columns (columns-with dataset feature-specs)]
    (-> (concat target-columns feature-columns)
        ;; array-map preserves order
        (columns-as-a-map array-map)
        tc/dataset
        (ds-mod/set-inference-target (->> target-columns
                                          (map ds-col/column-name))))))







(defn lm
  "Compute a linear regression model for `dataset`.
  The first column marked as target is the target.
  All the columns unmarked as target are the features.
  The resulting model is of type `fastmath.ml.regression.LMData`,
  a generated by [Fastmath](https://github.com/generateme/fastmath).
  It can be summarized by `summary`.

  See [fastmath.ml.regression.lm](https://generateme.github.io/fastmath/clay/ml.html#lm)
  for `options`."

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

(defn summary
  "Summarize a statistical model as a
  [Kindly](https://scicloj.github.io/kindly/)-compatible
  visual report."
  [model]
  (proto/summary model))

