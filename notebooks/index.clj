;; Math and stats modelling with table ergonomics

;; # Preface

;; This project is an initial attempt to create a Clojure library for math and statistics which is friendly to [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) and [Tablecloth](https://scicloj.github.io/tablecloth) datasets and uses the functionality of [Fastmath](https://github.com/generateme/fastmath). It is also intended to compose well with [Tableplot](https://scicloj.github.io/tableplot/) layered plotting. It is highly inspired by [R](https://www.r-project.org/) and its package.

;; In a way, it is intended to be a user-friendly compatiblity layer across these libraries.

;; Possibly, after the details clarify, it will be merged into one of the other Scicloj libraries.

;; Tablemath is a Clojure library for math and statistical modeling
;; with table ergonomics, inspired by R.

;; It composes [Tablecloth](https://scicloj.github.io/tablecloth/) datasets
;; with [Fastmath](https://github.com/generateme/fastmath) modeling.

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

;; ## General info
;; |||
;; |-|-|
;; |Website | [https://scicloj.github.io/tablemath/](https://scicloj.github.io/tablemath/)
;; |Source |[![(GitHub repo)](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/scicloj/tablemath)|
;; |Deps |[![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/tablemath.svg)](https://clojars.org/org.scicloj/tablemath)|
;; |License |[EPLv2.0](https://github.com/scicloj/tablemath/blob/main/LICENSE)|
;; |Status |🛠experimental🛠|



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
