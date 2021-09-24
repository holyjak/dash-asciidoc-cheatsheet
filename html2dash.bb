(ns html2dash
  (:require
   [clojure.string :as str]
   [babashka.pods :as pods]
   [selmer.parser :as selmer]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require
  '[pod.retrogradeorbit.bootleg.utils :refer [convert-to]]
  '[pod.retrogradeorbit.bootleg.enlive :as enlive]
  '[pod.retrogradeorbit.hickory.select :as s]
  '[pod.retrogradeorbit.hickory.render :refer [hickory-to-html]])

;; --------------------------------------------- HTML -> [cats [entries]]

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
     [:div.ulist]
     :some-content-2]
    ;; Otherwise content for a nameless entry:
    [:div.exampleblock "content right under h2 w/o h3"]]])

(defn heading-elm->text [heading-elm]
  (first (:content heading-elm)))

(defn elm->child-elms [elm]
  (->> elm :content (remove string?)))

(defn map-container-children
  [f containers-selector elm]
  (->> elm
       (s/select containers-selector)
       (sequence (comp
                   (map elm->child-elms)
                   (map f)))))

(defn entry-tuple->title+html [[heading-elm & body-elms]]
  (assert (= :h3 (:tag heading-elm)))
  (assert (seq body-elms))
  [(let [h (heading-elm->text heading-elm)]
     ;; Remove all separator characters such as &nbsp;, check:
     (if (empty? (str/replace h #"\p{Z}" "")) "" h))
   (->> body-elms
        (map hickory-to-html)
        (str/join "\n"))])

(defn category-tuple->title+entries [[heading-elm body-elm]]
  (assert (= :h2 (:tag heading-elm)))
  (assert (-> body-elm :attrs :class #{"sectionbody"}))
  [(heading-elm->text heading-elm)
   (map-container-children
     entry-tuple->title+html
     (s/and (s/tag :div) (s/class :sect2))
     body-elm)])

(def hickory->categories
  (partial map-container-children
    category-tuple->title+entries
    (s/and (s/tag :div) (s/class :sect1))))

;; ------------------------------------------- html -> data -> Ruby
(defn indent [strs]
  (map #(str "  " %) strs))

(defn flatten-one [col]
  (reduce
    #(if (coll? %2)
       (into %1 %2)
       (conj %1 %2))
    []
    col))

(defn entry->rb-lines [[name html]]
  ["entry do"
   (str "  name '" name "'")
   "  notes <<-'HTMLEND'"
   html
   "  HTMLEND"
   "end"])

(defn category->rb-lines [[title entries]]
  (flatten-one
    ["category do"
     (str "  id '" title "'")
     (indent (mapcat entry->rb-lines entries))
     "end"]))

;(-> cheatsheet-data first second)

(defn hickory->ruby [hickory]
  (let [body
        (->> (hickory->categories hickory)
             (mapcat category->rb-lines)
             (indent)
             (str/join "\n"))]
    (str
      (slurp "header.rb")
      "\n"
      body
      "\n"
      (slurp "footer.rb"))))


(def html (slurp "web-cheatsheet.html"))
(def hickory (convert-to html :hickory)) ; = (->> html hick/parse hick/as-hickory)
(comment (def cheatsheet-data (hickory->categories hickory)))

(spit "dash-asciidoc-cheatsheet.rb"
  (hickory->ruby hickory))

(println "File `dash-asciidoc-cheatsheet.rb` written")

(comment
  ;; FIXME create AsciiDoctor.docset/Contents//Resources/docSet.dsidx
  (selmer/cache-off!)
  (selmer/set-resource-path! (System/getProperty "user.dir"))
  (spit "index.html" 
    (selmer/render-file "./cheatsheet.template.html" {:categories cheatsheet-data})))