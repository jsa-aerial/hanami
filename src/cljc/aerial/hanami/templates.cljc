(ns aerial.hanami.templates
  )



(def xrule-layer
  {:mark "rule"
   :encoding {:x {:field :X
                  :type :RTYPE
                  :aggregate :AGG}
              :size {:value 2}
              :color {:value "red"}}})

(def bar-layer
  {:mark "bar"
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}
              }})

(def line-layer
  {:mark {:type "line", :point :POINT}
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}
              }})

(def point-layer
  {:mark "circle"
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}
              }})

(def gen-layer
  {:mark :MARK
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}
              }})




(def simple-bar-chart
  {:title {:text :TITLE
           #_:anchor #_"start"}
   :background :BACKGROUND
   :height :HEIGHT
   :width :WIDTH
   :data {:values :DATA},
   :mark "bar",
   :encoding {:x {:field :X, :type :XTYPE :axis {:title :XTITLE}},
              :y {:field :Y, :type :YTYPE :axis {:title :YTITLE}}
              :tooltip {:field "tt" :type "nominal"}}})


(def simple-line-chart
  {:title  {:text :TITLE}
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :mark "line"
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}
              }
   :data {:values :DATA}})


(def simple-layer-chart
  {:title  {:text :TITLE}
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :layer :LAYER
   :data {:values :DATA}
   :config {:bar {:binSpacing 0
                  :discreteBandSize 50}
            #_:view #_{:stroke "transparent"},
            #_:axis #_{:domainWidth 1}}})


(def grouped-sq-cnt-chart
  {:title  {:text :TITLE :offset 40}
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND

   :data {:values :DATA}

   :selection {:grid
               {:type "interval",
                :bind "scales", ; <-- This gives zoom and pan
                :on "[mousedown, window:mouseup] > window:mousemove!",
                :encodings ["x" "y"],
                :zoom "wheel!",
                :resolve "global"}}
   :mark "bar"
   :encoding {:x {:field :X
                  :type :XTYPE
                  ;;:scale {:rangeStep 1}
                  :axis {:title :XTITLE}}
              :y {:field :Y
                  :type :YTYPE
                  :axis {:title :YTITLE}}
              :column {:field :COLUMN :type :COLTYPE}
              :color {:field :X :type "nominal"
                      :scale {:scheme {:name "greenblue" #_"category20c"
                                         :extent [0.4 1]}}
                      }
              :tooltip {:field "tt" :type "nominal"}
              }

   :config {:bar {:binSpacing 0
                  :discreteBandSize 1
                  :continuousBandSize 1}
            :view {:stroke "transparent"},
            :axis {:domainWidth 1}}})




(def stacked-sq-cnt-chart
  {:background :BACKGROUND

   :data {:values :DATA}

   :title  {:text :TEXT :offset 5}

   :vconcat
   [{:transform [{:filter {:field "nm" :equal :NM1}}]
     :mark "bar"
     :height 500
     :width  1500
     :encoding {:x {:field :X
                    :type "nominal"
                    :axis {:title :XTITLE}
                    ;;:sort "none"
                    }
                :y {:field :Y
                    :type "quantitative"
                    :axis {:title :YTITLE}}
                :tooltip {:field "tt" :type "nominal"}
                :color {:field "nm" :type "nominal"
                        :scale {:range ["steelblue" "indianred"]}
                        :legend {:type "symbol"
                                 :offset 5
                                 :title "NM"}}}}
    {:transform [{:filter {:field "nm" :equal :NM2}}]
     :mark "bar"
     :height 500
     :width  1500
     :encoding {:x {:field :X
                    :type "nominal"
                    :axis {:title :XTITLE}
                    ;;:sort "none"
                    }
                :y {:field :Y
                    :type "quantitative"
                    :axis {:title :YTITLE}}
                :tooltip {:field "tt" :type "nominal"}
                :color {:field "nm" :type "nominal"
                        :legend {:type "symbol"
                                 :offset 5
                                 :title "NM"}}}}
    {:transform [{:filter {:field "nm" :equal :NM3}}]
     :mark "bar"
     :height 500
     :width  1500
     :encoding {:x {:field :X
                    :type "nominal"
                    :axis {:title :XTITLE}
                    ;;:sort "none"
                    }
                :y {:field :Y
                    :type "quantitative"
                    :axis {:title :YTITLE}}
                :tooltip {:field "tt" :type "nominal"}
                :color {:field "nm" :type "nominal"
                        :legend {:type "symbol"
                                 :offset 5
                                 :title "NM"}}}}]
   })




(def ident-num-by-lens
  {:title  {:text :TITLE}
   :height :HEIGHT
   :width  :WIDTH
   :background :BACKGROUND
   :layer [{;:transform [{:filter {:field "sz" :range [1,21#_70]}}]
            :selection {:grid
                        {:type "interval",
                         :bind "scales", ; <-- This gives zoom and pan
                         :zoom "wheel!",
                         :resolve "global"}}
            :mark "bar"
            :encoding {:x {:field :X
                           :axis {:title "Seq Length"}
                           :type :XTYPE}
                       :y {:field :Y
                           :axis {:title "Number"}
                           :type :YTYPE}
                       :tooltip {:field "tt" :type "nominal"}
                       }}
           {:mark "rule"
            :encoding {:x {:field :X
                           :type "quantitative"
                           :aggregate "median"}
                       :size {:value 2}
                       :color {:value "red"}}}]

   :data {:values :DATA}

   :config {:bar {:binSpacing 0
                  :discreteBandSize 10}
            :view {:stroke "transparent"},
            :axis {:domainWidth 1}
            :scale {;;:textXRangeStep 10
                    }}})





