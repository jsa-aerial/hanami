(ns  aerial.hanami.core
  (:require
   [cljs.core.async
    :as async
    :refer (<! >! put! chan)
    :refer-macros [go go-loop]]

   [com.rpl.specter :as sp]

   [aerial.hanasu.client :as cli]
   [aerial.hanasu.common :as com]
   [aerial.hanami.common :as hc]
   [aerial.hanami.templates :as ht]

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
            single-dropdown
            checkbox radio-button slider progress-bar throbber
            horizontal-bar-tabs vertical-bar-tabs
            modal-panel popover-content-wrapper popover-anchor-wrapper]
    :refer-macros [handler-fn]]
   [re-com.box
    :refer [h-box v-box box gap line flex-child-style]]
   [re-com.dropdown
    :refer [filter-choices-by-keyword single-dropdown-args-desc]]

   [aerial.hanami.utils
    :refer [title2 href]]
   ))



;;; Fix this. First 'refer :all' from re-com.core. Then use ns-publics
;;; and deref vars in the resulting map (to get the function objects)
;;; to make re-com-xref.
(def re-com-xref
  (into
   {} (mapv vector
            '[h-box v-box box gap line flex-child-style
              button row-button md-icon-button md-circle-icon-button info-button
              input-text input-password input-textarea
              label title p
              single-dropdown
              checkbox radio-button slider progress-bar throbber
              horizontal-bar-tabs vertical-bar-tabs
              modal-panel popover-content-wrapper popover-anchor-wrapper
              filter-choices-by-keyword single-dropdown-args-desc]
            [h-box v-box box gap line flex-child-style
             button row-button md-icon-button md-circle-icon-button info-button
             input-text input-password input-textarea
             label title p
             single-dropdown
             checkbox radio-button slider progress-bar throbber
             horizontal-bar-tabs vertical-bar-tabs
             modal-panel popover-content-wrapper popover-anchor-wrapper
             filter-choices-by-keyword single-dropdown-args-desc])))

(defn xform-recom
  ([x k v & kvs]
   (let [kvs (->> kvs (partition-all 2) (map vec))]
     (xform-recom x (into re-com-xref (cons [k v] kvs)))))
  ([x kvs]
   (sp/transform
    sp/ALL
    (fn[v] (cond
             (coll? v) (xform-recom v kvs)
             (symbol? v) (let [v (-> v name symbol)]
                           (get kvs v v))
             :else (get kvs v v)))
    x)))


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
   (sp/select-one [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid)]
                  app-db))
  ([tid field]
   (sp/select-one [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid) field]
                  app-db)))

(defn update-tab-field [tid field value]
  (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid) field]
             value app-db))

(defn get-cur-tab
  ([]
   (get-tab-field (deref (get-adb [:tabs :current]))))
  ([field]
   (get-tab-field (deref (get-adb [:tabs :current])) field)))

(defn update-cur-tab [field value]
  (update-tab-field (deref (get-adb [:tabs :current])) field value))

(defn add-tab [tabval]
  (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/AFTER-ELEM] tabval app-db)
  (sp/setval [sp/ATOM :tabs :current sp/ATOM] (tabval :id) app-db))

(defn replace-tab [tid newdef]
  (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid)]
             newdef app-db))

(defn active-tabs []
  (printchan :ACTIVE-TABS " called")
  (let [bar-cursor (rgt/cursor app-db [:tabs :bars])
        curtab (get-cur-tab)]
    (if curtab
      @bar-cursor
      [:p])))




(defn header [msg]
  [title2 msg :class "title"
   ;;:margin-top "2px" :margin-bottom "1px"
   #_:style #_{:background-color "lightgreen"}])


(defn visualize
  [spec elem uopts] #_(printchan :SPEC spec, :UOPTS uopts)
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
      (-> (js/vegaEmbed elem spec (clj->js opts))
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
        (printchan "Did-Mount: called")
        (visualize spec (rgt/dom-node comp) opts)))

    :component-did-update
    (fn [comp old-useless-argv]
      (printchan "Did-Update: called")
      ;; OK, this is a mess. 'new-useful-argv' is really only useful
      ;; in conjunction with old-useless-argv. Even then you may not
      ;; really get the data / args you want/need/expect. Only the
      ;; new-argv-2nd-way gives the complete argument set.
      (let [new-useful-argv (rgt/children comp)
            new-argv-2nd-way (rgt/argv comp) ; first elt some fn?!?
            new-spec (-> new-argv-2nd-way rest first)
            opts (-> new-argv-2nd-way rest second)]
        #_(printchan :ARGV1 new-useful-argv :ARGV2 (rest new-argv-2nd-way))
        (visualize new-spec (rgt/dom-node comp) opts)))

    :reagent-render
    (fn [spec opts]
      [box :child [:div#app]])}))


(defn vis-list [tabid spec-children-pairs opts]
  (let [layout (if (= (get-in opts [:layout :order]) :row) h-box v-box)
        eltnum (get-in opts [:layout :eltsper] 3)
        numspecs (count spec-children-pairs)
        spec-chunks (->> spec-children-pairs
                         (partition-all eltnum)
                         (mapv vec))]
    (printchan "VIS-LIST : " numspecs)
    (for [sub-pairs spec-chunks]
      [layout
       :size (get-in opts [:layout :size] "auto")
       :gap "20px"
       :children
       (for [[spec children] sub-pairs]
         (do #_(js-delete spec "usermeta")
             #_(printchan :NSPEC spec children)
             [v-box :gap "10px"
              :children [[h-box :gap "5px" :children children]
                         [vgl spec opts]]]))])))

(defn hanami []
  (if-let [tabval (get-cur-tab)]
    (let [tabid (tabval :id)
          specs (tabval :specs)
          compvis (tabval :compvis)
          opts (tabval :opts (get-adb [:main :opts]))]
      (cond
        compvis
        (do (printchan "hanami called - has compvis")
            compvis)

        specs
        (let [spec-children-pairs (tabval :spec-children-pairs)
              ;;_ (printchan :SCPAIRS spec-children-pairs)
              compvis (vis-list tabid spec-children-pairs opts)]
          (printchan "hanami called - making compvis")
          (update-cur-tab :compvis compvis)
          compvis)

        :else
        [[:img {:src (get-adb [:main :img])}]]))
    [[:img {:src (get-adb [:main :img])}]]))

(defn tabs []
  (printchan "TABS called ...")
  (let [opts (or (get-cur-tab :opts) (get-adb [:main :opts]))
        size (get-in opts [:layout :size] "auto")
        order (get-in opts [:layout :order] :col)
        layout (if (= order :row) v-box h-box)]
    [layout
     :size size
     :gap "10px"
     :children (hanami)]))

(defn hanami-main []
  (printchan "Hanami-main called ...")
  (let [inst-cursor (rgt/cursor app-db [:instrumentor])
        instrumentor (first @inst-cursor)
        hd-cursor (rgt/cursor app-db [:header])
        header (first @hd-cursor)]
    [v-box
     :gap "10px"
     :children
     [[v-box :gap "3px"
       :children [[header]
                  [h-box
                   :align :start :max-height "30px"
                   :children [[gap :size "15px"] [active-tabs]]]]]
      [line]
      [tabs]]]))




;;; Messaging ----------------------------------------------------------------


(defn get-ws []
  (->> (get-adb []) keys (filter #(-> % keyword? not)) first))

;; Stop app
(defn app-stop []
  (let [ws (get-ws)
        ch (when ws (get-adb [ws :chan]))]
    (when ch
      (go (async/>! ch {:op :stop :payload {:ws ws :cause :userstop}})))))

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
              [:vgl-as-vg] :rm
              [:tabs] (let [cur-ratom (rgt/atom nil)
                            tabs-ratom (rgt/atom [])]
                        {:current cur-ratom
                         :active tabs-ratom
                         :bars [horizontal-bar-tabs
                                :model cur-ratom
                                :tabs tabs-ratom
                                :on-change #(reset! cur-ratom %)]}))
  (rgt/render [hanami-main]
              (js/document.querySelector "#app")))

(defn update-opts [{:keys [main tab opts]}]
  (if main
    (update-adb [:main :opts]
                (merge-old-new-opts (get-adb [:main :opts]) opts))
    (do (update-tab-field tab :opts
                          (merge-old-new-opts (get-tab-field tab :opts) opts))
        (update-tab-field tab :compvis sp/NONE))))

(defn update-tabs [tabdefs]
  (mapv (fn [{:keys [id label opts specs] :as newdef}]
          (let [tid id
                oldef (get-tab-field tid)
                specs (when specs
                        (->> specs com/ev))
                main-opts (get-adb [:main :opts])
                instrumentor (-> :instrumentor get-adb first)
                spec-children-pairs (mapv #(vector % (instrumentor
                                                      {:tabid tid
                                                       :spec %
                                                       :opts opts}))
                                          specs)]
            (if (not oldef)
              (let [opts (merge-old-new-opts main-opts opts)]
                (add-tab (assoc newdef
                                :opts (merge-old-new-opts main-opts opts)
                                :spec-children-pairs spec-children-pairs
                                :specs specs)))

              (let [oldopts (or (oldef :opts) main-opts)
                    newopts (merge-old-new-opts oldopts opts)]
                (replace-tab tid {:id tid
                                  :label (or label (get-tab-field tid :label))
                                  :opts newopts
                                  :spec-children-pairs spec-children-pairs
                                  :specs specs})))))
        (com/ev tabdefs)))


(defn update-data [data-maps]
  (mapv (fn [{:keys [tid vid data]}]
          :???)
        data-maps))


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

      :data-update
      (update-data data)

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


(defn connect [port]
  (go
    (let [uri (str "ws://localhost:" port "/ws")
          ch (async/<! (cli/open-connection uri))]
      (printchan "Opening client, reading msgs from " ch)
      (loop [msg (<! ch)]
        (let [{:keys [op payload]} msg]
          (user-dispatch ch op payload)
          (when (not= op :stop)
            (recur (<! ch))))))))



(defn default-header-fn []
  [h-box :gap "10px" :max-height "30px"
   :children [[gap :size "5px"]
              [:img {:src (get-adb [:main :logo])}]
              [title
               :level :level3
               :label [:span.bold (get-adb [:main :title])]]
              [gap :size "5px"]
              [title
               :level :level3
               :label [:span.bold (get-adb [:main :uid :name])]]
              [gap :size "30px"]]])

(defn default-instrumentor-fn [{:keys [spec opts]}]
  (let [udata (spec :usermeta)]
    (printchan :UDATA udata :OPTS opts)
    []))

(defn start [& {:keys [elem port header-fn instrumentor-fn]
                :or {header-fn default-header-fn
                     instrumentor-fn default-instrumentor-fn}}]
  (printchan "Element 'app' available, port " port)
  (app-stop)
  (update-adb :elem elem
              :instrumentor [instrumentor-fn]
              :header [header-fn])
  (connect port))




(comment

  (start :elem (js/document.querySelector "#app")
         :port 3003
         :instrumentor-fn test-instrumentor)

  (defn bar-slider-fn [tid val]
    (let [tabval (get-tab-field tid)
          spec-children-pairs (tabval :spec-children-pairs)]
      (printchan "Slider update " val)
      (update-tab-field tid :compvis nil)
      (update-tab-field
       tid :spec-children-pairs
       (mapv (fn[[spec children]]
               (let [cljspec spec
                     data (mapv (fn[m] (assoc m :b (+ (m :b) val)))
                                (get-in cljspec [:data :values]))
                     newspec (assoc-in cljspec [:data :values] data)]
                 [newspec children]))
             spec-children-pairs))))

  (defn test-instrumentor [{:keys [tabid spec opts]}]
    (printchan "Test Instrumentor called" :TID tabid #_:SPEC #_spec)
    (let [cljspec spec
          udata (cljspec :usermeta)] (update-adb [:udata] udata)
      (cond
        (not (map? udata)) []

        (udata :slider)
        (let [sval (rgt/atom "0.0")]
          (printchan :SLIDER-INSTRUMENTOR)
          (xform-recom (udata :test1)
                       :m1 sval
                       :oc1 #(do (bar-slider-fn tabid %)
                                 (reset! sval (str %)))
                       :oc2 #(do (bar-slider-fn tabid (js/parseFloat %))
                                 (reset! sval %))))

        (udata :test2)
        [[gap :size "10px"]
         [label :label "Select a demo"]
         [single-dropdown
          :choices (udata :test2)
          :on-change #(printchan "Dropdown: " %)
          :model nil
          :placeholder "Hi there"
          :width "100px"]]

        :else
        [[[gap :size "10px"]
          [label :label "No user map data"]]])))



  (let [tid :tab2
        field :specs
        value [{:spec2 :aspec}]]
    (sp/setval [sp/ATOM :tabs :active sp/ALL sp/ATOM #(= (% :id) tid) field]
               value test-db))


  ;;; cljs.user=> (defn foo [x] (inc x))
  ;;; #'cljs.user/foo
  ;;; cljs.user=> (#'cljs.user/foo 3)
  ;;; 4
  ;;; cljs.user=> (def bar 10)
  ;;; #'cljs.user/bar
  ;;; cljs.user=> @#'cljs.user/bar
  ;;; 10
  ;;; 5:24 PM


  (get-adb [:test1 :sval])
  (update-adb [:main :uid :name] "Hello")

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


  (get-adb :tabs)

  {:op :tabs
   :data [{:id :px
           :label "MultiChartSVG"
           :opts {:vgl {:renderer "svg"}
                  :layout {:size "auto"}}
           :specs [js/vglspec js/vglspec2
                   js/vglspec3 js/vglspec]}]}
  )
