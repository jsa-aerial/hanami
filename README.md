# hanami

Interactive arts and charts visualizations with Clojure(Script), Vega-lite, and Vega. Flower viewing 花見 (hanami)

<a href="https://hanami.github.io"><img src="https://github.com/jsa-aerial/hanami/blob/master/resources/public/Himeji_sakura.jpg" align="left" hspace="10" vspace="6" alt="hanami logo" width="150px"></a>


**Hanami** is a Clojure(Script) library and application for creating interactive visualizations based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer).

In keeping with this central data oriented tenet, Hanami eschews the typical API approach for generating specifications in favor of using recursive transforms of parameterized templates. This is also in keeping with the data transformation focus in functional programming, which is espcially nice in Clojure(Script).

An important aspect of this approach is that parameterized templates can be used to build other such templates by being higher level substitutions. In addition templates can be composed and this is another important idiomatic use. Additionally templates may be merged, though typically this is after transformation. The result enables the construction of sharable libraries of templates providing reusable plots, charts, and entire visualizations. Generally these will be domain and/or task specific. Hanami itself provides only a small set of very generic templates, which have proven useful in constructing more domain/task specific end results.


## Installation

To install, add the following to your project `:dependencies`:

    [aerial.hanami "0.2.0"]

**NOTE** lib bundling/packaging is currently in flux and artifact not yet available...


## Features

* Parameterized templates with recursive transformations
  * Takes the place of the typical static procedural/functional API
  * Purely data driven - no objects, classes, inheritance, whatever
  * Completely open ended - users may define their own with their own defaults
  * More general in scope than an API while capable of arbitrary specific detail
* Named visulization groupings (tabbed navigation)
* Multiple simultaneous independent and dependent visulizations per grouping
* Default application for exploratory work
* Enables specific application construction as lib
  * Application level page header instrumentation (re-com enabled)
  * Application level external instrumentation of charts (re-com enabled)
  * Multiple simultaneous (named) applications
  * Multiple sessions per application
  * Data streaming capable - user extensible messages
* Uses light weight websocket messaging system

## Examples

```Clojure
(ns hanami.examples
  (:require [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [aerial.hanami.core :as hmi]
            ...)
```

```Clojure
(hc/xform ht/simple-point-chart
  :UDATA "data/cars.json"
  :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
```

Transforms to:

```Clojure
{:data {:url "data/cars.json"},
 :width 400,
 :height 300,
 :background "floralwhite",
 :encoding
   {:x {:field "Horsepower", :type "quantitative"},
    :y {:field "Miles_per_Gallon", :type "quantitative"},
    :color {:field "Origin", :type "nominal"},
    :tooltip
    [{:field "Horsepower", :type "quantitative"}
     {:field "Miles_per_Gallon", :type "quantitative"}]}}
 ```

And when sent to a view, results in, where the mouse is hovering over the point given by [132, 32.7]:

![Hanami pic 1](resources/public/images/hanami-cars-1.png?raw=true)


An example of an instrumented chart:

```Clojure
(hc/xform ht/simple-bar-chart
  :USERDATA
  {:test1 '[[gap :size "10px"] [label :label "Slider away"]
            [slider
             :model sval
             :min -10.0, :max 10.0, :step 1.0
             :width "200px"
             :on-change bar-slider-fn]
            [input-text
             :model sval
             :width "60px", :height "26px"
             :on-change bar-slider-fn]]}
  :TITLE "A Simple Bar Chart"
  :HEIGHT 300, :WIDTH 350
  :X "a" :XTYPE "ordinal" :XTITLE "foo" :Y "b" :YTITLE "bar"
  :DATA data)
```

Renders as (before slider move left; after slider move right)

![Hanami pic 2](resources/public/images/instrumented-chart-1a.png?raw=true)
![Hanami pic 3](resources/public/images/instrumented-chart-1b.png?raw=true)


A number of other examples appear at the end of this README, along with their transformations and renderings.



## Templates

_Templates_ are simply maps parameterized by _substitution keys_. Generally, templates will typically correspond to a legal VG or VGL specification or a legal subcomponent thereof. For example, a complete VGL specification (rendered as Clojure) is a legal template - even though it has no substitution keys. At the other extreme, temmplates can correspond to pieces of specifications or subcomponents. These will always have substitution keys - if they didn't there would be no point to them. Here are some examples as provided by the name space `aerial.hanami.templates`.

A couple of 'fragments':

```Clojure
(def default-mark-props
  {:field :MPFIELD :type :MPTYPE})

(def interval-scales
  {:INAME
   {:type "interval",
    :bind "scales", ; <-- This gives zoom and pan
    :translate
    "[mousedown[event.shiftKey], window:mouseup] > window:mousemove!"
    :encodings :ENCODINGS,
    :zoom "wheel!",
    :resolve :IRESOLVE}})
```

A few 'subcomponents':

```Clojure
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

(def gen-encode-layer
  {:transform :TRANSFORM
   :selection :SELECTION
   :mark :MARK
   :encoding :ENCODING})

(def simple-layer-chart
  {:usermeta :USERDATA
   :title  :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :layer :LAYER
   :resolve :RESOLVE
   :data data-options})
```

And a full chart. This one does faceted composing with optional interactivity. Most of the capability comes from `:ENCODING` and its default.

```Clojure
(def row-grouped-bar-chart
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
```


### Walk through example of transformation

It's worth having a preliminary look at what happens with this simple chart and its transformations. The value of `ht/simple-point-chart` is:

```Clojure
{:usermeta :USERDATA
 :title  :TITLE
 :height :HEIGHT
 :width :WIDTH
 :background :BACKGROUND
 :selection :SELECTION
 :data :DATA-OPTIONS
 :transform :TRANSFORM
 :mark {:type "circle", :size :MSIZE}
 :encoding :ENCODING}
```
In the above transform we specified values for `:UDATA`, `:X`, `:Y`, and `:COLOR`. First, none of these are anywhere to be seen in `ht/simple-point-chart` so where do they come from? Second, what happened to all those other fields like `:usermeta` and those values like `:SELECTION`. Both questions have answers rooted in the map of default _substitution keys_ and values for transformations. There is nothing special about these defaults and a user can completely change them if they do not like the key names or their values. However, out of the box, Hanami provides a starting set and  here is a subset of those substitutions that answer our two questions and also where some other values come from:

```Clojure
  :BACKGROUND "floralwhite"
  :TITLE RMV
  :XTITLE RMV, :XGRID RMV, :XSCALE RMV
  :YTITLE RMV, :YGRID RMV, :YSCALE RMV
  :HEIGHT 300
  :WIDTH 400
  :USERDATA RMV
  :DATA-OPTIONS ht/data-options
  :DATA RMV, :UDATA RMV, :NDATA RMV
  :TRANSFORM RMV
  :MSIZE RMV
  :ENCODING ht/xy-encoding
  :SIZE RMV, :SHAPE RMV
  :TOOLTIP ht/default-tooltip
```
Defaults for substitution keys are always overridden by values given for them in a call to `xform`. Any RMV value indicates removal - the key in a template associated with a substitution key whose value is RMV is removed from the template.

Further, we have these in the `ht` namespace, where our chart template is also defined:

```Clojure
(def default-tooltip
  [{:field :X :type :XTYPE}
   {:field :Y :type :YTYPE}])

(def default-mark-props
  {:field :MPFIELD :type :MPTYPE})

(def xy-encoding
  {:x {:field :X
       :type :XTYPE
       :axis {:title :XTITLE, :grid :XGRID}
       :scale :XSCALE}
   :y {:field :Y
       :type :YTYPE
       :axis {:title :YTITLE, :grid :YGRID}
       :scale :YSCALE}
   :color :COLOR
   :size :SIZE
   :shape :SHAPE
   :tooltip :TOOLTIP})
````




## API

As noted, there isn't much of a functional/procedural API and no objects or protocols (classes/interfaces) are involved. There are three primary functions. One on the server side, one on the browser/client side and one common to both. There are handful of other ancillary functions common to both sides involving the abiltiy to change default substitution map.

### Primary

#### Common

In name space `aerial.hanami.common`

```Clojure
(xform
  ([x xkv] ...)
  ([x k v & kvs] ...))
```

This is the recursive transformation function. `x` is a _template_ (see above). In both the 2 and 3+ argument cases, the remaining arguments involve providing _substitution keys_ and values. In the two argument case these are supplied as a `map`, while the 3+ argument case you provide them in the usual key/value pair `rest` style of clojure. Each key should correspond to a _substitution key_ (see above in **Templates**) while the value will be what is inserted into a template during the transformation sequence.




## Example Transform 'Gallery'

Here is the same data (observed distribution vs binomial models) as row and column grouped (faceted) charts.

```Clojure
(hc/xform ht/grouped-bar-chart
  :TITLE "Real distribution vs Binomials", :TOFFSET 10
  :HEIGHT 80, :WIDTH 450
  :DATA ...
  :X "cnt" :XTYPE "ordinal" :XTITLE ""
  :Y "y" :YTITLE "Probability"
  :COLOR ht/default-color :CFIELD :ROW :CTYPE :ROWTYPE
  :ROW "dist" :ROWTYPE "nominal")

(hc/xform ht/grouped-bar-chart
  :TITLE "Real distribution vs Binomials", :TOFFSET 40
  :WIDTH (-> 550 (/ 6) double Math/round (- 15))
  :DATA ...
  :X "dist" :XTYPE "nominal" :XTITLE ""
  :Y "y" :YTITLE "Probability"
  :COLOR ht/default-color
  :COLUMN "cnt" :COLTYPE "ordinal")
```

Both of these transform into similar amounts of VGL output, but the first is somewhat more interesting. Note the `:color` mark propery value and the input  for it in the above code.

```Clojure
{:title {:text "Real distribution vs Binomials", :offset 10},
 :height 80,
 :width 450,
 :background "floralwhite",
 :mark "bar",
 :encoding
 {:x {:field "cnt", :type "ordinal", :axis {:title ""}},
  :y {:field "y", :type "quantitative", :axis {:title "Probability"}},
  :row {:field "dist", :type "nominal"},
  :color
  {:field "dist",
   :type "nominal",
   :scale {:scheme {:name "greenblue", :extent [0.4 1]}}},
  :tooltip
  [{:field "cnt", :type "ordinal"}
   {:field "y", :type "quantitative"}]},
  :view {:stroke "transparent"},
  :axis {:domainWidth 1}},
 :data {:values ...}
 :config
 {:bar {:binSpacing 0, :discreteBandSize 1, :continuousBandSize 1}}
```

And the rendered visualizations are:

![Hanami pic 4](resources/public/images/real-vs-binomial-col.png?raw=true)

![Hanami pic 5](resources/public/images/real-vs-binomial-row.png?raw=true)


This is a nice example of how one visualization (the row grouping) can bring out the salient information so much better than another (the col grouping)


The next is a visualization for an investigation into using lowess smoothing of TNSeq fitness data.

```Clojure
(hc/xform ht/simple-layer-chart
  :TITLE "Raw vs 1-4 lowess smoothing" :TOFFSET 5
  :HEIGHT 500 :WIDTH 700
  :DATA (concat base-xy lowess-1 lowess-2 lowess-3 lowess-4)
  :LAYER (mapv #(hc/xform ht/gen-encode-layer
                  :MARK (if (= "NoL" %) "circle" "line")
                  :TRANSFORM [{:filter {:field "NM" :equal %}}]
                  :COLOR "NM"
                  :XTITLE "Position", :YTITLE "Count")
               ["NoL" "L1" "L2" "L3" "L4"]))
```

This one is interesting in that it combines some nice straight ahead Clojure data mapping with the template system. Here, we create five layers - but they are all different data sets and so VGL's `repeat` would not apply. But, Clojure's `mapv` combined with Hanami's `xform` does the trick. Of course, any number of layers could be so constructed.


```Clojure
{:title {:text "Raw vs 1-4 lowess smoothing", :offset 5},
 :height 500,
 :width 700,
 :background "floralwhite",
 :layer
 [{:transform [{:filter {:field "NM", :equal "NoL"}}],
   :mark "circle",
   :encoding
   {:x {:field "x", :type "quantitative", :axis {:title "Position"}},
    :y {:field "y", :type "quantitative", :axis {:title "Count"}},
    :color {:field "NM", :type "nominal"},
    :tooltip
    [{:field "x", :type "quantitative"}
     {:field "y", :type "quantitative"}]}}
  {:transform [{:filter {:field "NM", :equal "L1"}}],
   :mark "line",
   :encoding
   {:x {:field "x", :type "quantitative", :axis {:title "Position"}},
    :y {:field "y", :type "quantitative", :axis {:title "Count"}},
    :color {:field "NM", :type "nominal"},
    :tooltip
    [{:field "x", :type "quantitative"}
     {:field "y", :type "quantitative"}]}}
  {:transform [{:filter {:field "NM", :equal "L2"}}],
   :mark "line",
   :encoding
   {:x {:field "x", :type "quantitative", :axis {:title "Position"}},
    :y {:field "y", :type "quantitative", :axis {:title "Count"}},
    :color {:field "NM", :type "nominal"},
    :tooltip
    [{:field "x", :type "quantitative"}
     {:field "y", :type "quantitative"}]}}
  {:transform [{:filter {:field "NM", :equal "L3"}}],
   :mark "line",
   :encoding
   {:x {:field "x", :type "quantitative", :axis {:title "Position"}},
    :y {:field "y", :type "quantitative", :axis {:title "Count"}},
    :color {:field "NM", :type "nominal"},
    :tooltip
    [{:field "x", :type "quantitative"}
     {:field "y", :type "quantitative"}]}}
  {:transform [{:filter {:field "NM", :equal "L4"}}],
   :mark "line",
   :encoding
   {:x {:field "x", :type "quantitative", :axis {:title "Position"}},
    :y {:field "y", :type "quantitative", :axis {:title "Count"}},
    :color {:field "NM", :type "nominal"},
    :tooltip
    [{:field "x", :type "quantitative"}
     {:field "y", :type "quantitative"}]}}],
 :data {:values ...}
 :config
 {:bar {:binSpacing 1, :discreteBandSize 5, :continuousBandSize 5}}}
 ```

![Hanami pic 6](resources/public/images/lowess-tnseq-smoothing.png?raw=true)



And lastly a quite involved example from a real application for RNASeq Differential Gene Expression:

```Clojure
(let [data (->> DGE-data (filter #(-> "padj" % (<= 0.05))))
       mdwn-brush (hc/xform ht/interval-brush-mdwn :MDWM-NAME "brush" :IRESOLVE "global")
       color {:field "NM" :type "nominal" :scale {:range ["#e45756" "#54a24b" "#4c78a8"]}}
       size {:condition {:selection {:not "brush"} :value 40} :value 400}
       tooltip `[{:field "Gene", :type "nominal"} ~@ht/default-tooltip {:field "pvalue", :type "quantitative"}]]
   (hc/xform
    ht/hconcat-chart
    :TITLE
    "RNASeq Exp 180109_NS500751_0066 DGE for aT4-024V10min vs aT4-024V30min"
    :TOFFSET 40
    :DATA :data
    :HCONCAT[(hc/xform
              ht/simple-point-chart
              :TITLE "MA Plot"
              :MSIZE 40
              :SELECTION (merge  (hc/xform ht/interval-scales :INAME "grid1")
                                 mdwn-brush)
              :X "baseMean", :Y "log2FoldChange"
              :COLOR color, :SIZE size, :TOOLTIP tooltip)
             (hc/xform
              ht/simple-point-chart
              :TITLE "Volcano Plot"
              :MSIZE 40
              :SELECTION (merge (hc/xform ht/interval-scales :INAME "grid2")
                                mdwn-brush)
              :X "log2FoldChange", :Y "-log10(pval)"
              :COLOR color, :SIZE size :TOOLTIP tooltip)]))
```

Transforms to:

```Clojure
{:hconcat
 [{:encoding
   {:x {:field "baseMean", :type "quantitative"},
    :y {:field "log2FoldChange", :type "quantitative"},
    :color
    {:field "NM",
     :type "nominal",
     :scale {:range ["#e45756" "#54a24b" "#4c78a8"]}},
    :size
    {:condition {:selection {:not "brush"}, :value 40}, :value 400},
    :tooltip
    [{:field "Gene", :type "Nominal"}
     {:field "baseMean", :type "quantitative"}
     {:field "log2FoldChange", :type "quantitative"}
     {:field "pvalue", :type "quantitative"}]},
   :mark {:type "circle", :size 40},
   :width 450,
   :background "floralwhite",
   :title {:text "MA Plot"},
   :selection
   {"grid1"
    {:type "interval",
     :bind "scales",
     :translate
     "[mousedown[event.shiftKey], window:mouseup] > window:mousemove!",
     :encodings ["x" "y"],
     :zoom "wheel!",
     :resolve "global"},
    "brush"
    {:type "interval",
     :on
     "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove!",
     :translate
     "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove",
     :resolve "global",
     :mark {:fill "#333", :fillOpacity 0.125, :stroke "white"}}},
   :height 400}
  {:encoding
   {:x {:field "log2FoldChange", :type "quantitative"},
    :y {:field "-log10(pval)", :type "quantitative"},
    :color
    {:field "NM",
     :type "nominal",
     :scale {:range ["#e45756" "#54a24b" "#4c78a8"]}},
    :size
    {:condition {:selection {:not "brush"}, :value 40}, :value 400},
    :tooltip
    [{:field "Gene", :type "Nominal"}
     {:field "log2FoldChange", :type "quantitative"}
     {:field "-log10(pval)", :type "quantitative"}
     {:field "pvalue", :type "quantitative"}]},
   :mark {:type "circle", :size 40},
   :width 450,
   :background "floralwhite",
   :title {:text "Volcano Plot"},
   :selection
   {"grid2"
    {:type "interval",
     :bind "scales",
     :translate
     "[mousedown[event.shiftKey], window:mouseup] > window:mousemove!",
     :encodings ["x" "y"],
     :zoom "wheel!",
     :resolve "global"},
    "brush"
    {:type "interval",
     :on
     "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove!",
     :translate
     "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove",
     :resolve "global",
     :mark {:fill "#333", :fillOpacity 0.125, :stroke "white"}}},
   :height 400}],
 :config
 {:bar {:binSpacing 1, :discreteBandSize 5, :continuousBandSize 5}},
 :width 450,
 :background "floralwhite",
 :title
 {:text
  "RNASeq Exp 180109_NS500751_0066 DGE for aT4-024V10min vs aT4-024V30min",
  :offset 40},
 :height 400,
 :data {:values [... ]}}
```

Well, now that is quite a lot. When sent to a view, visualizes as follows. Note that this is a fully interactive visualization where each grid can be independently zoomed and panned and brush stroke highlighting in one view hightlights the covered points in the other view. Here the mouse is hovering over the upper left point in the volcano plot.

![Hanami pic 6](resources/public/images/RNASeq-interactive-vis.png?raw=true)

