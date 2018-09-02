(ns aerial.hanami.templates
  )



(def default-tooltip
  [{:field :X :type :XTYPE}
   {:field :Y :type :YTYPE}])

(def default-mark-props
  {:field :MPFIELD :type :MPTYPE})

(def default-color
  {:field :CFIELD :type :CTYPE :scale :CSCALE})

(def default-title
  {:text :TTEXT :offset :TOFFSET})

(def default-row
  {:field :ROW :type :ROWTYPE})

(def default-col
  {:field :COLUMN :type :COLTYPE})

(def data-options
  {:values :DATA :url :UDATA, :name :NDATA})


(def interval-scales
  {:INAME
   {:type "interval",
    :bind "scales", ; <-- This gives zoom and pan
    ;:on "[mousedown, window:mouseup] > window:mousemove!",
    ;:on "[mousedown[event.shiftKey], mouseup] > mousemove!",
    :translate
    "[mousedown[event.shiftKey], window:mouseup] > window:mousemove!"
    :encodings :ENCODINGS,
    :zoom "wheel!",
    :resolve :IRESOLVE}})

(def interval-brush-mdwn
  {:MDWM-NAME
   {:type "interval"
    :on "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove!"
    :translate "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove"
    :resolve :IRESOLVE
    :mark :MDWN-MARK}})

(def interval-brush-smdwn
  {:SMDWM-NAME
   {:type "interval"
    :on "[mousedown[event.shiftKey], mouseup] > mousemove"
    :translate "[mousedown[event.shiftKey], mouseup] > mousemove"
    :resolve :IRESOLVE
    :mark :SMDWN-MARK}})


(def xy-encoding
  {:x {:field :X
       :type :XTYPE
       :axis {:title :XTITLE, :grid :XGRID}
       :scale :XSCALE}
   :y {:field :Y
       :type :YTYPE
       :axis {:title :YTITLE, :grid :YGRID}
       :scale :YSCALE}
   :row :ROWDEF
   :column :COLDEF
   :color :COLOR
   :size :SIZE
   :shape :SHAPE
   :tooltip :TOOLTIP})




(def xrule-layer
  {:mark "rule"
   :encoding {:x {:field :X
                  :type :XTYPE
                  :aggregate :AGG}
              :size {:value 2}
              :color {:value :XRL-COLOR}}})

(def yrule-layer
  {:mark "rule"
   :encoding {:y {:field :Y
                  :type :YTYPE
                  :aggregate :AGG}
              :size {:value 2}
              :color {:value :YRL-COLOR}}})


(def bar-layer
  {:mark "bar"
   :encoding :ENCODING})

(def line-layer
  {:mark {:type "line", :point :POINT}
   :encoding :ENCODING})

(def point-layer
  {:mark "circle"
   :encoding :ENCODING})

(def gen-encode-layer
  {:transform :TRANSFORM
   :selection :SELECTION
   :mark :MARK
   :encoding :ENCODING})




(def simple-bar-chart
  {:usermeta :USERDATA
   :title :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :selection :SELECTION
   :data data-options
   :transform :TRANSFORM
   :mark "bar",
   :encoding :ENCODING})


(def simple-line-chart
  {:usermeta :USERDATA
   :title  :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :selection :SELECTION
   :data data-options
   :transform :TRANSFORM
   :mark {:type "line", :point :POINT}
   :encoding :ENCODING})

(def simple-point-chart
  {:usermeta :USERDATA
   :title  :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :selection :SELECTION
   :data data-options
   :transform :TRANSFORM
   :mark {:type "circle", :size :MSIZE}
   :encoding :ENCODING})


(def simple-layer-chart
  {:usermeta :USERDATA
   :title  :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :layer :LAYER
   :resolve :RESOLVE
   :data data-options
   :config {:bar {:binSpacing 1
                  :discreteBandSize 5
                  :continuousBandSize 5}
            #_:view #_{:stroke "transparent"},
            #_:axis #_{:domainWidth 1}}})


(def hconcat-chart
  {:usermeta :USERDATA
   :title  :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :hconcat :HCONCAT
   :resolve :RESOLVE
   :data data-options
   :config {:bar {:binSpacing 1
                  :discreteBandSize 5
                  :continuousBandSize 5}
            #_:view #_{:stroke "transparent"},
            #_:axis #_{:domainWidth 1}}})

(def vconcat-chart
  {:usermeta :USERDATA
   :title  :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :vconcat :VCONCAT
   :resolve :RESOLVE
   :data data-options
   :config {:bar {:binSpacing 1
                  :discreteBandSize 5
                  :continuousBandSize 5}}})


(def grouped-bar-chart
  {:usermeta :USERDATA
   :title  :TITLE
   :height :HEIGHT
   :width  :WIDTH
   :background :BACKGROUND
   :selection :SELECTION
   :data data-options

   :mark "bar"
   :encoding :ENCODING

   :config {:bar {:binSpacing 0
                  :discreteBandSize 1
                  :continuousBandSize 1}
            :view {:stroke "transparent"},
            :axis {:domainWidth 1}}})



