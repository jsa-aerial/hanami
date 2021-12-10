(ns  aerial.hanami.core
  (:require
   [cljs.core.async
    :as async
    :refer (<! >! put! chan)
    :refer-macros [go go-loop]]

   [clojure.string :as cljstr]

   [com.rpl.specter :as sp]

   [aerial.hanasu.client :as cli]
   [aerial.hanasu.common :as com]

   [aerial.hanami.md2hiccup :as m2h]
   [aerial.hanami.common :as hc]
   [aerial.hanami.templates :as ht]

   ;; Vega & Vega-Lite
   [cljsjs.vega]
   [cljsjs.vega-lite]
   [cljsjs.vega-embed]
   [cljsjs.vega-tooltip]

   [reagent.core :as rgt]
   [reagent.dom :as rgtd]

   [re-com.core
    :as rcm
    :refer [h-box v-box box border gap line h-split v-split scroller
            button row-button md-icon-button md-circle-icon-button info-button
            input-text input-password input-textarea
            label title alert-box alert-list hyperlink hyperlink-href p
            single-dropdown selection-list multi-select tag-dropdown typeahead
            simple-v-table v-table
            checkbox radio-button slider progress-bar throbber
            horizontal-tabs horizontal-bar-tabs horizontal-pill-tabs
            vertical-bar-tabs vertical-pill-tabs
            modal-panel popover-content-wrapper popover-anchor-wrapper
            datepicker datepicker-dropdown progress-bar input-time]
    :refer-macros [handler-fn]]
   [re-com.box
    :refer [flex-child-style]]
   [re-com.dropdown
    :refer [filter-choices-by-keyword single-dropdown-args-desc]]

   [aerial.hanami.utils
    :refer [title2 href]]
   ))



(def print-chan (async/chan 10))

(go-loop [msg (async/<! print-chan)]
  (js/console.log (print-str msg))
  (recur (async/<! print-chan)))

(defn printchan [& args]
  (async/put! print-chan (clojure.string/join " " args)))



;;; Need a _data_ database :/
;;;
;;; This is needed to handle state NOT associated with component state
;;; or updates. In particular, to avoid triggering their renderings on
;;; completely irrelevant changes.
;;;
;;; First and foremost, no state _directly associated_ with the
;;; messaging system should have any impact on components!!
;;;
(defonce data-db (atom {:dbg {}}))

(defn update-ddb
  ([] (com/update-db data-db {}))
  ([keypath vorf]
   (com/update-db data-db keypath vorf))
  ([kp1 vof1 kp2 vof2 & kps-vs]
   (apply com/update-db data-db kp1 vof1 kp2 vof2 kps-vs)))

(defn get-ddb
  ([] (com/get-db data-db []))
  ([key-path]
   (com/get-db data-db key-path)))


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




(defn md
  "Markdown to Hiccup with implicit support for handling LaTex via
  MathJax. Note: MathJax is _not_ supported in Hanami as it "
  [ & stg]
  (let [base-style {:flex "none", :width "450px", :min-width "450px"}
        x (first stg)
        stg (cljstr/join "\n" (if (map? x) (rest stg) stg))
        stg (cljstr/replace stg "\\(" "\\\\(")
        stg (cljstr/replace stg "\\)" "\\\\)")
        style (->> (if (map? x) (x :style) {}) (merge base-style))
        attr (if (map? x) (assoc x :style style) {:style style})
        hiccup (vec (concat [:div.md attr] (rest (m2h/parse stg))))]
    (print-when [:md] :MD hiccup)
    hiccup))


;;; Fix this. First 'refer :all' from re-com.core. Then use ns-publics
;;; and deref vars in the resulting map (to get the function objects)
;;; to make re-com-xref.
(def re-com-xref
  (into
   {} (mapv vector
            '[h-box v-box box border gap line h-split v-split scroller
              flex-child-style
              button row-button md-icon-button md-circle-icon-button info-button
              input-text input-password input-textarea
              label title alert-box alert-list hyperlink hyperlink-href p
              single-dropdown selection-list multi-select tag-dropdown typeahead
              simple-v-table v-table
              checkbox radio-button slider progress-bar throbber
              horizontal-tabs horizontal-bar-tabs horizontal-pill-tabs
              vertical-bar-tabs vertical-pill-tabs
              modal-panel popover-content-wrapper popover-anchor-wrapper
              filter-choices-by-keyword single-dropdown-args-desc
              datepicker datepicker-dropdown progress-bar input-time
              md]
            [h-box v-box box border gap line h-split v-split scroller
             flex-child-style
             button row-button md-icon-button md-circle-icon-button info-button
             input-text input-password input-textarea
             label title alert-box alert-list hyperlink hyperlink-href p
             single-dropdown selection-list multi-select tag-dropdown typeahead
             simple-v-table v-table
             checkbox radio-button slider progress-bar throbber
             horizontal-tabs horizontal-bar-tabs horizontal-pill-tabs
             vertical-bar-tabs vertical-pill-tabs
             modal-panel popover-content-wrapper popover-anchor-wrapper
             filter-choices-by-keyword single-dropdown-args-desc
             datepicker datepicker-dropdown progress-bar input-time
             md])))

(defn xform-recom
  ([x k v & kvs]
   (let [kvs (->> kvs (partition-all 2) (map vec))]
     (xform-recom x (into re-com-xref (cons [k v] kvs)))))
  ([x kvs]
   (let [xlate-cb (-> :symxlate-cb get-adb first)]
     (sp/transform
      sp/ALL
      (fn[v] (cond
               (coll? v) (xform-recom v kvs)
               (symbol? v) (let [v (-> v name symbol)]
                             (get kvs v (xlate-cb v)))
               :else (get kvs v v)))
      x))))




(declare get-cur-tab)

(defn get-vspec
  ([vid]
   (get-vspec (get-cur-tab :id) vid))
  ([tid vid]
   (sp/select-one [sp/ATOM :vspecs sp/ATOM tid vid]
                  app-db)))

(defn update-vspecs
  ([vid vspec]
   (update-vspecs (get-cur-tab :id) vid vspec))
  ([tid vid vspec]
   (sp/setval [sp/ATOM :vspecs sp/ATOM tid vid]
              vspec app-db)))


(defn get-vgview
  ([vid]
   (get-vgview (get-cur-tab :id) vid))
  ([tid vid]
   (sp/select-one [sp/ATOM :vgviews sp/ATOM tid vid]
                  app-db)))

(defn get-vgviews
  ([]
   (sp/select-any [sp/ATOM :vgviews sp/ATOM]
                  app-db))
  ([tid]
   (sp/select-any [sp/ATOM :vgviews sp/ATOM tid]
                  app-db)))

(defn update-vgviews
  ([vid vgview]
   (update-vgviews (get-cur-tab :id) vid vgview))
  ([tid vid vgview]
   (sp/setval [sp/ATOM :vgviews sp/ATOM tid vid]
              vgview app-db)))

(defn del-vgviews
  ([]
   (sp/setval [sp/ATOM :vgviews sp/ATOM]
              {} app-db) true)
  ([tid]
   (sp/setval [sp/ATOM :vgviews sp/ATOM tid]
              sp/NONE app-db) true)
  ([tid vid]
   (sp/setval [sp/ATOM :vgviews sp/ATOM tid vid]
              sp/NONE app-db) true))


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

(defn get-tab-body [id]
  (select-keys
   (sp/select-one [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) id)] app-db)
   [:id :opts :specs]))


(defn get-last-tid []
  (deref (get-adb [:tabs :last])))

(defn get-cur-tab
  ([]
   (get-tab-field (deref (get-adb [:tabs :current]))))
  ([field]
   (get-tab-field (deref (get-adb [:tabs :current])) field)))

(defn set-cur-tab [tid]
  (let [last-tid (get-cur-tab :id)]
    (sp/setval [sp/ATOM :tabs :last sp/ATOM] last-tid app-db)
    (sp/setval [sp/ATOM :tabs :current sp/ATOM] tid app-db)))

(defn update-cur-tab [field value]
  (update-tab-field (deref (get-adb [:tabs :current])) field value))


(defn tab-pos [tid]
  (let [tabvec (sp/select [sp/ATOM :tabs :active sp/ATOM sp/ALL] app-db)
        cnt (count tabvec)]
    [(sp/select-one [sp/INDEXED-VALS #(-> % second :id (= tid)) sp/FIRST]
                    tabvec)
     cnt tabvec]))

(defn move-tab [tid dir]
  (assert (#{:left :right} dir)
          (str "dir '" dir "' must be :left or :right"))
  (let [tabval (get-tab-field tid)
        [idx cnt curtabs] (tab-pos tid)
        f (cond (and (= dir :left) (= idx 0)) :nop
                (and (= dir :right) (= idx (dec cnt))) :nop
                (= dir :left) dec
                (= dir :right) inc)]
    (when (not= f :nop)
      (let [v (sp/setval [sp/ALL #(= (% :id) tid)] hc/RMV curtabs)
            v (sp/setval (sp/before-index (f idx)) tabval v)]
        (sp/setval [sp/ATOM :tabs :active sp/ATOM] v app-db)
        v))))

(defn add-tab [tabval]
  (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/AFTER-ELEM] tabval app-db)
  (set-cur-tab (tabval :id)))

(defn replace-tab [tid newdef]
  (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid)]
             newdef app-db))

(defn del-tab [tid]
  (let [curid (get-cur-tab :id)
        lastid (get-last-tid)
        cnt (count (sp/select [sp/ATOM :tabs :active sp/ATOM sp/ALL] app-db))
        [idx cnt tvec] (tab-pos curid)] ; get vec B4 removal
    (when (and tid curid)
      (sp/setval [sp/ATOM :tabs :active sp/ATOM sp/ALL #(= (% :id) tid)]
                 hc/RMV app-db)
      (when (and (> cnt 1) (= tid curid))
        (let [prev (dec idx)
              next (inc idx)
              newid (or (and (not= lastid curid) lastid)
                        (when (>= prev 0) (-> prev tvec :id))
                        (when (< next cnt) (-> next tvec :id)))]
          (set-cur-tab newid) :ok)))))


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
    (let [tid (-> spec :usermeta :tab :id)
          vid (-> spec :usermeta :vid) _ (printchan :VID vid)
          vopts (-> spec :usermeta :opts)
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
          vega (if (= vmode "vega-lite")
                 (->> spec js/vegaLite.compile .-spec)
                 spec)]
      (-> (js/vegaEmbed elem vega (clj->js opts))
          (.then (fn [res]
                   (when vid (update-vgviews tid vid res.view))
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
        (visualize spec (rgtd/dom-node comp))))

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
        (visualize new-spec (rgtd/dom-node comp))))

    :reagent-render
    (fn [spec]
      (let [id (or (-> spec :usermeta :vid) (gensym "vid-"))
            elt (js/document.getElementById id)]
        (if elt
          elt
          [box :attr {:id id} :child [:div]])))}))


(defn frameit [spec frame]
  (let [frame-cb (-> :frame-cb get-adb first)
        [spec frame] (frame-cb spec frame)]
    [v-box
     :attr {:id (frame :frameid)}
     :gap "10px"
     :children
     [[h-box :gap "5px" :children (frame :top)]
      [h-box :gap "5px"
       :children (if spec
                   [[h-box :children (frame :left)]
                    [vgl spec]
                    [h-box :children (frame :right)]]
                   [[h-box :children (frame :left)]
                    [h-box :children (frame :right)]])]
      [h-box :gap "5px" :children (frame :bottom)]]]))

(defn vis-list [tabid spec-frame-pairs opts]
  (let [layout (if (= (get-in opts [:order]) :row) h-box v-box)
        lgap   (if (= layout h-box) (opts :cgap) (opts :rgap))
        eltnum (get-in opts [:eltsper] 3)
        numspecs (count spec-frame-pairs)
        spec-chunks (->> spec-frame-pairs
                         (partition-all eltnum)
                         (mapv vec))]
    (printchan "VIS-LIST : " numspecs)
    (for [sub-pairs spec-chunks]
      [layout
       :size (get-in opts [:size] "auto")
       :gap lgap
       :children
       (for [[spec frame] sub-pairs]
         (do #_(js-delete spec "usermeta")
             (print-when [:vis :vis-list] :NSPEC spec frame)
             (frameit spec frame)))])))


(declare make-spec-frame-pairs)

(defn add-to-tab-body
  [id picframe & {:keys [at position opts]
                  :or {at :end position :after opts {}}}]
  (let [tbdy (get-tab-body id)
        opts (merge (tbdy :opts) opts)
        specs (tbdy :specs)
        pos (cond
              (= at :beg) 0
              (= at :end) (dec (count specs))
              :else
              (->> specs
                   (keep-indexed
                    (fn[idx item]
                      (when (-> item :usermeta :frame :fid (= at))
                        idx)))
                   first))
        pos (if (= position :after) (inc pos) pos)
        newspecs (concat (take pos specs) [picframe] (drop pos specs))
        s-f-pairs (make-spec-frame-pairs id opts newspecs)]
    (update-tab-field id :opts opts)
    (update-tab-field id :specs newspecs)
    (update-tab-field id :compvis (vis-list id s-f-pairs opts))
    id))

(defn remove-from-tab-body
  [id fid]
  (let [tbdy (get-tab-body id)
        opts (tbdy :opts)
        specs (tbdy :specs)
        specs (remove #(-> % :usermeta :frame :fid (= fid)) specs)
        s-f-pairs (make-spec-frame-pairs id opts specs)]
    (update-tab-field id :specs specs)
    (update-tab-field id :compvis (vis-list id s-f-pairs opts))
    id))

(defn update-frame [tid fid element content]
  (let [tbdy (get-tab-body tid)
        opts (tbdy :opts)
        specs (tbdy :specs)
        pos (->> specs
                 (keep-indexed
                  (fn[idx item]
                    (when (-> item :usermeta :frame :fid (= fid))
                      idx)))
                 first)
        newspec (if (= element :frame)
                  content
                  (assoc-in (nth specs pos) [:usermeta :frame element] content))
        newspecs (concat (take pos specs)
                         [newspec] ; splice in the updated spec
                         (drop (inc pos) specs))
        s-f-pairs (make-spec-frame-pairs tid opts newspecs)]
    (update-tab-field tid :specs newspecs)
    (update-tab-field tid :compvis (vis-list tid s-f-pairs opts))
    tid))


;;; The following two muck with the DOM directly and are not
;;; recommended. Probably should remove altogether, but leaving for
;;; right now.
(defn get-frame-elements [fid]
  (let [fid (name fid)
        frame (js/document.getElementById fid)
        top (aget frame.childNodes 0)
        bottom (aget frame.childNodes 4)
        middle (aget frame.childNodes 2)
        left (aget middle.childNodes 0)
        vis (aget middle.childNodes 2)
        right (aget middle.childNodes 4)]
    {:top top, :bottom bottom, :left left, :right right, :vis vis}))

(defn update-frame-element [fid element content]
  (let [fid (name fid)
        frame-element ((get-frame-elements fid) element)
        component content
        frame-cb (-> :frame-cb get-adb first)]
    (rgtd/render component frame-element)
    (frame-cb {} {:frameid fid})))




(defn hanami []
  (if-let [tabval (get-cur-tab)]
    (let [tabid (tabval :id)
          specs (tabval :specs)
          compvis (tabval :compvis)
          opts (tabval :opts (get-adb [:main :opts :tab]))
          frame-cb (-> :frame-cb get-adb first)]
      (cond
        compvis
        (do (printchan "hanami called - has compvis")
            (frame-cb)
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
        id     (or (and tabval (str (name (tabval :id)) "-body")) "xHx")
        wrapfn (or (opts :wrapfn) identity)
        extfn  (get-in opts [:extfn])
        size   (get-in opts [:size] "auto")
        order  (get-in opts [:order] :col)
        layout (if (= order :row) v-box h-box)
        lgap   (if (= layout v-box) (opts :rgap) (opts :cgap))]
    (printchan :OPTS opts)
    (if extfn
      (extfn tabval)
      (wrapfn
       [layout :attr {:id id}
        :size size
        :gap lgap
        :children (hanami)]))))

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
  (->> (get-ddb []) keys (filter #(-> % type (= js/WebSocket))) first))

(defn get-chan []
  (let [ws (get-ws)
        ch (when ws (get-ddb [ws :chan]))]
    ch))

;; Send server msg
(defn app-send [msg]
  (printchan "app-send: DEPRECATED, use 'send-msg'")
  (cli/send-msg (get-ws) msg))
(defn send-msg
  ([msg]
   (print-when [:msg :send] "Sending msg " msg)
   (cli/send-msg (get-ws) msg))
  ([op data]
   (send-msg {:op op :data data})))



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
                 last-ratom (rgt/atom nil)
                 tabs-ratom (rgt/atom [])]
             {:current cur-ratom
              :last last-ratom
              :active tabs-ratom
              :bars [horizontal-bar-tabs
                     :model cur-ratom
                     :tabs tabs-ratom
                     :on-change #(set-cur-tab %)]})))

(defn register [{:keys [uid title logo img opts]}]
  (printchan "Registering " uid)
  (update-adb [:main :uid] uid
              [:main :title] title
              [:main :logo] logo
              [:main :img] img
              [:main :opts] (hc/xform opts)
              [:main :session-name] (rgt/atom "")
              [:vgl-as-vg] :rm
              [:vgviews] (atom {}) ; This should not have any reactive update
              [:vspecs] (rgt/atom {}))
  (init-tabs)
  (rgtd/render [hanami-main]
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

(defn make-spec-frame-pairs [tid opts specs]
  (let [instrumentor (-> :instrumentor get-adb first)]
    (mapv #(do (when-let [vid (get-in % [:usermeta :vid])]
                 (update-vspecs vid %))
               (vector (if (empty-chart? %) nil %)
                       (instrumentor
                        {:tabid tid
                         :spec %
                         :opts opts})))
          specs)))

(defn update-tabs [specs]
  (let [tabdefs (make-tabdefs specs)]
    (mapv (fn [{:keys [id label opts specs] :as newdef}]
            (let [tid id
                  oldef (get-tab-field tid)
                  specs (when specs
                          (->> specs com/ev))
                  main-opts (get-adb [:main :opts :tab])
                  instrumentor (-> :instrumentor get-adb first)

                  spec-frame-pairs (make-spec-frame-pairs tid opts specs)]

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
    (update-ddb [ws :line-info :rcvcnt] inc)
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
  (update-ddb
   ws {:chan ch, :line-info {:rcvcnt 0, :sntcnt 0, :errcnt 0}}))


(defn message-dispatch [ch op payload]
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
            (update-ddb [ws :line-info :lastsnt] msg,
                        [ws :line-info :sntcnt] inc))

    :stop (let [{:keys [ws cause]} payload]
            (print-when [:msg :stop] :CLIENT "Stopping reads... Cause " cause)
            (cli/close-connection ws)
            (update-ddb ws :rm))

    :error (let [{:keys [ws err]} payload]
             (printchan :CLIENT :error :payload payload)
             (update-ddb [ws :line-info :errcnt] inc))

    (printchan :CLIENT :WTF :op op :payload payload)))


(defn connect [host port]
  (go
    (let [uri (str "ws://" host ":" port "/ws")
          ch (async/<! (cli/open-connection uri))]
      (printchan "Opening client, reading msgs from " ch)
      (loop [msg (<! ch)]
        (let [{:keys [op payload]} msg]
          (message-dispatch ch op payload)
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


(defn default-frame-cb
  ([])
  ([spec frame] [spec frame]))

(defn get-default-frame []
  {:top [], :bottom [],
   :left [[box :size "0px" :child ""]],
   :right [[box :size "0px" :child ""]]})

(defn make-frame [udata curframe]
  (let [frameid (get-in udata [:frame :fid] (gensym "frame-"))]
    (merge (if (udata :frame)
             (let [default-frame (get-default-frame)
                   framedef (udata :frame)
                   frame-sides (dissoc framedef :fid :at :pos)]
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
           (assoc curframe :frameid frameid))))


(defn default-instrumentor-fn [{:keys [tabid spec opts]}]
  (let [udata (spec :usermeta)]
    (print-when [:instrumentor] chan :UDATA udata :OPTS opts))
  {})

(defn make-instrumentor [userfn]
  (fn [{:keys [tabid spec opts] :as m}]
    (make-frame (spec :usermeta) (userfn m))))


(defn start [& {:keys [elem host port
                       header-fn instrumentor-fn
                       frame-cb symxlate-cb]
                :or {host js/location.hostname
                     header-fn default-header-fn
                     instrumentor-fn default-instrumentor-fn
                     frame-cb default-frame-cb
                     symxlate-cb identity}}]
  (let [instfn (make-instrumentor instrumentor-fn)]
    (printchan "Element 'app' available, port " port)
    (app-stop)
    (update-adb :elem elem
                :symxlate-cb [symxlate-cb]
                :frame-cb [frame-cb]
                :instrumentor [instfn]
                :header [header-fn])
    (connect host port)))


(defn sv!
  "Mirror of server side hmi/sv! Here no need for messages. Just need to
  call `update-tabs` on the vector of vg/vgl-maps."
  [vgl-maps]
  (let [vgl-maps (com/ev vgl-maps)]
    (update-tabs vgl-maps)
    true))








(comment

  ;; Rich Comment!!

  ;; Always run this setup init for any testing
  (do
    (hc/update-defaults
     :USERDATA {:tab {:id :TID, :label :TLBL, :opts :TOPTS}
                :frame {:top :TOP, :bottom :BOTTOM,
                        :left :LEFT, :right :RIGHT
                        :fid :FID}
            :opts :OPTS
                :vid :VID,
                :msgop :MSGOP,
                :session-name :SESSION-NAME}
     :MSGOP :tabs, :SESSION-NAME "Exploring"
     :TID :expl1, :TLBL #(-> :TID % name cljstr/capitalize)
     :OPTS (hc/default-opts :vgl), :TOPTS (hc/default-opts :tab))

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




  ;; =======================================================================
  ;; Testing new incremental tab bodies and user wrapping function support


  ;; Create a tab that includes a user defined WRAPFN
  (add-tab
   {:id :dists
    :label "Dists"
    :specs []
    :opts {:order :row, :eltsper 1, :size "auto"
           :wrapfn (fn[hcomp] [h-split
                               :panel-1 [box
                                         :child [input-textarea
                                                 :model "
1234567890123456789012345678901234567890123456789012345678901234567890123456789"
                                                 :width "650px" :rows 40
                                                 :on-change identity]]
                               :panel-2 [scroller
                                         :max-height "800px"
                                         :max-width "1200px"
                                         :align :start
                                         :child hcomp]
                               :initial-split "33%"
                               :width "2048px"])}})

  ;; Add some page/section title text
  (do (add-to-tab-body
       :dists
       (hc/xform
        ht/empty-chart :FID :dtitle
        :TOP '[[gap :size "50px"]
               [md "# Example Interactive Document Creation"]]))
      :done)


  ;; Render a graphic in it
  (add-to-tab-body
   :dists
   (hc/xform
    ht/bar-chart
    :TITLE "Top headline phrases"
    :TID :dists :FID :dhline :ELTSPER 1
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
     {:x-value 1287 :y-value "make you cry"}]))


  ;; Add empty picture frame with all elements as MD
  (do (add-to-tab-body
       :dists
       (hc/xform
        ht/empty-chart :FID :dwrup1
        :TOP '[[gap :size "200px"][md "# Header top"]]
        :BOTTOM '[[gap :size "200px"] [md "# Header bottom"]]
        :LEFT '[[gap :size "50px"] [md "### Header left"]]
        :RIGHT '[[md "### Header right"]]))
      :done)

  ;; Make some distribution data
  (def obsdist
    (let [obs [[0 9] [1 78] [2 305] [3 752] [4 1150] [5 1166]
               [6 899] [7 460] [8 644] [9 533] [10 504]]
          totcnt (->> obs (mapv second) (apply +))
          pdist (map (fn[[k cnt]] [k (double (/ cnt totcnt))]) obs)]
      pdist))

  ;; Add a picture to the tab
  (do (add-to-tab-body
       :dists
       (hc/xform
        ht/layer-chart
        :TID :dists :FID :dex1
        :TITLE "A Real (obvserved) distribution with incorrect simple mean"
        :HEIGHT 400 :WIDTH 450
        :LAYER
        [(hc/xform ht/bar-layer :XTITLE "Count" :YTITLE "Probability")
         (hc/xform ht/xrule-layer :AGG "mean")]
        :DATA (mapv (fn[[x y]] {:x x :y y :m 5.7}) obsdist)))
      :done)

  ;; Add some more 'writeup'
  (do (add-to-tab-body
       :dists
       (hc/xform
        ht/empty-chart :FID :dwrup2
        :BOTTOM '[[gap :size "50px"]
                  [md "# Fixed distribution
Here we have corrected the mean by properly including item weights"]]))
      :done)

  (do (add-to-tab-body
       :dists
       (hc/xform
        ht/layer-chart
        :TID :dists :FID :dex2
        :TITLE "The same distribution with correct weighted mean"
        :HEIGHT 400 :WIDTH 450
        :LAYER
        [(hc/xform ht/bar-layer :XTITLE "Count" :YTITLE "Probability")
         (hc/xform ht/xrule-layer :X "m")]
        :DATA (mapv (fn[[x y]] {:x x :y y :m 5.7}) obsdist)))
      :done)





  ;; =======================================================================
  ;; Direct noodling on the DOM - not recommended as subverts data flow of
  ;; Reagent/React

  (let [body (js/document.getElementById "expl1-body")
        lastchild body.lastChild
        div (js/document.createElement "div")
        _ (set! (.-id div) "mymd")
        component [h-box
                   :children
                   [[gap :size "20px" :width "20px"]
                    [md
                     "
# Header 1
## Header 2
### Header 3, following is list
* One
* Two
* Three"]]]]
    (.appendChild lastchild div)
    (rgtd/render component lastchild.lastChild)
    (js/console.log lastchild)
    (js/console.log lastchild.lastChild))


  (let [body (js/document.getElementById "expl1-body")
        lastchild body.lastChild]
    (js/console.log body)
    (js/console.log lastchild))

  (let [body (js/document.getElementById "expl1-body")
        lastchild body.lastChild]
    (.appendChild lastchild (js/document.createElement "div"))
    (js/console.log lastchild)
    (js/console.log lastchild.lastChild))

  (let [body (js/document.getElementById "expl1-body")
        lastchild body.lastChild
        component ""]
    (.remove lastchild.lastChild)
    (js/console.log lastchild)
    (js/console.log lastchild.lastChild))

  (let [mymd (js/document.getElementById "mymd")]
    #_(.removeChild mymd.parentNode mymd)
    (.remove mymd))

  (let [body (js/document.getElementById "expl1-body")
        lastchild body.lastChild
        mymd (js/document.getElementById "mymd")]
    (.insertBefore mymd lastchild))




  ;; =======================================================================
  ;; Old original db messing and testing of adding / updating tabs

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
