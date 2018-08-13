(ns aerial.hanami.templates
  )



(def default-tooltip
  [{:field :X :type :XTYPE}
   {:field :Y :type :YTYPE}])

(def interval-scales
  {:INAME
   {:type "interval",
    :bind "scales", ; <-- This gives zoom and pan
    :on "[mousedown, window:mouseup] > window:mousemove!",
    :encodings :ENCODINGS,
    :zoom "wheel!",
    :resolve :IRESOLVE}})

(def interval-mark-mdwn
  {:MDWM-NAME
   {:type "interval"
    :on "[mousedown[!event.shiftKey], mouseup] > mousemove"
    :translate "[mousedown[!event.shiftKey], mouseup] > mousemove"
    :mark :MDWN-MARK}})

(def interval-mark-smdwn
  {:SMDWM-NAME
   {:type "interval"
    :on "[mousedown[event.shiftKey], mouseup] > mousemove"
    :translate "[mousedown[event.shiftKey], mouseup] > mousemove"
    :mark :SMDWN-MARK}})




(def xrule-layer
  {:mark "rule"
   :encoding {:x {:field :X
                  :type :XTYPE
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
              :tooltip :TOOLTIP
              }})

(def line-layer
  {:mark {:type "line", :point :POINT}
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}}})

(def point-layer
  {:mark "circle"
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}
              :size :SIZE
              :tooltip :TOOLTIP}})

(def gen-encode-layer
  {:transform :TRANSFORM
   :selection :SELECTION
   :mark :MARK
   :encoding {:x {:field :X
                  :type :XTYPE
                  :axis {:title :XTITLE, :grid :XGRID}
                  :scale :XSCALE}
              :y {:field :Y
                  :type :YTYPE
                  :axis {:title :YTITLE, :grid :YGRID}
                  :scale :YSCALE}
              :size :SIZE
              :tooltip :TOOLTIP
              :color :COLOR}})




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
              :tooltip :TOOLTIP}})


(def simple-line-chart
  {:title  {:text :TITLE :offset :TOFFSET}
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :mark {:type "line", :point :POINT}
   :encoding {:x {:field :X
                  :axis {:title :XTITLE}
                  :type :XTYPE}
              :y {:field :Y
                  :axis {:title :YTITLE}
                  :type :YTYPE}
              :tooltip :TOOLTIP}
   :data {:values :DATA}})


(def simple-layer-chart
  {:title  {:text :TITLE :offset :TOFFSET}
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :layer :LAYER
   :resolve :RESOLVE
   :data {:values :DATA}
   :config {:bar {:binSpacing 1
                  :discreteBandSize 5
                  :continuousBandSize 5}
            #_:view #_{:stroke "transparent"},
            #_:axis #_{:domainWidth 1}}})


(def row-grouped-bar-chart
  {:title  {:text :TITLE :offset :TOFFSET}
     :height :HEIGHT
     :width  :WIDTH
     :background :BACKGROUND
     :data {:values :DATA}

     :mark "bar"
     :encoding {:x {:field :X, :type :XTYPE
                    :axis {:title :XTITLE}}
                :y {:field :Y, :type :YTYPE
                    :axis {:title :YTITLE}}
                :row {:field :ROW :type :ROWTYPE}
                :color {:field :ROW :type :ROWTYPE
                        :scale {:scheme {:name "greenblue" #_"category20c"
                                         :extent [0.4 1]}}}
                :tooltip :TOOLTIP}

     :config {:bar {:binSpacing 0
                    :discreteBandSize 1
                    :continuousBandSize 1}
              :view {:stroke "transparent"},
              :axis {:domainWidth 1}}})


(def col-grouped-bar-chart
  {:title  {:text :TITLE :offset :TOFFSET}
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND

   :data {:values :DATA}

   :selection {:grid
               {:type "interval",
                :bind "scales", ; <-- This gives zoom and pan
                :on "[mousedown, window:mouseup] > window:mousemove!",
                :encodings ["y"], ;  <-- x is typically nominal
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
              :color {:field :X :type :XTYPE
                      :scale {:scheme {:name "greenblue"
                                       :extent [0.4 1]}}}
              :tooltip :TOOLTIP}

   :config {:bar {:binSpacing 0
                  :discreteBandSize 1
                  :continuousBandSize 1}
            :view {:stroke "transparent"},
            :axis {:domainWidth 1}}})

