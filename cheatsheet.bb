;; Babashka script to produce a Dash cheatsheet-producing Ruby code
;; NOTE: I could also use nbb and showdown to do Markdown -> HTML myself instead of
;; relying on the weird impl. in the Ruby lib; see https://github.com/borkdude/nbb/blob/main/examples/showdown/example.cljs
(def cheatsheet
  [; Categories
   ["Document header"
    ; entries
    [[""
      {:note "```
              Main Header
              ===========
              Optional Author Name <optional@author.email>
              Optional version, optional date
              :Author:    AlternativeWayToSetOptional Author Name
              :Email:     <AlternativeWayToSetOptional@author.email>
              :Date:      AlternativeWayToSetOptional date
              :Revision:  AlternativeWayToSetOptional version
              ```"}]]]
   
   ["Attributes"
    [["" 
      {:note "There a lot of predefined attributes in AsciiDoc, plus you can add your own. To get attribute value use {attributename} syntax."}
      {:table [["`Author is {author}`"  "Author is Alex Efros"]
               ["Version is {revision}" "Version is 2.2.2"]
               ["<pre>:My name: Alex Efros\nMy name is {myname}</pre>" "My name is Alex Efros"]
               ["<pre>Line\nwith bad attribute {qwe} will be\ndeleted</pre>" "Line deleted"]
               ["`Escaped: \\{qwe} and +++{qwe}+++`" "Escaped: {qwe} and {qwe}"]]}]]]
   
   ["Headers"
    [[""
      {:table [["<pre>== Level 1\ntext</pre>" "<h2>Level 1</h2>Text."]
               ["<pre>=== Level 2\ntext</pre>" "<h3>Level 2</h3>Text."]
               ["<pre>==== Level 3\ntext</pre>" "<h4>Level 3</h4>Text."]
               ["<pre>===== Level 4\ntext</pre>" "<h5>Level 4</h5>Text."]]}]]]
   
   ["Paragraphs"
    [["" 
      {:table [[[:pre ".Optional Title\nUsual paragraph."]
                [:div [:div.title "Optional Title"]
                 [:p "Usual paragraph"]]]
               [[:pre ".Optional Title\n```clojure\n(hello!)\n```"]
                [:div [:div.title "Optional Title"]
                 [:pre [:code "(hello!)"]]]]
               ]}]
     ["Admonitions"
      {:table [[[:pre "NOTE: This is an example\n      single-paragraph note."]
                [:table [:tr [:td "NOTE"] [:td [:div.title "Optional Title"]
                                           [:p "This is an example single-paragraph note."]]]]]
               [[:code "TIP: (i) Useful to know"]
                [:table.admon [:tr [:td "TIP"] [:td [:p "(i) Useful to know"]]]]]
               [[:code "IMPORTANT: (!)"]
                [:table.admon [:tr [:td "IMPORTANT"] [:td [:p "(!)"]]]]]
               [[:code "WARNING: Warning"]
                [:table.admon [:tr [:td "WARNING"] [:td [:p "Warning"]]]]]
               [[:code "CAUTION: Beware...."]
                [:table.admon [:tr [:td "CAUTION"] [:td [:p "Beware..."]]]]]]}]]]])

(defn indent [strs]
  (map #(str "  " %) strs))

(defn flatten-one [col]
  (reduce
    #(if (coll? %2)
       (into %1 %2)
       (conj %1 %2))
    []
    col))

(defn rubify 
  "Apply the `f` transformer to each entry, 
    flatten the returned lines, indent them"
  [f col]
  (->> col
       (mapcat (comp flatten-one f))
       (indent)))

(defn table->lines [rows]
  (flatten
    ["<table>"
     (for [row rows]
       ["<tr>"
        (for [cell row]
          ["<td>"
           (-> (cond
                 (vector? cell) (hiccup.core/html cell)
                 (string? cell) cell
                 :else (throw (ex-info "Invalid type" {:cell cell})))
             (str/replace
               "<pre>"
               "<pre class='highlight plaintext'>"))
           "</td>"])
        "</tr>"])
     "</table>"]))

(defn block-body->rb [block-map]
  (let [[type content] (first block-map)]
    (case type
      :note (->> content
                 (str/split-lines)
                 (map #(str/replace % #"^\s*" ""))
                 (indent))
      :table (indent (table->lines content)))))

(defn combine-blocks->rb
  "Max one `notes` allowed => merge multiple note/table into 1"
  [blocks]
  (flatten-one
    ["notes <<-'END'"
     (mapcat block-body->rb blocks)
     "END"]))

(defn entry->rb [[name & blocks]]
  ["entry do"
   (str "  name '" name "'")
   (vec (combine-blocks->rb blocks))
   "end"])

(comment
  (-> (get-in cheatsheet [1 1 0 2])
      :table
      table->lines)
  (->>
   [(get-in cheatsheet [0 1 0])]
   (rubify entry->rb)
   (str/join "\n")
   (println))
  )

(defn category->rb [[id entries]]
  ["category do"
    (str "  id '" id "'")
    (rubify entry->rb entries)    
    "end"])

(defn cheatsheet->rb [categories]
  (->>
   (rubify category->rb categories)
   (str/join "\n")))

(spit "dash-asciidoc-cheatsheet.rb"
  (str (slurp "header.rb")
    "\n"
    (cheatsheet->rb cheatsheet)
    "\n"
    (slurp "footer.rb")))

(println "File `dash-asciidoc-cheatsheet.rb` written")