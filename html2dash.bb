(ns html2dash
  (:require
   [clojure.string :as str]
   [babashka.pods :as pods]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require
  '[pod.retrogradeorbit.bootleg.utils :refer [convert-to]]
  '[pod.retrogradeorbit.bootleg.enlive :as enlive]
  '[pod.retrogradeorbit.hickory.select :as s]
  '[pod.retrogradeorbit.hickory.render :refer [hickory-to-html]])

(def html (slurp "experiment/full.html"))
(def hickory (convert-to html :hickory)) ; = (->> html hick/parse hick/as-hickory)

(defn extract-categories 
  "Returns a sequence of pairs of Hickory nodes: `:tag :h2` (category heading)
  and `:tag :table` (category single entry body)"
  [hickory]
  (let [s-cheatsheet-table (s/and (s/tag :table) (s/class :cheatsheet))]
    (->> hickory
         (s/select
           (s/or
             (s/and
               (s/tag :h2)
               (s/not (s/descendant s-cheatsheet-table s/any)))
             s-cheatsheet-table))
         (partition 2))))

;(second (extract-categories hickory))
;; -------------------------------------------------------------- cheatsheet ruby output
(defn indent [strs]
  (map #(str "  " %) strs))

(defn flatten-one [col]
  (reduce
    #(if (coll? %2)
       (into %1 %2)
       (conj %1 %2))
    []
    col))

(defn entry->rb-lines [content-node]
  ["entry do"
   (str "  name ''")
   "  notes <<-'END'"
   (str "    " (hickory-to-html content-node))
   "  END"
   "end"])

(defn category->rb-lines [{[heading] :content :as heading-node} 
                          content-node]
  (flatten-one
    ["category do"
     (str "  id '" heading "'")
     (indent (entry->rb-lines content-node))
     "end"]))

(defn hickory->ruby [hickory]
  (let [body
        (->> (extract-categories hickory)
             (mapcat (partial apply category->rb-lines))
             (indent)
             (str/join "\n"))]
    (str 
      (slurp "header.rb")
      "\n"
      body
      "\n"
      (slurp "footer.rb"))))

(spit "dash-asciidoc-cheatsheet.rb"
  (hickory->ruby hickory))

(println "File `dash-asciidoc-cheatsheet.rb` written")