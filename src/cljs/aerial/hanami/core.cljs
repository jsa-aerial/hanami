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
    :refer [h-box v-box box gap line h-split v-split scroller
            button row-button md-icon-button md-circle-icon-button info-button
            input-text input-password input-textarea
            label title hyperlink-href p
            single-dropdown
            checkbox radio-button slider progress-bar throbber
            horizontal-bar-tabs vertical-bar-tabs
            modal-panel popover-content-wrapper popover-anchor-wrapper]
    :refer-macros [handler-fn]]
   [re-com.box
    :refer [flex-child-style]]
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
            '[h-box v-box box gap line h-split v-split scroller flex-child-style
              button row-button md-icon-button md-circle-icon-button info-button
              input-text input-password input-textarea
              label title hyperlink-href p
              single-dropdown
              checkbox radio-button slider progress-bar throbber
              horizontal-bar-tabs vertical-bar-tabs
              modal-panel popover-content-wrapper popover-anchor-wrapper
              filter-choices-by-keyword single-dropdown-args-desc]
            [h-box v-box box gap line h-split v-split scroller flex-child-style
             button row-button md-icon-button md-circle-icon-button info-button
             input-text input-password input-textarea
             label title hyperlink-href p
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


(defonce app-db (rgt/atom {:dbg {}}))

(def default-opts
  {:vgl {:export true
         :renderer "canvas" #_"svg"
         :mode "vega-lite" #_vega}
   :tab {:order :col
         :eltsper 2
         :size "none"}})

(declare print-when)

(defn update-adb
  ([] (com/update-db app-db :default-opts default-opts))
  ([keypath vorf] #_(printchan [:db :update] "UPDATE-ADB " keypath vorf)
   (com/update-db app-db keypath vorf))
  ([kp1 vof1 kp2 vof2 & kps-vs]
   (apply com/update-db app-db kp1 vof1 kp2 vof2 kps-vs)))

(defn get-adb
  ([] (com/get-db app-db []))
  ([key-path] #_(printchan [:db :get]"GET-ADB " key-path)
   (com/get-db app-db key-path)))

(let [id (atom 0)]
  (defn next-key []
    (str "cnvs-" (swap! id inc))))


(defn set-dbg [dbg-path on-off]
  (let [dbg-path (com/ev dbg-path)
        path (->> dbg-path (concat [:dbg]) vec)]
    (update-adb path on-off)))

(defn dbgon [dbg-path]
  (set-dbg dbg-path true))

(defn dbgoff [dbg-path]
  (set-dbg dbg-path false))

(defn dbg? [dbg-path]
  (let [dbg-path (com/ev dbg-path)
        path (->> dbg-path (concat [:dbg]) vec)]
    (get-adb path)))

(defn print-when [dbg-path & args]
  (when (dbg? dbg-path)
    (apply printchan args)))




(defn get-vspec [vid]
  (sp/select-one [sp/ATOM :vspecs sp/ATOM vid]
                 app-db))

(defn update-vspecs [vid vspec]
  (sp/setval [sp/ATOM :vspecs sp/ATOM vid]
             vspec app-db))


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

(defn set-cur-tab [tid]
  (sp/setval [sp/ATOM :tabs :current sp/ATOM] tid app-db))

(defn update-cur-tab [field value]
  (update-tab-field (deref (get-adb [:tabs :current])) field value))

(defn add-tab [tabval]
  (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/AFTER-ELEM] tabval app-db)
  (set-cur-tab (tabval :id))
  #_(sp/setval [sp/ATOM :tabs :current sp/ATOM] (tabval :id) app-db))

(defn replace-tab [tid newdef]
  (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid)]
             newdef app-db))

(defn del-tab [tid]
  (let [curid ((get-cur-tab) :id)
        cnt (count (sp/select [sp/ATOM :tabs :active sp/ATOM sp/ALL] app-db))]
    (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid)]
               hc/RMV app-db)
    (when (and (> cnt 0) (= tid curid))
      (set-cur-tab (-> (sp/select [sp/ATOM :tabs :active sp/ATOM sp/ALL] app-db)
                       first :id)))))

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
  [spec elem] (print-when [:vis :vis] :SPEC spec)
  (when spec
    (let [vopts (-> spec :usermeta :opts)
          vmode (get vopts :mode)
          default-hover (if (= vmode "vega-lite") false js/undefined)
          spec (clj->js spec)
          opts {:renderer (get vopts :renderer "canvas")
                :mode     "vega"
                :hover    (get vopts :hover default-hover)
                :defaultStyle true
                :scaleFactor (get vopts :scaleFactor 1)
                :actions  {:export (get vopts :export false),
                           :source (get vopts :source false),
                           :editor (get vopts :editor false),
                           :compiled false}}
          vega (if (= vmode "vega-lite") (->> spec js/vl.compile .-spec) spec)]
      (-> (js/vegaEmbed elem vega (clj->js opts))
          (.then (fn [res]
                   #_(js/vegaTooltip.vega res.view spec)
                   #_(js/vegaTooltip.vegaLite res.view spec)))
          (.catch (fn [err]
                    (printchan err)))))))

(defn vgl
  "Reagent component to render vega/vega-lite visualizations."
  [spec]
  (printchan "VGL called")
  (rgt/create-class
   {:display-name "VGL"

    :component-did-mount
    (fn [comp]
      (let [argv (rest (rgt/argv comp))]
        (printchan "Did-Mount: called")
        (visualize spec (rgt/dom-node comp))))

    :component-did-update
    (fn [comp old-useless-argv]
      (printchan "Did-Update: called")
      ;; OK, this is a mess. 'new-useful-argv' is really only useful
      ;; in conjunction with old-useless-argv. Even then you may not
      ;; really get the data / args you want/need/expect. Only the
      ;; new-argv-2nd-way gives the complete argument set.
      (let [new-useful-argv (rgt/children comp)
            new-argv-2nd-way (rgt/argv comp) ; first elt some fn?!?
            new-spec (-> new-argv-2nd-way rest first)]
        (print-when [:vis :vgl :update]
                    :ARGV1 new-useful-argv :ARGV2 (rest new-argv-2nd-way))
        (visualize new-spec (rgt/dom-node comp))))

    :reagent-render
    (fn [spec]
      [box :child [:div#app]])}))


(defn frameit [spec frame]
  (if spec
    [[h-box :children (frame :left)]
     [vgl spec]
     [h-box :children (frame :right)]]
    [[h-box :children (frame :left)]
     [h-box :children (frame :right)]]))

(defn vis-list [tabid spec-frame-pairs opts]
  (let [layout (if (= (get-in opts [:order]) :row) h-box v-box)
        eltnum (get-in opts [:eltsper] 3)
        numspecs (count spec-frame-pairs)
        spec-chunks (->> spec-frame-pairs
                         (partition-all eltnum)
                         (mapv vec))]
    (printchan "VIS-LIST : " numspecs)
    (for [sub-pairs spec-chunks]
      [layout
       :size (get-in opts [:size] "auto")
       :gap "20px"
       :children
       (for [[spec frame] sub-pairs]
         (do #_(js-delete spec "usermeta")
             (print-when [:vis :vis-list] :NSPEC spec frame)
             [v-box :gap "10px"
              :children
              [[h-box :gap "5px" :children (frame :top)]
               [h-box :gap "5px"
                :children (frameit spec frame)]
               [h-box :gap "5px" :children (frame :bottom)]]]))])))

(defn hanami []
  (if-let [tabval (get-cur-tab)]
    (let [tabid (tabval :id)
          specs (tabval :specs)
          compvis (tabval :compvis)
          opts (tabval :opts (get-adb [:main :opts :tab]))]
      (cond
        compvis
        (do (printchan "hanami called - has compvis")
            compvis)

        specs
        (let [spec-frame-pairs (tabval :spec-frame-pairs)
              ;;_ (printchan :SCPAIRS spec-frame-pairs)
              compvis (vis-list tabid spec-frame-pairs opts)]
          (printchan "hanami called - making compvis")
          (update-cur-tab :compvis compvis)
          compvis)

        :else
        [[:img {:src (get-adb [:main :img])}]]))
    [[:img {:src (get-adb [:main :img])}]]))

(defn tabs []
  (printchan "TABS called ...")
  (let [tabval (get-cur-tab)
        opts   (or (and tabval (tabval :opts)) (get-adb [:main :opts :tab]))
        extfn  (get-in opts [:extfn])
        size   (get-in opts [:size] "auto")
        order  (get-in opts [:order] :col)
        layout (if (= order :row) v-box h-box)]
    (if extfn
      (extfn tabval)
      [layout
       :size size
       :gap "10px"
       :children (hanami)])))

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
  (->> (get-adb []) keys (filter #(-> % type (= js/WebSocket))) first))

(defn get-chan []
  (let [ws (get-ws)
        ch (when ws (get-adb [ws :chan]))]
    ch))

;; Send server msg
(defn app-send [msg]
  (printchan "app-send: DEPRECATED, use 'send-msg'")
  (cli/send-msg (get-ws) msg))
(defn send-msg [msg]
  (print-when [:msg :send] "Sending msg " msg)
  (cli/send-msg (get-ws) msg))



(defn merge-old-new-opts [oldopts newopts]
  (cond
    (and oldopts newopts)
    (if (or (oldopts :tab) (oldopts :vgl))
      (sp/transform [(sp/submap [:vgl :tab]) sp/MAP-VALS]
                    #(merge % newopts)
                    oldopts)
      (merge oldopts newopts))

    oldopts oldopts
    newopts newopts
    :else (get-adb [:main :opts])))


;; Stop app
(defn app-stop []
  (let [ws (get-ws)
        ch (get-chan)]
    (when ch
      (go (async/>! ch {:op :stop :payload {:ws ws :cause :userstop}})))))

(defn init-tabs []
  (update-adb
   [:tabs] (let [cur-ratom (rgt/atom nil)
                 tabs-ratom (rgt/atom [])]
             {:current cur-ratom
              :active tabs-ratom
              :bars [horizontal-bar-tabs
                     :model cur-ratom
                     :tabs tabs-ratom
                     :on-change #(reset! cur-ratom %)]})))

(defn register [{:keys [uid title logo img opts]}]
  (printchan "Registering " uid)
  (update-adb [:main :uid] uid
              [:main :title] title
              [:main :logo] logo
              [:main :img] img
              [:main :opts] (hc/xform opts)
              [:main :session-name] (rgt/atom "")
              [:vgl-as-vg] :rm
              [:vspecs] (rgt/atom {}))
  (init-tabs)
  (rgt/render [hanami-main]
              (get-adb :elem)
              #_(js/document.querySelector "#app")))


(defn update-opts [{:keys [main tab opts]}]
  (if main
    (update-adb [:main :opts]
                (merge-old-new-opts (get-adb [:main :opts]) opts))
    (do (update-tab-field tab :opts
                          (merge-old-new-opts (get-tab-field tab :opts) opts))
        (update-tab-field tab :compvis sp/NONE))))



(defn empty-chart? [spec]
  (and (-> spec keys count (= 1)) (-> spec keys first (= :usermeta))))

(defn make-tabdefs [specs]
  (let [grps (group-by #(->> % :usermeta :tab :id) (com/ev specs))]
    (mapv (fn[[id specs]]
            (let [tab (->> specs first :usermeta :tab)]
              (assoc tab :specs (mapv (fn[spec] (dissoc spec :tab)) specs))))
          grps)))

(defn update-tabs [specs]
  (let [tabdefs (make-tabdefs specs)]
    (mapv (fn [{:keys [id label opts specs] :as newdef}]
            (let [tid id
                  oldef (get-tab-field tid)
                  specs (when specs
                          (->> specs com/ev))
                  main-opts (get-adb [:main :opts :tab])
                  instrumentor (-> :instrumentor get-adb first)

                  spec-frame-pairs
                  (mapv #(do (when-let [vid (get-in % [:usermeta :vid])]
                               (update-vspecs vid %))
                             (vector (if (empty-chart? %) nil %)
                                     (instrumentor
                                        {:tabid tid
                                         :spec %
                                         :opts opts})))
                        specs)]

              (if (not oldef)
                (add-tab (assoc newdef
                                :opts (merge-old-new-opts main-opts opts)
                                :spec-frame-pairs spec-frame-pairs
                                :specs specs))

                (let [oldopts (or (oldef :opts) main-opts)]
                  (replace-tab tid
                               {:id tid
                                :label (or label (get-tab-field tid :label))
                                :opts (merge-old-new-opts oldopts opts)
                                :spec-frame-pairs spec-frame-pairs
                                :specs specs})))))
          tabdefs)))


(defn update-data
  "Originally meant as general updater of vis plot/chart data
  values. But to _render_ these, requires knowledge of the application
  pages/structure. So, this is not currently used. If we can figure
  out Vega chageSets and how they update, we may be able to make this
  a general op in Hanami."
  [data-maps app-fn]
  (printchan :UPDATE-DATA data-maps)
  (app-fn (mapv (fn [{:keys [usermeta data]}]
                  (let [vid (usermeta :vid)
                        spec (dissoc (get-vspec vid) :data)]
                    (assoc-in spec [:data :values] data)))
                data-maps)))


(defmulti user-msg :op)

(defmethod user-msg :default [msg]
  (printchan msg))


(defn on-msg [ch ws hanami-msg]
  (let [{:keys [op data]} hanami-msg]
    (update-adb [ws :line-info :rcvcnt] inc)
    (case op

      :register
      (do (register data)
          (user-msg {:op :app-init :data data}))

      :opts
      (update-opts data)

      :tabs
      (update-tabs data)

      #_:data
      #_(update-data data)

      :specs
      (let [{:keys [tab specs]} data]
        (update-tab-field tab :specs specs))

      (user-msg {:op op :data data :ws ws :ch ch}))))


(defn on-open [ch ws]
  (update-adb
   ws {:chan ch, :line-info {:rcvcnt 0, :sntcnt 0, :errcnt 0}}))


(defn user-dispatch [ch op payload]
  (case op
    :open (let [ws payload]
            (print-when [:msg :open] :CLIENT :open :ws ws)
            (on-open ch ws))
    :close (let [{:keys [ws code reason]} payload]
             (print-when :CLIENT :RMTclose :payload payload)
             (go (async/put! ch {:op :stop
                                 :payload {:ws ws :cause :rmtclose}})))

    :msg (let [{:keys [ws data]} payload]
           (print-when [:msg :msg] :CLIENT :msg :payload payload)
           (on-msg ch ws data))

    :bpwait (let [{:keys [ws msg encode]} payload]
              (print-when [:msg :bpwait] :CLIENT "Waiting to send msg " msg)
              (go (async/<! (async/timeout 5000)))
              (print-when [:msg :bpwait] :CLIENT "Trying resend...")
              (cli/send-msg ws msg :encode encode))
    :bpresume (print-when [:msg :bpresume] :CLIENT "BP Resume " payload)

    :sent (let [{:keys [ws msg]} payload]
            (print-when [:msg :sent] :CLIENT "Sent msg " msg)
            (update-adb [ws :line-info :lastsnt] msg,
                        [ws :line-info :sntcnt] inc))

    :stop (let [{:keys [ws cause]} payload]
            (print-when [:msg :stop] :CLIENT "Stopping reads... Cause " cause)
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


(defn set-session-name [name]
  (let [old-uid (get-adb [:main :uid])
        name (if (= name "") (old-uid :name) name)]
    (sp/setval [sp/ATOM :main :session-name sp/ATOM] name app-db)
    (when (not= name (old-uid :name))
      (update-adb [:main :uid :name] name)
      (send-msg {:op :set-session-name
                 :data {:uid old-uid
                        :new-name name}}))))

(defn session-input []
  (if (not= "" (sp/select-one [sp/ATOM :main :session-name sp/ATOM] app-db))
    [:p]
    [input-text
     :model (get-adb [:main :session-name])
     :on-change set-session-name
     :placeholder (get-adb [:main :uid :name])
     :width "100px"]))

(defn default-header-fn []
  [h-box :gap "10px" :max-height "30px"
   :children [[gap :size "5px"]
              [:img {:src (get-adb [:main :logo])}]
              [title
               :level :level3
               :label [:span.bold (get-adb [:main :title])]]
              #_[gap :size "5px"]
              [session-input]
              [gap :size "5px"]
              [title
               :level :level3
               :label [:span.bold (get-adb [:main :uid :name])]]
              [gap :size "30px"]]])


(defn get-default-frame []
  {:top [], :bottom [],
   :left [[box :size "0px" :child ""]],
   :right [[box :size "0px" :child ""]]})

(defn make-frame [udata curframe]
  (merge (if (udata :frame)
           (let [default-frame (get-default-frame)
                 frame-sides (udata :frame)]
             (print-when [:frames :make] :Frame-Maker frame-sides curframe)
             (update-adb [:dbg :frame]
                         (->> (keys frame-sides)
                              (reduce
                               (fn[F k]
                                 (assoc F k (xform-recom
                                             (frame-sides k) re-com-xref)))
                               default-frame)))
             (get-adb [:dbg :frame]))
           (get-default-frame))
         curframe))

(defn default-instrumentor-fn [{:keys [tabid spec opts]}]
  (let [udata (spec :usermeta)]
    (print-when [:instrumentor] chan :UDATA udata :OPTS opts))
  {})

(defn start [& {:keys [elem port header-fn instrumentor-fn]
                :or {header-fn default-header-fn
                     instrumentor-fn default-instrumentor-fn}}]
  (let [instfn (fn [{:keys [tabid spec opts] :as m}]
                 (make-frame (spec :usermeta) (instrumentor-fn m)))]
    (printchan "Element 'app' available, port " port)
    (app-stop)
    (update-adb :elem elem
                :instrumentor [instfn]
                :header [header-fn])
    (connect port)))


(defn sv!
  "Mirror of server side hmi/sv! Here no need for messages. Just need to
  call `update-tabs` on the vector of vg/vgl-maps."
  [vgl-maps]
  (let [vgl-maps (com/ev vgl-maps)]
    (update-tabs vgl-maps)))



(comment

  (do
    (defn bar-slider-fn [tid val]
      (let [tabval (get-tab-field tid)
            spec-frame-pairs (tabval :spec-frame-pairs)]
        (printchan "Slider update " val)
        (update-tab-field tid :compvis nil)
        (update-tab-field
         tid :spec-frame-pairs
         (mapv (fn[[spec frame]]
                 (let [cljspec spec
                       data (mapv (fn[m] (assoc m :b (+ (m :b) val)))
                                  (get-in cljspec [:data :values]))
                       newspec (assoc-in cljspec [:data :values] data)]
                   [newspec frame]))
               spec-frame-pairs))))

    (defn test-instrumentor [{:keys [tabid spec opts]}]
      (printchan "Test Instrumentor called" :TID tabid #_:SPEC #_spec)
      (let [cljspec spec
            udata (cljspec :usermeta)
            default-frame (get-default-frame)]
        (update-adb [:udata] udata)
        (cond
          (not (map? udata)) []

          (udata :slider)
          (let [sval (rgt/atom "0.0")]
            (printchan :SLIDER-INSTRUMENTOR)
            {:top (xform-recom
                   (udata :slider)
                   :m1 sval
                   :oc1 #(do (bar-slider-fn tabid %)
                             (reset! sval (str %)))
                   :oc2 #(do (bar-slider-fn tabid (js/parseFloat %))
                             (reset! sval %)))})

          (udata :test2)
          {:right [[gap :size "10px"]
                   [v-box :children
                    [[label :label "Select a demo"]
                     [single-dropdown
                      :choices (udata :test2)
                      :on-change #(printchan "Dropdown: " %)
                      :model nil
                      :placeholder "Hi there"
                      :width "100px"]]]]}

          :else {}
          )))

    (start :elem (js/document.querySelector "#app")
           :instrumentor-fn test-instrumentor
           :port 3003))




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
    :opts  {:order :col
            :eltsper 2
            :size "none"}
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
                   (get-adb [:defopts :tab])
                   {:size "auto"})
            :specs [js/vglspec js/vglspec2
                    js/vglspec3 js/vglspec]})


  (get-adb :tabs)

  {:op :tabs
   :data [{:id :px
           :label "MultiChartSVG"
           :opts {:size "auto"}
           :specs [js/vglspec js/vglspec2
                   js/vglspec3 js/vglspec]}]}
  )
