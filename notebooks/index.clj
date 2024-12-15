;; # Preface

^:kindly/hide-code
(ns index
  (:require [scicloj.metamorph.ml.toydata.ggplot :as ggtoydata]
            [clojure.string :as str]
            [tablemath-book.utils :as utils]
            [scicloj.tablemath.v1.api :as tm]))

^{:kindly/hide-code true
  :kind/md true}
(->> "README.md"
     slurp
     str/split-lines
     (drop 1)
     (str/join "\n"))

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

;; ## A little example

(require '[scicloj.metamorph.ml.toydata.ggplot :as ggtoydata]
         '[scicloj.tablemath.v1.api :as tm])

(-> ggtoydata/mpg
    (tm/design [:hwy]
               ['(tm/polynomial displ 2)
                '(tm/one-hot cyl)])
    tm/lm
    tm/summary)

