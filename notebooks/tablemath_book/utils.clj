(ns tablemath-book.utils
  (:require [scicloj.kindly.v4.api :as kindly]
            [clojure.string :as str]
            [scicloj.kindly.v4.kind :as kind]))

(defn include-symbol-name [s]
  (format "[`%s`](#%s)"
          s
          s))

(def symbol-pattern
  #"`[a-z|\-]+`")

(defn familiar-symbol-name? [s]
  (->> (str/replace s #"`" "")
       ((juxt identity
              (partial str "scicloj.tablemath.v1.api/")))
       (map symbol)
       (some resolve)))

(defn enrich-text-with-links [text]
  (or (some->> text
               (re-seq symbol-pattern)
               distinct
               (reduce (fn [current-text s]
                         (if (familiar-symbol-name? s)
                           (str/replace current-text
                                        s
                                        (-> s
                                            (str/replace #"`" "")
                                            include-symbol-name))
                           current-text))
                       text))
      text))

(defn include-fnvar-as-section [fnvar]
  (-> (let [{:keys [name arglists doc]} (meta fnvar)]
        (str (format "### `%s`\n" name)
             (->> arglists
                  (map (fn [l]
                         (->> l
                              pr-str
                              (format "`%s`\n\n"))))
                  (str/join ""))
             (enrich-text-with-links doc)))
      kind/md
      kindly/hide-code))

(defn md [& strings]
  (->> strings
       (str/join " ")
       enrich-text-with-links
       kind/md
       kindly/hide-code))

