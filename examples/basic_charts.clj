(ns hanami.basic-charts

  (:require [clojure.string :as str]
            #_[clojure.data.csv :as csv]
            [clojure.data.json :as json]

            #_[aerial.utils.math.probs-stats :as p]
            #_[aerial.utils.math.infoth :as it]

            #_[aerial.utils.string :as str]
            #_[aerial.utils.coll :as coll]

            [aerial.hanami.common :as hc :refer [RMV]]
            [aerial.hanami.templates :as ht]
            [aerial.hanami.core :as hmi]))



(defn log2 [x]
  (let [ln2 (Math/log 2)]
    (/ (Math/log x) ln2)))

(defn roundit [r & {:keys [places] :or {places 4}}]
  (let [n (Math/pow 10.0 places)]
    (-> r (* n) Math/round (/ n))))


(defn connfn [data]
  (assoc data :exps ["eid1" "eid2" "eid3"]))

(hmi/start-server
 3003
 :route-handler (hmi/hanami-handler
                 (hmi/hanami-routes :index-path "public/Fig/index.html"))
 :idfn (constantly "Exploring")
 :connfn connfn)
#_(hmi/stop-server)


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
 :TID :expl1, :TLBL #(-> :TID % name str/capitalize)
 :OPTS (hc/default-opts :vgl), :TOPTS (hc/default-opts :tab))

((hc/xform (hc/get-default :USERDATA)
           :LEFT `[[gap "10px"]] :FID "frame-test"
           :TID :geo :VID :v1) :tab)



;;; Simple scatter with template
(->> (hc/xform ht/point-chart
       ;;:DATA (->> "http://localhost:3003/data/cars.json" slurp json/read-str)
       :UDATA "data/cars.json"
       :VID :vscat1 :FID :fscat1
       :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
     hmi/sv!)

;;; Same as above but 'template' == base vega-lite spec
(->>
 (hc/xform
  {:usermeta :USERDATA
   :data {:url "data/cars.json"},
   :mark "point",
   :encoding {:x {:field "Horsepower", :type "quantitative"},
              :y {:field "Miles_per_Gallon", :type "quantitative"},
              :color {:field "Origin", :type "nominal"}}})
 hmi/sv!)


;;; With picture framing
(let [text "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsum, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."
      top `[[gap :size "150px"]
            [p "An example showing a "
             [:span.bold "picture "] [:span.italic.bold "frame"]
             ". This is the top 'board'"
             [:br] ~text]]
      left `[[gap :size "10px"]
             [p {:style {:width "100px" :min-width "50px"}}
              "Some text on the " [:span.bold "left:"] [:br] ~text]]
      right `[[gap :size "2px"]
              [p {:style {:width "200px" :min-width "50px"
                          :font-size "20px" :color "red"}}
               "Some large text on the " [:span.bold "right:"] [:br]
               ~(.substring text 0 180)]]
      bottom `[[gap :size "200px"]
               [title :level :level3
                :label [p {:style {:font-size "large"}}
                        "Some text on the "
                        [:span.bold "bottom"] [:br]
                        "With a cool info button "
                        [info-button
                         :position :right-center
                         :info
                         [:p "Check out Saite Visualizer!" [:br]
                          "Built with Hanami!" [:br]
                          [hyperlink-href
                           :label "Saite "
                           :href  "https://github.com/jsa-aerial/saite"
                           :target "_blank"]]]]]]]
  (->> [(hc/xform ht/point-chart
          :TID :picframes :VID :vscat2
          :TOP top :BOTTOM bottom :LEFT left :RIGHT right
          :UDATA "data/cars.json"
          :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")]
       hmi/sv!))


(let [text "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsu
m, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."
      top `[[gap :size "50px"]
            [p {:style {:width "600px" :min-width "50px"}}
             "An example empty picture frame showing all four areas."
             " This is the " [:span.bold "top"] " area. "
             ~text ~text ~text]]
      left `[[gap :size "50px"]
             [p {:style {:width "300px" :min-width "50px"}}
              "The " [:span.bold "left "] "area as a column of text. "
              ~text ~text ~text ~text]]
      right `[[gap :size "70px"]
              [p {:style {:width "300px" :min-width "50px"}}
               "The " [:span.bold "right "] "area as a column of text. "
               ~text ~text ~text ~text]]
      bottom `[[gap :size "50px"]
               [v-box
                :children
                [[p {:style {:width "600px" :min-width "50px"}}
                  "The " [:span.bold "bottom "]
                  "area showing a variety of text. "
                  [:span.italic ~text] [:span.bold ~text]]
                 [md {:style {:font-size "16px" :color "blue"}}
                  "#### Some Markup
* **Item 1** Lorem ipsum dolor sit amet, consectetur adipiscing elit.
* **Item 2** Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ips
um, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."]
                 [p {:style {:width "600px" :min-width "50px"
                             :color "red"}}
                  ~text]]]]]
  (->> (hc/xform
        ht/empty-chart
        :TID :picframes
        :TOP top :BOTTOM bottom :LEFT left :RIGHT right)
       hmi/sv!))



;;; With and without chart
(let [text "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsum, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."
      top `[[gap :size "50px"]
            [p "Here's a 'typical' chart/plot filled picture frame."
             "It only has the top area"
             [:br] ~text]]
      left `[[gap :size "20px"]
             [p {:style {:width "200px" :min-width "50px"}}
              "This is an empty frame with a " [:span.bold "left "]
              "column of text" [:br] ~text ~text ~text ~text]]
      right `[[gap :size "30px"]
              [p {:style {:width "200px" :min-width "50px"}}
               "And a " [:span.bold "right "]
               "column of text"
               [:br] ~text ~text ~text ~text]]]
  (->> [(hc/xform ht/point-chart
          :TID :picframes :UDATA "data/cars.json"
          :TOP top :FID :f1 :VID :vscat3
          :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
        (hc/xform ht/empty-chart
          :TID :picframes
          :LEFT left :RIGHT right :FID :f2)]
       hmi/sv!))



;;; Simple Barchart with instrumented template
(->>
 (let [data [{:a "A", :b 28 },
             {:a "B", :b 55 },
             {:a "C", :b 43 },
             {:a "D", :b 91 },
             {:a "E", :b 81 },
             {:a "F", :b 53 },
             {:a "G", :b 19 },
             {:a "H", :b 87 },
             {:a "I", :b 52 }]
       min -10.0
       minstr (-> min str (str/split #"\.") first)
       max 10.0
       maxstr (-> max str (str/split #"\.") first (#(str "+" %)))]
   (hc/xform ht/bar-chart
             :USERDATA
             (merge
              (hc/get-default :USERDATA)
              {:vid :bc1
               :frame {:bottom `[p "Move slider to change data stream"]}
               :slider `[[gap :size "10px"] [label :label "Add Bar"]
                         [label :label ~minstr]
                         [slider
                          :model :m1
                          :min ~min, :max ~max, :step 1.0
                          :width "200px"
                          :on-change :oc1]
                         [label :label ~maxstr]
                         [input-text
                          :model :m1
                          :width "60px", :height "26px"
                          :on-change :oc2]]})
             :HEIGHT 300, :WIDTH 350
             :X "a" :XTYPE "ordinal" :XTITLE "Foo" :Y "b" :YTITLE "Bar"
             :DATA data))
 hmi/sv!)

(hmi/sd! {:usermeta {:msgop :data :vid :bc1}
          :data (mapv #(assoc % :b (+ 50 (% :b)))
                      [{:a "A", :b 28 },{:a "B", :b 55 },{:a "C", :b 43 },
                       {:a "D", :b 91 },{:a "E", :b 81 },{:a "F", :b 53 },
                       {:a "G", :b 19 },{:a "H", :b 87 },{:a "I", :b 52 }])})



(->>
 (hc/xform
  {:usermeta :USERDATA
   :height 600,
   :width 600,
   :data
   {:values
    [{:dose 0.5, :response 32659}
     {:dose 0.5, :response 40659}
     {:dose 0.5, :response 29000}
     {:dose 1, :response 31781}
     {:dose 1, :response 30781}
     {:dose 1, :response 35781}
     {:dose 2, :response 30054}
     {:dose 4, :response 29398}
     {:dose 5, :response 27779}
     {:dose 10, :response 27915}
     {:dose 15, :response 27410}
     {:dose 20, :response 25819}
     {:dose 50, :response 23999}
     {:dose 50, :response 25999}
     {:dose 50, :response 20999}]},
   :layer
   [{:selection {:grid {:type "interval", :bind "scales"}},
     :mark {:type "point", :filled true, :color "black"},
     :encoding
     {:x {:field "dose", :type "quantitative", :scale {:type "log"}},
      :y {:field "response", :type "quantitative", :aggregate "mean"}}}
    {:mark {:type "errorbar", :ticks true},
       :encoding
       {:x {:field "dose", :type "quantitative", :scale {:zero false}},
        :y {:field "response", :type "quantitative"},
        :color {:value "#4682b4"}}}]})
 hmi/sv!)

(->>
 (hc/xform
  {:usermeta :USERDATA
   :height 600,
   :width 600,
   :data
   {:values
    [{:Dose 0.5, :Response 32659.00003,
      :drc_dose 0.05, :drc_ll3 35597.08053881955},
     {:Dose 0.5, :Response 40659.00002340234,
      :drc_dose 1, :drc_ll3 35597.08053881955},
     {:Dose 0.5, :Response 29000,
      :drc_dose 2, :drc_ll3 35597.08053881955},
     {:Dose 1, :Response 31781,
      :drc_dose 5, :drc_ll3 35597.08053881955},
     {:Dose 1, :Response 30781,
      :drc_dose 10, :drc_ll3 35597.08053881955},
     {:Dose 1, :Response 35781,
      :drc_dose 50, :drc_ll3 35597.08053881955},
     {:Dose 2, :Response 30054,
      :drc_dose 200, :drc_ll3 35597.08053881955},
     {:Dose 4, :Response 29398,
      :drc_dose 1000, :drc_ll3 35597.08053881955}]},
   :layer
   [{:selection {:grid {:type "interval", :bind "scales"}},
     :mark {:type "point", :filled true, :color "black"},
     :encoding
     {:x {:field "Dose", :type "quantitative", :scale {:type "log"}},
      :y {:field "Response", :type "quantitative", :aggregate "mean"}}}
    {:mark {:type "errorbar", :ticks true},
       :encoding
       {:x {:field "Dose", :type "quantitative", :scale {:zero false}},
        :y {:field "Response", :type "quantitative"},
        :color {:value "black"}}}
    {:mark {:type "line", :color "red"},
     :encoding
     {:x {:field "drc_dose", :type "quantitative"},
      :y {:field "drc_ll3", :type "quantitative"}}}]})
 hmi/sv!)



;;; Geo Example
(->>
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



hc/subkeyfns
(hc/update-subkeyfns :TOOLTIP hc/RMV)

(->
 (let [data (->> (range 0.005 0.999 0.001)
                 (mapv (fn[p] {:x p, :y (- (log2 p)) :col "SI"})))]
   (hc/xform ht/line-chart
             :TID :multi :TOPTS {:order :row, :size "auto"}
             :TITLE "Self Information (unexpectedness)"
             :XTITLE "Probability of event", :YTITLE "-log(p)"
             :DATA data))
 hmi/sv!)




;;; Multi Chart - cols and rows
;;;
(->>
 [(let [data (->> (range 0.005 0.999 0.001)
                  (mapv (fn[p] {:x p, :y (- (log2 p)) :col "SI"})))]
    ;; Self Info - unexpectedness
    (hc/xform ht/layer-chart
              :TID :multi :TOPTS {:order :row, :size "auto"}
              :TITLE "Self Information (unexpectedness)"
              :LAYER [(hc/xform ht/line-layer
                                :XTITLE "Probability of event"
                                :YTITLE "-log(p)")
                      (hc/xform ht/xrule-layer :AGG "mean")]
              :DATA data))
  ;; Entropy - unpredictability
  (let [data (->> (range 0.00005 0.9999 0.001)
                  (mapv (fn[p] {:x p,
                               :y (- (- (* p (log2 p)))
                                     (* (- 1 p) (log2 (- 1 p))))})))]
    (hc/xform ht/layer-chart
              :USERDATA (merge (hc/get-default :USERDATA)
                               {:test2 [{:id 1 :label "One" :val 1}
                                        {:id 2 :label "Two" :val 2}
                                        {:id 3 :label "Three" :val 3}]})
              :TID :multi
              :TITLE "Entropy (Unpredictability)"
              :LAYER [(hc/xform ht/gen-encode-layer
                                :MARK "line"
                                :XTITLE "Probability of event" :YTITLE "H(p)")
                      (hc/xform ht/xrule-layer :AGG "mean")]
              :DATA data))]
 hmi/sv!)



;;; Some distributions
;;;;
(def obsdist
  (let [obs [[0 9] [1 78] [2 305] [3 752] [4 1150] [5 1166]
             [6 899] [7 460] [8 644] [9 533] [10 504]]
        totcnt (->> obs (mapv second) (apply +))
        pdist (map (fn[[k cnt]] [k (double (/ cnt totcnt))]) obs)]
    pdist))
;;(p/mean obsdist) => 5.7
(->>
 [(hc/xform ht/layer-chart
            :TID :dists :FID :dex1
            :TITLE "A Real (obvserved) distribution with incorrect simple mean"
            :HEIGHT 400 :WIDTH 450
            :LAYER
            [(hc/xform ht/bar-layer :XTITLE "Count" :YTITLE "Probability")
             (hc/xform ht/xrule-layer :AGG "mean")]
            :DATA (mapv (fn[[x y]] {:x x :y y :m 5.7}) obsdist))

  (hc/xform ht/layer-chart
            :TID :dists :FID :dex2
            :TITLE "The same distribution with correct weighted mean"
            :HEIGHT 400 :WIDTH 450
            :LAYER
            [(hc/xform ht/bar-layer :XTITLE "Count" :YTITLE "Probability")
             (hc/xform ht/xrule-layer :X "m")]
            :DATA (mapv (fn[[x y]] {:x x :y y :m 5.7}) obsdist))]
 hmi/sv!)



;;; Contour maps (a Vega template!)
;;;
(->>
 (hc/xform
  ht/contour-plot
  :OPTS (merge (hc/default-opts :vgl) {:mode "vega"})
  :HEIGHT 400, :WIDTH 500
  :X "Horsepower", :XTITLE "Engine Horsepower"
  :Y "Miles_per_Gallon" :YTITLE "Miles/Gallon"
  :UDATA "data/cars.json"
  :XFORM-EXPR #(let [d1 (% :X)
                     d2 (% :Y)]
                 (format "datum['%s'] != null && datum['%s'] !=null" d1 d2)))
 hmi/sv!)

(->>
 (hc/xform
  ht/contour-plot
  :HEIGHT 500, :WIDTH 600
  :OPTS (merge (hc/default-opts :vgl) {:mode "vega"})
  :DATA (take 400 (repeatedly #(do {:x (rand-int 300) :y (rand-int 50)})))
  :XFORM-EXPR #(let [d1 (% :X)
                     d2 (% :Y)]
                 (format "datum['%s'] != null && datum['%s'] !=null" d1 d2)))
 hmi/sv!)




;;; =====================================================================;;;
