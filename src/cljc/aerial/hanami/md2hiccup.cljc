(ns aerial.hanami.md2hiccup
  "As written by niquola and provided here:
  https://github.com/niquola/md-to-hiccup
  "

  (:require
   [clojure.string :as str]
    #?(:cljs [aerial.hanami.utils :as hu :refer [format]])))

(declare parse-inline)

(defn inline-transformers [s]
  (-> s
      (str/replace #"&" "&amp;")
      (str/replace #"&amp;copy;" "©")
      (str/replace #"\t$" "    ")
      #_(str/replace #"<" "&lt;")
      #_(str/replace #">" "&gt;")))

(defn escape-code [s]
  (-> s
      (str/replace #"<" "&lt;")
      (str/replace #">" "&gt;")))

(declare parse-inline)

(def inline-rules
  [{:name :escapes
    :regex
    #"^(\\\\|\\`|\\\*|\\_|\\\{|\\}|\\\[|\\]|\\\(|\\\)|\\#|\\\+|\\-|\\\.|\\!)"
    :f (fn [[_ txt]]
         (subs txt 1))}

   {:name :strong
    :regex #"^(__([^_]+)__)"
    :f (fn [[_ _ txt]]
         [:strong  txt])}

   {:name :empth
    :regex #"^((\*|_)([^*]+)(\*|_))"
    :f (fn [[_ _ _ txt]] [:em txt])}

   {:name :inline-link
    :regex #"^(https?://[^ ]+)"
    :f (fn [[_ txt]]
         (into [:a {:target "_blank" :rel "noopener noreferrer"
                    :href txt} txt]))}


   {:name :auto-link
    :regex  #"^(<(https?://[^ ]+)>)"
    :f (fn [[_ _ txt]] (into [:a {:href txt} txt]))}

   {:name :bold
    :regex #"^(\*\*([^*]+)\*\*)"
    :f (fn [[_ _ txt]]
         [:strong  txt])}

   {:name :double-code
    :regex #"^(``([^`]+)``)"
    :f (fn [[_ _ txt]]
         [:code  (inline-transformers txt)])}

   {:name :inline-code
    :regex #"^(`([^`]+)`)"
    :f (fn [[_ _ txt]]
         [:code  (inline-transformers txt)])}

   {:name :line-break
    :regex #"^( {5}| {2})$"
    :f (fn [& _] [:br ])}

   {:name :link
    :regex #"^(\[([^\]]+)\]\(([^)]*)\))"
    :f (fn [[_ _ txt ref]]
         (let [[ref title & _] (str/split ref #"\s\"")
               attrs (if title
                       {:target "_blank" :rel "noopener noreferrer"
                        :href ref :title (str/replace title #"\"$\s*" "")}
                       {:target "_blank" :rel "noopener noreferrer"
                        :href ref})]
           (into [:a attrs] (parse-inline txt))))}

   {:name :img
    :regex #"^(!\[([^\]]+)\]\(([^)]*)\))"
    :f (fn [[_ _ txt ref]]
         (let [[ref title & _] (str/split ref #"\s\"")
               attrs (if title
                       {:src ref :alt txt
                        :title (str/replace title #"\"$\s*" "")}
                       {:src ref :alt txt})]
           [:img attrs]))}

   {:name :latex
    :regex #"^((\@\()([^@]+)(\@\)))"
    :f (fn[[_ _ _ txt]] (format "\\(%s\\)" txt))
    }

   ])


(defn apply-rules [char-idx txt]
  ;; hardcoded rule for code in code
  (loop [[{:keys [regex f] :as rule} & rules] inline-rules]
    (when rule
      (if-let [re-gr (re-find regex txt)]
        [(+ char-idx (count (second re-gr))) (f re-gr)]
        (recur rules)))))

(apply-rules 0 "*aaa* bb ddd")

(defn parse-inline [txt]
  (loop [acc [] char-idx 0 from-char-idx 0]
    (let [txt-tail (subs txt char-idx)
          [next-char-idx new-elem] (apply-rules char-idx txt-tail)]
      (cond
        (= "" txt-tail) (if (> char-idx from-char-idx)
                          (conj acc
                                (inline-transformers
                                 (subs txt from-char-idx char-idx)))
                          acc)

        new-elem (recur
                  (if (> char-idx from-char-idx)
                    (conj acc
                          (inline-transformers
                           (subs txt from-char-idx char-idx)) new-elem)
                    (conj acc new-elem))
                  next-char-idx next-char-idx)

        :else (recur acc (inc char-idx) from-char-idx)))))

(comment

  (parse-inline "Hello *amigo* how are you")

  (re-find (:regex (first inline-rules)) "\\*")
  (re-find (:regex (first inline-rules)) "\\\\")
  (re-find (:regex (first inline-rules)) "\\.")
  (re-find (:regex (second inline-rules)) "* some text * and some more text")

  (parse-inline
   "text **bold** [my super **link** !!](the-url) ![image](http://theurl)")
  (parse-inline
   "text **bold** *emph* [my super *emph-link* !!](the-url) ![image](http://theurl)")
  (parse-inline
   "text _emph_ [my super _link_ !!](the-url) ![image](http://theurl)")
  )




(declare *parse)

(def ^:private olist-pattern #"^\d\.\s+(.*)")

(defn- string-tag [ln]
  (cond
    (nil? ln) :end-of-file
    (re-matches #"^\s*$" ln) :empty-line
    (re-matches #"^=(=)+$" ln) :old-header-1
    (= ln "- - -") :hr
    (= ln "---") :hr
    (= ln "___") :hr
    (= ln "-------") :hr
    (= ln "***") :hr
    (re-matches #"^--(-)+$" ln) :old-header-2
    (str/starts-with? ln "#") :header
    (re-matches #"^(\s*)(\*|-|\+) .+" ln) :ulist
    (str/starts-with? ln "```") :code
    (str/starts-with? ln ">") :blockquote
    (re-matches olist-pattern ln) :olist
    (re-matches #"^( {4}|\t).*" ln) :pre
    (re-matches #"^ [^ ]+" ln) :text
    :else :text))

(defn- parse-header [txt]
  (let [header-txt (str/replace txt #"^\s*#{1,6}\s+" "")
        header-txt (str/replace header-txt #"\s+(#+)?$" "")
        header-marker (-> #"^(#{1,6})[^#]+.*"
                          (re-seq txt)
                          first
                          second)
        header-tag (->> header-marker
                        count
                        (str "h")
                        keyword)]
    (into [header-tag ] (parse-inline header-txt))))

(defn- parse-old-header [tag lis]
  [tag  (reduce (fn [acc l] (str acc l)) "" lis)])

(defn concat-strings [[i & is]]
  (loop [start true
         [x & xs] is
         prev i
         acc []]
    (cond
      (and (nil? x) (nil? prev)) acc

      (and (nil? x) prev)
      (conj acc (if (and start (string? prev)) (str/triml prev) prev))

      (and prev (string? prev) (string? x))
      (recur false xs (str (str/triml prev) x) acc)

      :else
      (recur false xs x (conj acc prev)))))

(concat-strings ["  a" "b" [:b "ups"] "c"])

(defn- parse-paragraph [lis]
  (let [inline-parsed (mapcat parse-inline
                              (if (< 1 (count lis))
                                (conj (mapv #(str % "\n")
                                            (butlast (filterv identity lis)))
                                      (last (filterv identity lis)))
                                lis))
        res (concat-strings inline-parsed)]
    (into [:p ] res)))


(defn- parse-list [lns]
  (let [fl (first lns)
        [prefix pre-sps sym sps] (re-find #"^(\s*)(\*|-|\+)(\s\s*)" fl)
        max-indent (count sps)
        escapes {"*" "\\*", "+" "\\+", "-" "-"}
        cut-prefix-regex (re-pattern
                          (str "^" pre-sps "(" (get escapes sym) "| )"
                               " {0," max-indent "}"))
        cut-fn (fn [x] (str/replace x cut-prefix-regex ""))
        flash-buf (fn [acc buf]
                    (if-not buf
                      acc
                      (let [item-parsed (*parse buf)]
                        (if (and (= 1 (count item-parsed))
                                 (= :p (ffirst item-parsed)))
                          (conj acc (into [:li] (rest (first item-parsed))))
                          (conj acc (into [:li] item-parsed))))))]
    (loop [acc [:ul]
           buf nil
           [ln & lns :as prev-lns] lns]
      (if (not ln)
        [(flash-buf acc buf) []]
        (let [ln (str/replace ln #"^\t" "    ")]
          (if-not (or (re-find cut-prefix-regex ln)
                      (re-matches #"^\s*$" ln))
            [(flash-buf acc buf) prev-lns]
            (if-not (str/starts-with? ln prefix)
              (recur acc (conj buf (cut-fn ln)) lns)
              (recur (flash-buf acc buf) [(cut-fn ln)] lns))))))))

;; (parse-list [" - a" " - b" " * c"])

;; (re-find #"^(\s*)(\*|-|\+)(\s\s*)" " - a")


;; (str/replace "   c" (first (parse-list [" * a" " * b" " * c"])) "")

;; (parse-list ["* a" " * b" " * c"])

;; (str/replace "   c" (first (parse-list ["* a" " * b" " * c"])) "")


(defn- parse-olist [lns]
  (let [fl (first lns)
        [_ prefix] (re-find #"^(\d.\s*)" fl)
        max-indent (count prefix)
        cut-prefix-regex (re-pattern
                          (str "^(\\d\\.|  ) {0," (- max-indent 2) "}"))
        cut-fn (fn [x] (str/replace x cut-prefix-regex ""))
        flash-buf (fn [acc buf]
                    (if-not buf
                      acc
                      (let [item-parsed (*parse buf)]
                        (if (and (= 1 (count item-parsed))
                                 (= :p (ffirst item-parsed)))
                          (conj acc (into [:li] (rest (first item-parsed))))
                          (conj acc (into [:li] item-parsed))))))]
    (loop [acc [:ol]
           buf nil
           [ln & lns :as prev-lns] lns]
      (if (not ln)
        [(flash-buf acc buf) []]
        (if-not (or (re-find cut-prefix-regex ln) (re-matches #"^\s*$" ln))
          [(flash-buf acc buf) prev-lns]
          (if-not (re-find #"^\d\." ln)
            (recur acc (conj buf (cut-fn ln)) lns)
            (recur (flash-buf acc buf) [(cut-fn ln)] lns)))))))

;; (parse-list ["*     a" "  b" "*  c" " fsdfsssss" "* d" "" "  ups"])

(defn- parse-code [[ln & lns :as prev-lns]]
  (let [lang (second (str/split ln #"```" 2))
        attrs (if (and lang (not (str/blank? lang)))
                {:lang lang :class lang}
                {})]
    (loop [acc []
           [ln & lns :as prev-lns] lns]
      (if-not ln
        [[:pre [:code attrs (escape-code (str/join "\n" acc))]] []]
        (if (str/starts-with? ln "```")
          [[:pre [:code attrs (escape-code (str/join "\n" acc))]] (or lns [])]
          (recur (conj acc ln) lns))))))


(defn- parse-pre [lis]
  (let [[_ rep] (re-matches #"^(\s{4}|\t).*" (first lis))]
    [:pre  (into [:code ]
                   (concat-strings
                    (mapv (fn [x]
                            (-> x
                                (str/replace (re-pattern (str "^" rep)) "")
                                inline-transformers
                                (str "\n")))
                          lis)))]))

(defn- parse-blockquote [lis]
  (let [[_ rep] (re-matches #"^(>\s*).*" (first lis))
        sp-lengh (- (count rep) 1)]
    (-> [:blockquote  ]
        (into (*parse (mapv
                       #(str/replace
                         % (re-pattern (str "^> {0," sp-lengh "}")) "")
                       lis))))))


(defn- *parse [lns]
  (loop [state :default
         [ln & lns :as prev-lns] lns
         acc []
         block-acc []]

    (let [transition (string-tag ln)]
      #_(println "..." [state  transition] ln
                 "; block acc: " block-acc
                 "; acc " acc)
      (let [with-paragraph (fn [acc]
                             (if (not (empty? block-acc))
                               (conj acc (parse-paragraph block-acc))
                               acc))]
        (cond

          ;; header
          (= [state transition] [:default :header])
          (recur :default lns (conj acc (parse-header ln)) [])

          (= [state transition] [:default :hr])
          (recur :default lns (conj acc [:hr ]) [])


          ;; blockquote
          (and (not (or (= state :blockquote-em)
                        (= state :blockquote)))
               (= transition :blockquote))
          (recur :blockquote lns (with-paragraph acc) [ln])

          (= state :blockquote)
          (cond
            (= transition :blockquote)
            (recur :blockquote lns acc (conj block-acc ln))

            (= transition :empty-line)
            (recur :blockquote-em lns acc (conj block-acc ln))

            (= transition :text)
            (recur :blockquote lns acc (conj block-acc ln))

            (= transition :end-of-file)
            (conj acc (parse-blockquote block-acc))

            :else
            (recur :default prev-lns
                   (conj acc (parse-blockquote block-acc)) []))

          (= state :blockquote-em)
          (cond
            (= transition :empty-line)
            (recur :blockquote-em lns acc block-acc)

            (= transition :blockquote)
            (recur :blockquote lns acc (conj block-acc ln))

            :else
            (recur :default prev-lns
                   (conj acc (parse-blockquote block-acc)) []))


          ;; ulist
          (= [state transition] [:default :ulist])
          (let  [[list rest] (parse-list prev-lns)]
            (recur :default rest (conj acc list) []))

          (= [state transition] [:default :olist])
          (let  [[list rest] (parse-olist prev-lns)]
            (recur :default rest (conj acc list) []))

          ;; olist
          (= [:default :pre] [state transition])
          (recur :pre lns acc [ln])

          (= state :pre)
          (cond
            (= transition :pre)
            (recur :pre lns acc (conj block-acc ln))

            :else
            (recur :default prev-lns (conj acc (parse-pre block-acc)) []))


          (= [:default :empty-line] [state transition])
          (recur :default lns acc [])

          ;; code
          (= [:default :code] [state transition])
          (let  [[code rest] (parse-code prev-lns)]
            (recur :default rest (conj acc code) []))

          ;; paragraph
          (= [:default :text] [state transition])
          ;; (and (not= state :blockquote) (= transition :blockquote))
          (recur :paragraph lns acc [ln])

          (= state :paragraph)
          (do
            (cond
              (= :old-header-1 transition)
              (recur :default lns
                     (conj acc (parse-old-header :h1 block-acc)) [])

              (= :old-header-2 transition)
              (recur :default lns
                     (conj acc (parse-old-header :h2 block-acc)) [])

              (= :text transition)
              (recur :paragraph lns acc (conj block-acc ln))

              (= :end-of-file transition)
              (conj acc (parse-paragraph block-acc))

              (= :empty-line transition)
              (recur :default lns (conj acc (parse-paragraph block-acc)) [])

              :else
              (recur :default prev-lns
                     (conj acc (parse-paragraph block-acc)) [])))

          (= transition :empty-line)
          (recur state lns (with-paragraph acc) block-acc)

          ;; alles
          (= transition :end-of-file) acc
          :else (recur :default lns (with-paragraph acc) []))))))

(defn parse [s]
  (into [:div.md] (*parse (str/split s #"(\r\n|\n|\r)"))))

; (parse "## Header [Some link](/url)")

;; (parse "1. Hello
;; 2. Allow
;; 3. Ballow")



;; (parse "

;; *   a list containing a block of code

;;          10 PRINT HELLO INFINITE
;;          20 GOTO 10
;; "
;;        )
#_(parse
 "
1. 1

    - inner par list

2. 2
")

;; (parse
;;  "
;; [![Build Status](https://travis-ci.org/niquola/md-to-hiccup.svg?branch=master)](https://travis-ci.org/niquola/md-to-hiccup)
;; ")

(parse
 "
``app.js``
```js
(function() {
    var BOX_URL = 'https://myapp.aidbox.io';
```
"
 )
