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

(pods/load-pod 'org.babashka/go-sqlite3 "0.0.1")
(require '[pod.babashka.go-sqlite3 :as sqlite])
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

(defn empty-entry-name? [name]
  (empty? (str/replace name #"\p{Z}" "")))

(defn entry-tuple->title+html [[heading-elm & body-elms]]
  (assert (= :h3 (:tag heading-elm)))
  (assert (seq body-elms))
  [(let [h (heading-elm->text heading-elm)]
     ;; Remove all separator characters such as &nbsp;, check:
     (if (empty-entry-name? h) "" h))
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

(def html (slurp "web-cheatsheet.html"))
(def hickory (convert-to html :hickory)) ; = (->> html hick/parse hick/as-hickory)
(comment (def cheatsheet-data (hickory->categories hickory)))

(selmer/set-resource-path! (System/getProperty "user.dir"))
(spit "index.html" 
    (selmer/render-file "./cheatsheet.template.html" {:categories (hickory->categories hickory)}))

(println "File `index.html` written")

(comment
  (doseq [[_ entries] cheatsheet-data, [entry-name] entries]
    (when-not (empty-entry-name? entry-name)
      (println entry-name)))
  
  (doseq [[category-id] cheatsheet-data]
    (println category-id))

  (sqlite/execute! "docSet.dsidx"
    ["create table searchIndex(id integer primary key, name TEXT, type TEXT, path TEXT)"])
  (sqlite/execute! "docSet.dsidx"
    ["create unique index anchor on searchIndex (name,type,path)"])
  (sqlite/execute! "docSet.dsidx"
    ["insert into searchIndex(name, type, path) values (?,?,?),(?,?,?),(?,?,?)"
     "AsciiDoctor" "Category" "index.html",
     "MY_CATEGORY" "Category" "index.html#//dash_ref/Category/MY_CATEGORY/1",
     "MY_ENTRY" "Entry" "index.html#//dash_ref_MY_CATEGORY/Entry/MY_ENTRY/0"])
  (sqlite/query "AsciiDoctor.docset//Contents/Resources/docSet.dsidx" 
    ["select * from searchIndex"])
  ;; FIXME create AsciiDoctor.docset/Contents//Resources/docSet.dsidx
  (selmer/cache-off!)
  )