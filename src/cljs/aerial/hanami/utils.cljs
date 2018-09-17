(ns aerial.hanami.utils
  [:require
   [clojure.string :as cljstr]
   [cljs.pprint :as pp]
   [re-com.core
    :refer [h-box v-box box gap
            title line label hyperlink hyperlink-href
            align-style]]])

(defn format [fmt & args]
  (let [fmt (cljstr/replace fmt #"%s" "~a")]
    (apply pp/cl-format nil fmt args)))
;;(format "scale('x', datum.%s)" "foo")


;;; (str "https://github.com/Day8/re-com/tree/master/")
(defn href
  "given a label and a relative path, return a component which
  hyperlinks to the GitHub URL in a new tab"
  [label base-url src-path]
  [hyperlink-href
   :label  label
   ;;:style  {:font-size    "13px"}
   :href   (str base-url src-path)
   :target "_blank"])

(defn clicker
  "Makes a 'hyperlink' clickable entity with handler"
  [name action-fn & {:keys [tooltip tooltip-pos]} ]
  [hyperlink
   :label name
   :tooltip tooltip
   :tooltip-position :above-center
   :on-click action-fn
   #_:disabled? ])


(defn panel-title
  "Shown across the top of each page"
  [panel-name src1 src2]
  [v-box
   :children
   [[h-box
     :margin "0px 0px 9px 0px"
     :height "54px"
     :align :end
     :children [[title
                 :label panel-name
                 :level :level1
                 :margin-bottom "0px"
                 :margin-top    "2px"]
                [gap :size "25px"]
                (when src1
                  [h-box
                   :class "all-small-caps"
                   :gap    "7px"
                   :align  :center
                   :children [[label :label "source:" ]
                              [href "component" src1]
                              [label :label "|"  :style {:font-size "12px"}]
                              ;;[line]
                              [href "page" src2]]])]]
    [line]]])

(defn title2
  "2nd level title"
  [text & {:keys [class style]}]
  [title
   :label text
   :level :level2
   :class class
   :style style])

(defn status-text
  "given some status text, return a component that displays that status"
  [status style]
  [:span
   [:span.bold "Status: "]
   [:span {:style style} status]])


(defn right-arrow
  []
  [:svg
   {:height 20  :width 25}
   [:line {:x1 "0" :y1 "10" :x2 "20" :y2 "10"
           :style {:stroke "#888"}}]
   [:polygon {:points "20,6 20,14 25,10"
              :style {:stroke "#888" :fill "#888"}}]])


(defn left-arrow
  []
  [:svg
   {:height 20  :width 25}
   [:line {:x1 "5" :y1 "10" :x2 "20" :y2 "10"
           :style {:stroke "#888"}}]
   [:polygon {:points "5,6 5,14 0,10"
              :style {:stroke "#888" :fill "#888"}}]])
