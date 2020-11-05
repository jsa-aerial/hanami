(ns  clientex.core
  (:require
   [cljs.core.async
    :as async
    :refer (<! >! put! chan)
    :refer-macros [go go-loop]]
   [clojure.string :as cljstr]

   [aerial.hanami.core
    :as hmi
    :refer [printchan user-msg
            re-com-xref xform-recom
            default-header-fn
            default-frame-cb
            default-instrumentor-fn
            make-instrumentor start
            update-adb get-adb
            get-vspec update-vspecs
            init-tabs
            hanami-main]]
   [aerial.hanami.common :as hc :refer [RMV]]
   [aerial.hanami.templates :as ht]

   [com.rpl.specter :as sp]

   [reagent.core :as rgt]

   [re-com.core
    :as rcm
    :refer [h-box v-box box gap line h-split v-split scroller
            button row-button md-icon-button md-circle-icon-button info-button
            input-text input-password input-textarea
            label title p
            single-dropdown
            checkbox radio-button slider progress-bar throbber
            horizontal-bar-tabs vertical-bar-tabs
            modal-panel popover-content-wrapper popover-anchor-wrapper]
    :refer-macros [handler-fn]]
   [re-com.box
    :refer [flex-child-style]]
   [re-com.dropdown
    :refer [filter-choices-by-keyword single-dropdown-args-desc]]

   ))


(defn setup [{:keys [uid title logo img opts]}]
  (printchan "Registering " uid)
  (update-adb [:main :uid] uid
              [:main :title] title
              [:main :logo] logo
              [:main :img] img
              [:main :opts] (hc/xform opts)
              [:main :session-name] (rgt/atom "Test")
              [:vgl-as-vg] :rm
              [:vgviews] (atom {}) ; This should not have any reactive update
              [:vspecs] (rgt/atom {}))
  (init-tabs)
  (rgt/render [hanami-main]
              (get-adb :elem)))


(defmethod user-msg :app-init [msg]
  (printchan "Got application initialization msg :app-init"))


(defn app-init [elem]
  (let [instfn (make-instrumentor default-instrumentor-fn)]
    (printchan "Element 'app' available ...")
    (update-adb :elem elem
                :symxlate-cb [identity]
                :frame-cb [default-frame-cb]
                :instrumentor [instfn]
                :header [default-header-fn])
    (setup {:uid {:uuid "testing", :name "clientex"}
            :title "Client only Hanami test example"
            :logo "logo.png"
            :img "Small-Himeji_sakura.png"
            :opts hc/default-opts})))

;;; Startup ============================================================== ;;;

(when-let [elem (js/document.querySelector "#app")]
  (hc/add-defaults
   :HEIGHT 400 :WIDTH 450
   :USERDATA {:tab {:id :TID, :label :TLBL, :opts :TOPTS}
              :frame {:top :TOP, :bottom :BOTTOM,
                      :left :LEFT, :right :RIGHT
                      :fid :FID :at :AT :pos :POS}
              :opts :OPTS
              :vid :VID, :msgop :MSGOP, :session-name :SESSION-NAME}
   :AT :end :POS :after
   :VID RMV, :MSGOP :tabs, :SESSION-NAME "Exploring"
   :TID :expl1, :TLBL #(-> :TID % name cljstr/capitalize)
   :OPTS (hc/default-opts :vgl)
   :TOPTS (hc/default-opts :tab))
  (app-init elem))

;;; We will run this one visualization upon page load!
;;; From a clojureverse data-science comment/example
(->
 (hc/xform ht/bar-chart
   :TITLE "Top headline phrases"
   :X :x-value :Y :y-value :YTYPE "nominal"
   :DATA
   [{:x-value 8961 :y-value "will make you"}
    {:x-value 4099 :y-value "this is why"}
    { :x-value 3199 :y-value "can we guess"}
    {:x-value 2398 :y-value "only X in"}
    {:x-value 1610 :y-value "the reason is"}
    {:x-value 1560 :y-value "are freaking out"}
    {:x-value 1425 :y-value "X stunning photos"}
    {:x-value 1388 :y-value "tears of joy"}
    {:x-value 1337 :y-value "is what happens"}
    {:x-value 1287 :y-value "make you cry"}])
 hmi/sv!)


(comment

  ;; When running these make sure your current name space is clientex.core

  ;; From a clojureverse data-science comment/example
  (->
   (hc/xform ht/bar-chart
     :TITLE "Top headline phrases"
     :X :x-value :Y :y-value :YTYPE "nominal"
     :DATA
     [{:x-value 8961 :y-value "will make you"}
      {:x-value 4099 :y-value "this is why"}
      { :x-value 3199 :y-value "can we guess"}
      {:x-value 2398 :y-value "only X in"}
      {:x-value 1610 :y-value "the reason is"}
      {:x-value 1560 :y-value "are freaking out"}
      {:x-value 1425 :y-value "X stunning photos"}
      {:x-value 1388 :y-value "tears of joy"}
      {:x-value 1337 :y-value "is what happens"}
      {:x-value 1287 :y-value "make you cry"}])
   hmi/sv!)

  ;; The stanard IDL simple car scatter plot
  (-> (hc/xform ht/point-chart
        :UDATA "data/cars.json"
        :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
      hmi/sv!)

  ;; Example where template is a vega-lite spec. But we do merge in
  ;; the :usermeta in order to use our tabs
  (->
   (hc/xform
    {:usermeta :USERDATA
     :width 500,
     :height 300,
     :data {:url "data/airports.csv"},
     :projection {:type "albersUsa"},
     :mark "circle",
     :encoding {:longitude {:field "longitude", :type "quantitative"},
                :latitude {:field "latitude", :type "quantitative"},
                :tooltip [{:field "name", :type "nominal"}
                          {:field "longitude", :type "quantitative"}
                          {:field "latitude", :type "quantitative"}],
                :size {:value 10}},
     :config {:view {:stroke "transparent"}}}
    :TID :geo)
   hmi/sv!)

  ;; Some distributions
  ;;
  (def obsdist
    (let [obs [[0 9] [1 78] [2 305] [3 752] [4 1150] [5 1166]
               [6 899] [7 460] [8 644] [9 533] [10 504]]
          totcnt (->> obs (mapv second) (apply +))
          pdist (map (fn[[k cnt]] [k (double (/ cnt totcnt))]) obs)]
      pdist))

  (->>
   [(hc/xform ht/layer-chart
      :TID :dists :TOPTS {:order :row, :size "auto"}
      :TITLE "A Real (obvserved) distribution with incorrect simple mean"
      :HEIGHT 400 :WIDTH 450
      :LAYER
      [(hc/xform ht/bar-layer :XTITLE "Count" :YTITLE "Probability")
       (hc/xform ht/xrule-layer :AGG "mean")]
      :DATA (mapv (fn[[x y]] {:x x :y y :m 5.7}) obsdist))

    (hc/xform ht/layer-chart
      :TID :dists
      :TITLE "The same distribution with correct weighted mean"
      :HEIGHT 400 :WIDTH 450
      :LAYER
      [(hc/xform ht/bar-layer :XTITLE "Count" :YTITLE "Probability")
       (hc/xform ht/xrule-layer :X "m")]
      :DATA (mapv (fn[[x y]] {:x x :y y :m 5.7}) obsdist))]
   hmi/sv!)
)
