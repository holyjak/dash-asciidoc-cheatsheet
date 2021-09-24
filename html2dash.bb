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

(def html (slurp "web-cheatsheet.html"))
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

(defn entry->rb-lines [[name html]]
  ["entry do"
   (str "  name '" name "'")
  ;;  "  notes <<-'END'"
  ;;  (str/replace html #"\n" " ")
  ;;  "  END"
   (str "  notes '" (str/replace html #"\n" " "))
   "end"])

(defn category->rb-lines [[title entries]]
  (flatten-one
    ["category do"
     (str "  id '" title "'")
     (indent (entry->rb-lines entries))
     "end"]))

(defn hickory->ruby [hickory]
  (let [pairs-tree
        (->> hickory
             (s/select (s/and (s/tag :div) (s/class :sect1)))
             (sequence (comp
                         (map elm->child-elms)
                         (map category-pair->title+entries))))

        body
        (->> pairs-tree
             (mapcat category->rb-lines)
             (indent)
             (str/join "\n"))]
    (str
      (slurp "header.rb")
      "\n"
      body
      "\n"
      (slurp "footer.rb"))))

;; (spit "dash-asciidoc-cheatsheet.rb"
;;   (hickory->ruby hickory))

;; (println "File `dash-asciidoc-cheatsheet.rb` written")

(comment
  ;; AsciiDoc output structure:
  [:div.sect1 ;; Category
   [:h2 "category title"]
   [:div.sectionbody
    ;; Entry, if there is a h3 heading:
    [:div.sect2
     [:h2 "entry name"]
     [:div.paragraph "some para..."]
     [:div.literalblock "some .... ... ...."]
     [:div.ulist ]
     :some-content-2]
    ;; Otherwise content for a nameless entry:
    [:div.exampleblock "content right under h2 w/o h3"]]]
  )

(defn heading-elm->text [heading-elm]
  (first (:content heading-elm)))

(defn elm->child-elms [elm]
  (->> elm :content (remove string?)))

(defn entry-pair->title+html [[heading-elm & body-elms]]
  [(let [h (heading-elm->text heading-elm)]
     ;; Remove all separator characters such as &nbsp;, check:
     (if (empty? (str/replace h #"\p{Z}" "")) "" h))
   (->> body-elms 
        (map hickory-to-html)
        (str/join "\n"))])

(defn category-pair->title+entries [[heading-elm body-elm]]
  (assert (= :h2 (:tag heading-elm)))
  (assert (-> body-elm :attrs :class #{"sectionbody"}))
  [(heading-elm->text heading-elm)
   (->> body-elm
        (s/select (s/and (s/tag :div) (s/class :sect2)))
        (sequence (comp
                    (map elm->child-elms)
                    (map entry-pair->title+html))))])

;; (->> hickory
;;      (s/select (s/and (s/tag :div) (s/class :sect1)))
;;      (sequence (comp
;;                  (map elm->child-elms)
;;                  (map category-pair->title+entries))))

(def html-empty " ")
(= " " " ")
(-> " " seq first int Character/getName)
(Character/codePointOf "NO-BREAK SPACE")
(java.lang.Character$UnicodeBlock/of 160)
(Character/isWhitespace (first html-empty))
