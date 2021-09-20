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
      {:table [["== Level 1\ntext" "<h2>Level 1</h2>Text."]
               ["=== Level 2\ntext" "<h3>Level 2</h3>Text."]
               ["==== Level 3\ntext" "<h4>Level 3</h4>Text."]
               ["===== Level 4\ntext" "<h5>Level 4</h5>Text."]]}]]]
   
   ["Paragraphs"
    [["" {:note "todo"}]]]])

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
           (str/replace cell
             "<pre>"
             "<pre class='highlight plaintext'>")
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

(println (cheatsheet->rb cheatsheet))
