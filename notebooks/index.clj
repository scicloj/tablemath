;; # Preface

;; Tablemath is a Clojure library for math and statistical modeling
;; with table ergonomics, inspired by R.

;; It composes [Tablecloth](https://scicloj.github.io/tablecloth/) datasets
;; with [Fastmath](https://github.com/generateme/fastmath) modeling.

;; **status:** Experimental. Things are expected to keep moving.

^:kindly/hide-code
(ns index
  (:require [scicloj.metamorph.ml.toydata.ggplot :as ggtoydata]
            [clojure.string :as str]
            [tablemath-book.utils :as utils]
            [scicloj.tablemath.v1.api :as tm]))

(-> ggtoydata/mpg
    (tm/design [:hwy]
               ['(tm/polynomial displ 2)
                '(tm/one-hot cyl)])
    tm/lm
    tm/summary)

;; ## Chapters in this book

^:kindly/hide-code
(defn chapter->title [chapter]
  (or (some->> chapter
               (format "notebooks/tablemath_book/%s.clj")
               slurp
               str/split-lines
               (filter #(re-matches #"^;; # .*" %))
               first
               (#(str/replace % #"^;; # " "")))
      chapter))

(->> "notebooks/chapters.edn"
     slurp
     clojure.edn/read-string
     (map (fn [chapter]
            (prn [chapter (chapter->title chapter)])
            (format "\n- [%s](tablemath_book.%s.html)\n"
                    (chapter->title chapter)
                    chapter)))
     (str/join "\n")
     utils/md)
