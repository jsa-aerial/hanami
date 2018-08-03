(ns  aerial.hanami.core
  (:require
   [cljs.core.async
    :as async
    :refer (<! >! put! chan)
    :refer-macros [go go-loop]]

   [aerial.hanasu.client :as cli]
   [aerial.hanasu.common :as com]

   [com.rpl.specter :as sp]

   ;; Vega & Vega-Lite
   [cljsjs.vega]
   [cljsjs.vega-lite]
   [cljsjs.vega-embed]
   [cljsjs.vega-tooltip]

   [reagent.core :as rgt]

   [re-com.core
    :as rcm
    :refer [h-box v-box box gap line
            button row-button md-icon-button md-circle-icon-button info-button
            input-text input-password input-textarea
            label title p
            checkbox radio-button slider progress-bar throbber
            horizontal-bar-tabs vertical-bar-tabs
            modal-panel popover-content-wrapper popover-anchor-wrapper]
    :refer-macros [handler-fn]]
   [re-com.box
    :refer [h-box v-box box gap line flex-child-style]]

   [aerial.hanami.utils
    :refer [title2 href]]
   ))


(def print-chan (async/chan 10))

(go-loop [msg (async/<! print-chan)]
  (js/console.log (print-str msg))
  (recur (async/<! print-chan)))

(defn printchan [& args]
  (async/put! print-chan (clojure.string/join " " args)))


(defonce app-db (rgt/atom {}))

(def default-opts
  {:defopts {:vgl {:export true
                   :renderer "canvas" #_"svg"
                   :mode "vega-lite" #_vega}
             :layout {:order :col
                      :eltsper 2
                      :size "none"}}})

(defn update-adb
  ([] (com/update-db app-db default-opts))
  ([keypath vorf] #_(printchan "UPDATE-ADB " keypath vorf)
   (com/update-db app-db keypath vorf))
  ([kp1 vof1 kp2 vof2 & kps-vs]
   (apply com/update-db app-db kp1 vof1 kp2 vof2 kps-vs)))

(defn get-adb
  ([] (com/get-db app-db []))
  ([key-path] #_(printchan "GET-ADB " key-path) (com/get-db app-db key-path)))

(let [id (atom 0)]
  (defn next-key []
    (str "cnvs-" (swap! id inc))))


(defn get-tab-field
  ([tid]
   (sp/select-one [sp/ATOM :tabs :active sp/ALL #(= (% :id) tid)]
                  app-db))
  ([tid field]
   (sp/select-one [sp/ATOM :tabs :active sp/ALL #(= (% :id) tid) field]
                  app-db)))

(defn update-tab-field [tid field value]
  (sp/setval [sp/ATOM :tabs :active sp/ALL #(= (% :id) tid) field]
             value app-db))

(defn get-cur-tab
  ([]
   (get-tab-field (get-adb [:tabs :current])))
  ([field]
   (get-tab-field (get-adb [:tabs :current]) field)))

(defn update-cur-tab [field value]
  (update-tab-field (get-adb [:tabs :current]) field value))

(defn add-tab [tabval]
  (sp/setval [sp/ATOM :tabs :active sp/AFTER-ELEM] tabval app-db)
  (update-adb [:tabs :current] (tabval :id)))

(defn replace-tab [tid newdef]
  (sp/setval [sp/ATOM :tabs :active sp/ALL #(= (% :id) tid)] newdef app-db))

(defn active-tabs []
  (printchan :ACTIVE-TABS " called")
  (let [curtab (get-adb [:tabs :current])]
    (if curtab
      [horizontal-bar-tabs
       :model curtab
       :tabs (get-adb [:tabs :active])
       :on-change #(update-adb [:tabs :current] %)]
      [:p])))




(defn header [msg]
  [title2 msg :class "title" #_:style #_{:background-color "lightgreen"}])


(defn visualize
  [spec elem uopts] (printchan :UOPTS uopts)
  (when spec
    (let [spec (clj->js spec)
          opts {:renderer (get-in uopts [:vgl :renderer] "canvas")
                :mode     (get-in uopts [:vgl :mode] "vega-lite")
                :actions  {:export (get-in uopts [:vgl :export] false),
                           :source false,
                           :editor false,
                           :compiled false}}
          vega-spec (js/vl.compile spec)]
      #_(update-adb :vgl-as-vg vega-spec)
      (-> (js/vegaEmbed elem  spec (clj->js opts))
          (.then (fn [res]
                   #_(js/vegaTooltip.vega res.view spec)
                   #_(js/vegaTooltip.vegaLite res.view spec)))
          (.catch (fn [err]
                    (printchan err)))))))

(defn vgl
  "Reagent component to render vega/vega-lite visualizations."
  [spec opts]
  (printchan "VGL called")
  (rgt/create-class
   {:display-name "VGL"

    :component-did-mount
    (fn [comp]
      (let [argv (rest (rgt/argv comp))
            opts (second argv)]
        #_(printchan :DIDMNT :ARGV argv)
        (visualize spec (rgt/dom-node comp) opts)))

    :component-did-update
    (fn [comp old-useless-argv]
      (printchan "Did-Update: called")
      (let [new-useful-argv (rgt/children comp)
            new-argv-2nd-way (rgt/argv comp)
            new-spec (first new-useful-argv)
            opts (second new-useful-argv)]
        #_(printchan :ARGV (rest new-argv-2nd-way))
        (visualize new-spec (rgt/dom-node comp) opts)))

    :reagent-render
    (fn [spec opts]
      [box :child [:div#app]])}))


(defn vis-list [specs opts]
  (let [specs (com/ev specs)
        layout (if (= (get-in opts [:layout :order]) :row) h-box v-box)
        eltnum (get-in opts [:layout :eltsper] 3)
        numspecs (count specs)
        spec-chunks (->> specs (partition-all eltnum) (mapv vec))]
    (printchan "VIS-LIST : " (count specs))
    (for [specs spec-chunks]
      [layout
       :size (get-in opts [:layout :size] "auto")
       :gap "20px"
       :children
       (for [spec specs]
         [vgl spec opts])])))

(defn hanami []
  (if-let [tabval (get-cur-tab)]
    (let [spec (tabval :specs)
          compvis (tabval :compvis)
          opts (tabval :opts (get-adb [:main :opts]))]
      (cond
        compvis
        (do (printchan "hanami called - has compvis")
            compvis)

        spec
        (let [compvis (vis-list spec opts)]
          (printchan "hanami called - making compvis")
          (update-cur-tab :compvis compvis)
          compvis)

        :else
        [[:img {:src (get-adb [:main :img])}]]))
    [[:img {:src (get-adb [:main :img])}]]))


(defn hanami-main []
  (printchan "Hanami-main called ...")
  [v-box
   :gap "10px"
   :children
   [[h-box :gap "10px"
     :children [[gap :size "5px"]
                [:img {:src (get-adb [:main :logo])}]
                [header (get-adb [:main :title])]
                [gap :size "5px"]
                [title :level :level3 :label (get-adb [:main :uid])]
                [gap :size "30px"]
                (active-tabs)]]
    [line]
    (let [opts (or (get-cur-tab :opts) (get-adb [:main :opts]))
          size (get-in opts [:layout :size] "auto")
          order (get-in opts [:layout :order] :col)
          layout (if (= order :row) v-box h-box)]
      [layout
       :size size
       :gap "10px"
       :children (hanami)])]])




;;; Messaging ----------------------------------------------------------------


(defn get-ws []
  (->> (get-adb []) keys (filter #(-> % keyword? not)) first))

;; Stop app
(defn app-stop []
  (let [ws (get-ws)
        ch (get-adb [ws :chan])]
    (go (async/>! ch {:op :stop :payload {:ws ws :cause :userstop}}))))

;; Send server msg
(defn app-send [msg]
  (cli/send-msg (get-ws) msg))



(defn merge-old-new-opts [oldopts newopts]
  (cond
    (and oldopts newopts)
    (sp/transform [(sp/submap [:vgl :layout]) sp/MAP-VALS]
                  #(merge % (if (% :mode) (newopts :vgl) (newopts :layout)))
                  oldopts)
    oldopts oldopts
    newopts newopts
    :else (get-adb [:main :opts])))

(defn register [{:keys [uid title logo img opts]}]
  (printchan "Registering " uid)
  (update-adb [:main :uid] uid
              [:main :title] title
              [:main :logo] logo
              [:main :img] img
              [:main :opts] opts
              [:tabs :active] []
              [:tabs :current] :rm
              [:vgl-as-vg] :rm)
  (rgt/render [hanami-main]
              (js/document.querySelector "#app")))

(defn update-opts [{:keys [main tab opts]}]
  (if main
    (update-adb [:main :opts]
                (merge-old-new-opts (get-adb [:main :opts]) opts))
    (update-tab-field tab :opts
                      (merge-old-new-opts (get-tab-field tab :opts) opts))))

(defn update-tabs [tabdefs]
  (mapv (fn[{:keys [id label opts specs] :as newdef}]
          (let [oldef (get-tab-field id)
                specs (when specs
                        (->> specs com/ev (mapv #(.parse js/JSON %))))
                main-opts (get-adb [:main :opts])]
            (if (not oldef)
              (add-tab (assoc newdef
                              :opts (merge-old-new-opts main-opts opts)
                              :specs specs))

              (let [oldopts (or (oldef :opts) main-opts)
                    newopts (merge-old-new-opts oldopts opts)]
                (replace-tab id {:id id
                                 :label (or label (get-tab-field id :label))
                                 :opts newopts
                                 :specs specs})))))
        (com/ev tabdefs)))


(defn on-msg [ch ws hanami-msg]
  (let [{:keys [op data]} hanami-msg]
    (update-adb [ws :line-info :rcvcnt] inc)
    (case op

      :register
      (register data)

      :opts
      (update-opts data)

      :tabs
      (update-tabs data)

      :specs
      (let [{:keys [tab specs]} data]
        (update-tab-field tab :specs specs)))))


(defn on-open [ch ws]
  (update-adb
   ws {:chan ch, :line-info {:rcvcnt 0, :sntcnt 0, :errcnt 0}}))


(defn user-dispatch [ch op payload]
  (case op
    :open (let [ws payload]
            #_(printchan :CLIENT :open :ws ws)
            (on-open ch ws))
    :close (let [{:keys [ws code reason]} payload]
             (printchan :CLIENT :RMTclose :payload payload)
             (go (async/put! ch {:op :stop
                                 :payload {:ws ws :cause :rmtclose}})))

    :msg (let [{:keys [ws data]} payload]
           #_(printchan :CLIENT :msg :payload payload)
           (on-msg ch ws data))

    :bpwait (let [{:keys [ws msg encode]} payload]
              (printchan :CLIENT "Waiting to send msg " msg)
              (go (async/<! (async/timeout 5000)))
              (printchan :CLIENT "Trying resend...")
              (cli/send-msg ws msg :encode encode))
    :bpresume (printchan :CLIENT "BP Resume " payload)

    :sent (let [{:keys [ws msg]} payload]
            (printchan :CLIENT "Sent msg " msg)
            (update-adb [ws :line-info :lastsnt] msg,
                        [ws :line-info :sntcnt] inc))

    :stop (let [{:keys [ws cause]} payload]
            (printchan :CLIENT "Stopping reads... Cause " cause)
            (cli/close-connection ws)
            (update-adb ws :rm))

    :error (let [{:keys [ws err]} payload]
             (printchan :CLIENT :error :payload payload)
             (update-adb [ws :line-info :errcnt] inc))

    (printchan :CLIENT :WTF :op op :payload payload)))


(defn connect []
  (go
    (let [port js/location.port
          uri (str "ws://localhost:" port "/ws")
          ch (async/<! (cli/open-connection uri))]
      (printchan "Opening client, reading msgs from " ch)
      (loop [msg (<! ch)]
        (let [{:keys [op payload]} msg]
          (user-dispatch ch op payload)
          (when (not= op :stop)
            (recur (<! ch))))))))


(when-let [elem (js/document.querySelector "#app")]
  (update-adb [:main :title] "花見 Hanami"
              [:main :uid] ""
              [:main :port] "3000"
              [:main :logo] "logo.png"
              [:main :img] "Himeji_sakura.jpg"
              [:main :opts] {:vgl {:export true
                                   :renderer "canvas" #_"svg"
                                   :mode "vega-lite" #_vega}
                             :layout {:order :col
                                      :eltsper 2
                                      :size "none"}}
              [:tabs :active] []
              [:tabs :current] :rm
              [:vgl-as-vg] :rm)
  (printchan "Element 'app' available, port " js/location.port)
  (rgt/render [hanami-main]
              (js/document.querySelector "#app")))


(comment

  (go
    (let [port 3000 ;;js/location.port
          uri (str "ws://localhost:" port "/ws")
          ch (async/<! (cli/open-connection uri))]
      (printchan "Opening client, reading msgs from " ch)
      (def hanami-handler
        (loop [msg (<! ch)]
          (let [{:keys [op payload]} msg]
            (user-dispatch ch op payload)
            (when (not= op :stop)
              (recur (<! ch))))))))
  )


(comment

  (get-adb)
  (update-adb [:main :uid] "Hello")

  (add-tab
   {:id :p1
    :label "BarChart"
    :opts {:vgl {:export true
                 :renderer "canvas" #_"svg"
                 :mode "vega-lite" #_vega}
           :layout {:order :col
                    :eltsper 2
                    :size "none"}}
    :specs [js/vglspec]})

  (add-tab {:id :p2
            :label "BarChart/Mean"
            :specs [js/vglspec3]})

  (add-tab {:id :p3
            :label "MultiChart"
            :specs [js/vglspec js/vglspec2
                    js/vglspec3 js/vglspec]})

  (add-tab {:id :ttest
            :label "ToolTip"
            :specs js/ttest})

  (add-tab {:id :geotest
            :label "GeoTest"
            :specs js/geotest})
  (update-tab-field :geotest :specs js/geotest)

  (add-tab {:id :px
           :label "MultiChartSVG"
            :opts (merge-old-new-opts
                   (get-adb [:main :opts])
                   {:vgl {:renderer "svg"}
                    :layout {:size "auto"}})
            :specs [js/vglspec js/vglspec2
                    js/vglspec3 js/vglspec]})

  (update-adb :tabs :current :p1)


  (get-adb :tabs)

  {:op :tabs
   :data [{:id :px
           :label "MultiChartSVG"
           :opts {:vgl {:renderer "svg"}
                  :layout {:size "auto"}}
           :specs [js/vglspec js/vglspec2
                   js/vglspec3 js/vglspec]}]}
  )
