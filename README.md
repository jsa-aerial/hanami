# hanami

Interactive arts and charts visualizations with Clojure(Script), Vega-lite, and Vega. Flower viewing 花見 (hanami)

<a href="https://jsa-aerial.github.io/aerial.hanami/index.html"><img src="https://github.com/jsa-aerial/hanami/blob/master/resources/public/Himeji_sakura.jpg" align="left" hspace="10" vspace="6" alt="hanami logo" width="150px"></a>

**Hanami** is a Clojure(Script) library and framework for creating interactive visualization applications based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer). In addition to VGL and VG, Hanami is built on top of [Reagent](http://reagent-project.github.io/) and [Re-Com](https://github.com/Day8/re-com).


Table of Contents
=================

   * [Hanami](#hanami)
   * [Installation](#installation)
   * [Features](#features)
   * [Examples](#examples)
      * [Simple cars](#simple-cars)
      * [Instrumented barchart](#instrumented-barchart)
      * [Contour plot using Vega template](#contour-plot-using-vega-template)
      * [Tree Layout using Vega template](#tree-layout-using-vega-template)
   * [Templates, Substitution Keys and Transformations](#templates-substitution-keys-and-transformations)
      * [Function values for substitution keys](#function-values-for-substitution-keys)
      * [Subtitution Key Functions](#subtitution-key-functions)
      * [Basic transformation rules](#basic-transformation-rules)
      * [Meta data and the :USERDATA key](#meta-data-and-the-userdata-key)
      * [Example predefined templates](#example-predefined-templates)
      * [Example predefined substitution keys](#example-predefined-substitution-keys)
      * [Data Sources](#data-sources)
         * [File data source](#file-data-source)
      * [Walk through example of transformation](#walk-through-example-of-transformation)
   * [Application Construction](#application-construction)
      * [Header](#header)
      * [Tabs](#tabs)
      * [Sessions](#sessions)
      * [Messages](#messages)
         * [Registration](#registration)
         * [Tab updates](#tab-updates)
         * [User messages](#user-messages)
      * [Picture Frames](#picture-frames)
         * [Empty Frames](#empty-frames)
      * [Data Streaming](#data-streaming)
      * [API](#api)
         * [Templates and Substitution keys](#templates-and-substitution-keys)
         * [Message system](#message-system)
   * [Example Transform 'Gallery'](#example-transform-gallery)

[toc](https://github.com/ekalinin/github-markdown-toc)

# Hanami

**Hanami** is a Clojure(Script) library and framework for creating interactive visualization applications based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer). In addition to VGL and VG, Hanami is built on top of [Reagent](http://reagent-project.github.io/) and [Re-Com](https://github.com/Day8/re-com).

In keeping with the central data oriented tenet, Hanami eschews the typical API approach for generating specifications in favor of using recursive transforms of parameterized templates. This is also in keeping with the data transformation focus in functional programming, which is espcially nice in Clojure(Script).

An important aspect of this approach is that parameterized templates can be used to build other such templates by being higher level substitutions. In addition templates can be composed and this is an important idiomatic use. Furthermore, templates may be merged, though typically this is after transformation. The overall result enables the construction of sharable libraries of templates providing reusable plots, charts, and entire visualizations. Generally these will be domain and/or task specific. Hanami itself provides only a small set of very generic templates, which have proven useful in constructing more domain/task specific end results.

Hence, templates are a means to abstract all manner of visualization aspects and requirements. In this sense they are similar to what [Altair](https://altair-viz.github.io/) provides but without the complications and limitations of an OO class/method based approach.




# Installation

To install, add the following to your project `:dependencies`:

    [aerial.hanami "0.5.1"]




# Features

* Parameterized templates with recursive transformations
  * Takes the place of the typical static procedural/functional API
  * Purely data driven - no objects, classes, inheritance, whatever
  * Completely open ended - users may define their own with their own defaults
  * More general in scope than an API while capable of arbitrary specific detail
* A tabbing system for named visulization groupings
  * Multiple simultaneous independent and dependent visulizations per grouping
  * Automatic grid layout
  * Option system for customization
* Enables specific application construction
  * Application level page header instrumentation (re-com enabled)
  * Application level external instrumentation of charts (re-com enabled)
  * Full hiccup/re-com "picture framing" capability for independent charts
  * Multiple simultaneous (named) applications
  * Multiple sessions per application
    * Shared named sessions
  * Application extensible messaging capability
  * Data streaming capable
    * Realtime chart/plot updates with data updates
* Uses light weight websocket messaging system




# Examples

```Clojure
(ns hanami.examples
  (:require [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [aerial.hanami.core :as hmi]
            ...)
```

In all of the documentation, these namespaces are referred to by the shorthand provided in this require example.

* `aerial.hanami.common` == `hc`
* `aerial.hanami.templates` == `ht`
* `aerial.hanami.core` == `hmi`


## Simple cars

As a first example let's compare a Hanami template with corresponding Altair code. This is a the typical scatter plot example from the Vega-Lite developers used in many tutorials and by others in various places. First, the Altair:

```Python
cars = data.cars()

alt.Chart(cars).mark_point().encode(
    x='Horsepower',
    y='Miles_per_Gallon',
    color='Origin',
    ).interactive()
```

Hanami template version:

```Clojure
(hc/xform ht/point-chart
  :UDATA "data/cars.json"
  :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
```

Which, using the standard default [substitution keys](#example-predefined-substitution-keys) transforms to this Vega-Lite JSON specification:

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


When rendered, both the Altair code and Hanami template, result in the following visualization, where the mouse is hovering over the point given by [132, 32.7]:

![Hanami pic 1](resources/public/images/hanami-cars-1.png?raw=true)


## Instrumented barchart

Hanami visualizations may be instrumented with external active componets (react/reagent/re-com) to enable external transforms on them. An example of an instrumented chart:

```Clojure
(hc/xform ht/bar-chart
  :USERDATA
  (merge
   (hc/get-default :USERDATA)
   {:vid :bc1
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
  :DATA data)
```

Which requires the active component implementing the instrument to be coded over on the client side. For this simple example, the test code on the client is a branch inside of a `cond` testing for the `:slider` key and value and then processing it:

```Clojure
      (let [cljspec spec
            udata (cljspec :usermeta)
            default-frame {:top [], :bottom [],
                           :left [[box :size "0px" :child ""]],
                           :right [[box :size "0px" :child ""]]}]
        (cond
          ...
          (udata :slider)
          (let [sval (rgt/atom "0.0")]
            (printchan :SLIDER-INSTRUMENTOR)
            (merge default-frame
                   {:top (xform-recom
                          (udata :slider)
                          :m1 sval
                          :oc1 #(do (bar-slider-fn tabid %)
                                    (reset! sval (str %)))
                          :oc2 #(do (bar-slider-fn tabid (js/parseFloat %))
                                    (reset! sval %)))}))
```

When this chart is rendered, it shows as (left, before slider move; right, after slider move)

![Hanami pic 2](resources/public/images/instrumented-chart-1a.png?raw=true)
![Hanami pic 3](resources/public/images/instrumented-chart-1b.png?raw=true)


## Contour plot using Vega template

An example using a Vega template for contour plotting. An important point to recognize here is that Vega specifications are also pure data, so the exact same recursive transformation works on Vega templates as Vega Lite templates:

```Clojure
(hc/xform
  ht/contour-plot
  :MODE "vega"
  :HEIGHT 400, :WIDTH 500
  :X "Horsepower", :XTITLE "Engine Horsepower"
  :Y "Miles_per_Gallon" :YTITLE "Miles/Gallon"
  :UDATA "data/cars.json"
  :XFORM-EXPR #(let [d1 (% :X)
                     d2 (% :Y)]
                 (format "datum['%s'] != null && datum['%s'] !=null" d1 d2)))
```

This generates far too much to show here, as Vega is a much lower level formal specification language than Vega Lite. This renders as:

![Hanami pic 3.1](resources/public/images/contour-1.png?raw=true)


## Tree Layout using Vega template

Another interesting Vega example uses the `tree-layout` template. Using this template for such layouts abstracts away a good deal of low level complexity. In this example a software system module dependency graph is rendered.

```Clojure
(hc/xform
 ht/tree-layout
  :MODE "vega"
  :WIDTH 650, :HEIGHT 1600
  :UDATA "data/flare.json"
  :LINKSHAPE "line" :LAYOUT "cluster"
  :CFIELD "depth")
 ```

![Hanami pic 3.2](resources/public/images/tree-layout-1.png?raw=true)




A number of other examples appear at the end of this README, along with their transformations and renderings.




# Templates, Substitution Keys and Transformations

_Templates_ are simply maps parameterized by _substitution keys_. Generally, templates will typically correspond to a legal VG or VGL specification or a legal subcomponent thereof. For example, a complete VGL specification (rendered as Clojure) is a legal template - even though it has no substitution keys. At the other extreme, templates can correspond to pieces of specifications or subcomponents. These will always have substitution keys - if they didn't there would be no point to them.

_Substitution Keys_ can be considered or thought of in two ways. They are the keys in the substitution map(s) of the recursive transformer `hc/xform`. There is a default map `hc/_defaults` (which is an atom containing the map) with various default substitution keys and their corresponding default values. This map can be updated and `xform` can also be supplied a variadic list of substitution keys and their values for a given invocation.

The second way of thinking about substitution keys is that they are the starting values of keys in templates. So, they represent parameterized values of keys in templates.

As an example consider the following. In a template we have a field and value `:field :X`. The `:X` here is a substitution key - it will be replaced as the value of `:field` during transformation by _its_ value in the current substitution map. In the default substitution map `hc/_defaults` `:X` has a value of "x", so the result will be `:field "x"`.

A more complex example would be the field and value `:encoding :ENCODING` in the predefined subcomponent `ht/view-base` which lays out the structure of a Vega-Lite _view_.  As before `:ENCODING` will be replaced during transformation by the value of `:ENCODING` in the substitution map. However, the default value of `:ENCODING` is the predefined subcomponent `ht/xy-encoding`. This value is a map describing the structure of a view's `x-y` encodings and contains many fields with their own substitution keys. So, to produce a final value, `ht/xy-encoding` is recursively transformed so that the final value of `:encoding` is a fully realized `x-y` encoding for the view being processed. `ht/view-base` has several other such fields and is itself recursively transformed in the context of the current substitution map. And its final value will be the base of a chart, such as a line chart (`ht/line-chart`) or area chart (`ht/area-chart`), or some new plot/chart/layout/etc of your own for some domain specific application.


## Function values for substitution keys

A substitution key may have a function of one argument for its value: `(fn[submap] ...)`. The `submap` parameter is the current substitution map in the transformation. This map contains the special key `::hc/spec` whose value is the initial (input) specification as well as all current substitution keys and their current values. This is useful in cases where a substitution key's value should be computed from other values. For example, a typical ([Saite](https://github.com/jsa-aerial/saite#user-tabs) does this by default) use case would be to compute the _label_ (display name) of a tab from its _id_:

```Clojure
        :TLBL #(-> :TID % name cljstr/capitalize)
```

This takes the tab's id, which here is a keyword, converts to a string and returns the capitalized version.


## Subtitution Key Functions

A more general version of the function as value of a substitution key is provided by the `hc/subkeyfns` map and associated processing. The function `hc/update-subkeyfns` (see [API](#api)) can be used to register a function for a subtitution key. This is a 'global' function - separate from any value given for the key. These functions take three arguments: `(fn[submap, subkey, subval] ...)`.

Where the `submap` parameter is the current substitution map in the transformation; the `subkey` parameter is the substitution key for this function; and the `subval` parameter is the _value_ of the substitution key.

These functions can be very useful in providing a further level of abstractions is template specifications. For example, it is very typical that a `color` specification is intended for the mark of a chart (line-chart, bar-chart, point-chart, etc). providing a simple string indicating the data element to facet with colors makes for a cleaner abstraction, but this needs to be converted to correct full form `field` and `type` element. Having a subkeyfn for the `hc/color-key` (default `:COLOR`) supports this sort of processing:

```Clojure
        color-key
         (fn[xkv subkey subval]
           (if (string? subval)
             (xform ht/default-mark-props (assoc xkv :MPFIELD subval))
             subval))
```

There is a default set of such keys provided with this particular case being one of them. There are a few others, including one which implements the [file](#file-data-source) data source capability for specifications.


## Basic transformation rules

There are some important rules that guide certain aspects of the recursive transformation process. These aspects are really only important in advanced template contruction, but if you find yourself in such circumstances, they are quite useful and helpful.

* Defaults for substitution keys are always overridden by values given for them in a call to `hc/xform`.

* Any field whose final value is the special `hc/RMV` value, will be removed from the corresponding specification or subcomponent thereof.

* Any field whose final value is an empty collection, will have this value replaced by `hc/RMV` and thus removed from its containing collection and specification. This is a very important transformation rule as it enables highly general substitution values. See for example such values as `hc/data-options`.

* While the simplest way to look at the transformation process is as a continuous transform until the last output is the same as the input, the actual processing is a preorder depth first walk and replacement. Typically, this implementation detail should not factor in to any template authoring, as they should be declarative in nature. However, there may be cases where this ordering can help in constructing proper substitution keys and values.


## Meta data and the `:USERDATA` key

Vega and Vega-Lite support the inclusion of application specific meta data in specifications via the `:usermeta` top level key in a specification. To Vega and Vega-Lite, this key is completely transparent and is not used in any way by their processing. This affords applications using them to include application specific data, in particular, _control_ data.

Hanami defaults the `:usermeta` field to the substitution key `:USERDATA`, which has a default value of `RMV`. So, by default, there is no application meta data. However, several aspects of Hanami's [messaging system](#api), [session management](#sessions), [picture frames](#picture-frames) and [tab](#tabs) system expect and make use of control data supplied via the `:usermeta` field and associated values for the `:USERDATA` substitution key. So, if you plan on using any of that, you will need to supply a value for this key - either as a default (via `hc/update-defaults`) or explicitly in a transform (`hc/xform`). Of course, if you have your own application specific meta/control data that needs to be supplied to clients, you can provide your own fields and values here.

Hanami understands the following 'special' fields:

* `:tab` - value is a map of fields (`:id`, `:label`, `:opts`) identifying and controlling tabs.
  * `:id` - value is an id for the tab. Typically a keyword
  * `:label` - value is the display name for the tab
  * `:opts` - value is a map of fields (`:order`, `:eltsper`, and `:size`)
    * `:order` - value is either `:row` or `:col` for either row based grid layouts or column based grid layouts
    * `:eltsper` - value is an integer for the maximum number of row or col entries. For example a value of 2 would mean 2XN grids for rows and Nx2 grids for columns
    * `:size` - value is a flexbox size indicator. Best known values are "auto" for row based grids and "none" for column based grids

* `:opts` - value is a map of fields controlling Vega/Vega-Lite options. This is **not** the `[:tab :opts]` path and value!
  * `:export` - value is a map of boolean value fields (`:png`, `:svg`). Setting these true will provide implicit saving options in the popup Vega-Embed options button (typically upper left circle with "..." content).
  * `:renderer` - value is either "canvas" or "svg"
  * `:mode` - value is either "vega-lite" or "vega". Indicates whether the specification is a Vega spec or a Vega-Lite spec (that will be compiled to Vega before rendering). Hanami default values are in (`hc/default-opts` :vgl) which defaults to
```Clojure
    {:export {:png true, :svg true}
     :editor true
     :source false
     :renderer :RENDERER ; either "canvas" or "svg" - see defaults
     :mode :MODE}        ; either "vega-lite" or "vega" - see defaults
```

* `:vid` - value is an application specific identifier of the associated visualization (if any)

* `:msgop` - value is one of `:register`, `:tabs`, or some application specific operator. These messages are from the server to the client. `:register` is sent on client connection for [registration](#registration) purposes. `:tabs` is used to [update tabs](#tab-updates) and their content, if tabs are used. [User messages](#user-messages) have application specific operators and are sent when your application deems they should be sent.

* `:session-name` - the name of session(s) which will receive the complete specification (either via a `:tabs` message or some user message).

As an example, [Saite](https://github.com/jsa-aerial/saite#user-tabs) has an init function as part of its start up which sets the values of `:USERDATA` and subsequent defining substitution keys as:


```Clojure
:USERDATA
{:tab {:id :TID, :label :TLBL, :opts :TOPTS},
 :opts :OPTS,
 :vid :VID,
 :msgop :MSGOP,
 :session-name :SESSION-NAME}

:OPTS
{:export {:png true, :svg false},
 :renderer :RENDERER,
 :mode :MODE}

:SESSION-NAME "Exploring"
:TID :expl1
:TLBL #(-> :TID % name cljstr/capitalize)
:TOPTS {:order :row, :eltsper 2, :size "auto"}

:VID hc/RMV
:MSGOP :tabs
```


## Example predefined templates

Here are some examples as provided by the name space `aerial.hanami.templates` which is always referred to in documentation as `ht/`.

A number of 'fragments':

```Clojure
(def default-tooltip
  [{:field :X :type :XTYPE}
   {:field :Y :type :YTYPE}])

(def default-mark-props
  {:field :MPFIELD :type :MPTYPE})

(def default-row
  {:field :ROW :type :ROWTYPE})

(def data-options
  {:values :VALDATA, :url :UDATA, :name :NDATA})

(def interval-scales
  {:INAME
   {:type "interval",
    :bind "scales", ; <-- This gives zoom and pan
    :translate
    "[mousedown[event.shiftKey], window:mouseup] > window:mousemove!"
    :encodings :ENCODINGS,
    :zoom "wheel!",
    :resolve :IRESOLVE}})

(def mark-base
  {:type :MARK, :point :POINT,
   :size :MSIZE, :color :MCOLOR,
   :filled :MFILLED})
```

A couple 'subcomponents':

```Clojure
(def xy-encoding
  {:x {:field :X
       :type :XTYPE
       :timeUnit :XUNIT
       :axis :XAXIS
       :scale :XSCALE
       :aggregate :XAGG}
   :y {:field :Y
       :type :YTYPE
       :timeUnit :YUNIT
       :axis :YAXIS
       :scale :YSCALE
       :aggregate :YAGG}
   :opacity :OPACITY
   :row :ROWDEF
   :column :COLDEF
   :color :COLOR
   :size :SIZE
   :shape :SHAPE
   :tooltip :TOOLTIP})

(def view-base
  {:usermeta :USERDATA
   :title :TITLE
   :height :HEIGHT
   :width :WIDTH
   :background :BACKGROUND
   :selection :SELECTION
   :data data-options
   :transform :TRANSFORM
   :encoding :ENCODING})
```

And some charts.

```Clojure
;; Useful for empty picture frames
(def empty-chart
  {:usermeta :USERDATA})

(def line-chart
  (assoc view-base
         :mark (merge mark-base {:type "line"})))

(def point-chart
  (assoc view-base
         :mark (merge mark-base {:type "circle"})))

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
```

## Example predefined substitution keys

All of these are taken from `hc/_defaults` They are chosen so as to indicate how some aspects of the above template examples get transformed.

```Clojure
         :USERDATA RMV, :MODE "vega-lite", :RENDERER "canvas"
         :BACKGROUND "floralwhite"
         :OPACITY RMV

         ;; Note that removed things will get Vega/Vega-Lite defaults
         :TITLE RMV, :TOFFSET RMV
         :HEIGHT 300, :WIDTH 400, :DHEIGHT 60

         ;; get-data-vals is a function which handles :DATA and :FDATA
         :VALDATA get-data-vals
         :DATA RMV, :FDATA RMV, :SDATA RMV, :UDATA RMV, :NDATA RMV

         ;; Describes the x encoding in xy-encoding. Similar for y encoding
         :X "x", :XTYPE, "quantitative", :XUNIT RMV
         :XSCALE RMV, :XAXIS {:title :XTITLE, :grid :XGRID, :format :XFORMAT}
         :XTITLE RMV, :XGRID RMV, :XFORMAT RMV

         ;; Aggregation transforms
         :AGG RMV, :XAGG RMV, :YAGG RMV

         ;; Mark properties
         :POINT RMV, :MSIZE RMV, :MCOLOR RMV, :MFILLED RMV
         :MPTYPE "nominal", :SHAPE RMV, :SIZE RMV


         ;; Note that by default then :ROWDEF -> {:field :ROW :type :ROWTYPE} ->
         ;; {:field RMV, :type RMV} -> {} -> RMV. See transformation rules
         ;; Similar for :COLDEF
         :ROWDEF ht/default-row :ROW RMV, :ROWTYPE RMV

         :TOOLTIP ht/default-tooltip
         :ENCODING ht/xy-encoding

         :TRANSFORM RMV
         :SELECTION RMV
```


## Data Sources

Visualizations are based in and so require data to realize them. Specifications provide several means of declaring the source and processing of data to be used in a visualization. The underlying Vega and Vega-Lite systems provide for several of these by various means. Hanami currently directly supports three of these plus a fourth. The keys and expected values are given in this section.

* `:DATA` - expects an explicit vector of maps, each map defining the data fields and values
* `:UDATA` - expects a relative URL (for example "data/cars.json") or a fully qualified URL to a `csv` or `json` data file.
* `:NDATA` - expects a named Vega _data channel_.

These three are based directly on the underlying Vega and Vega-Lite options. The fourth, file data sources, is provided by Hanami.

### File data source

`:FDATA <filepath | [filepath, type-vector-or-map]]`

The `filepath` is a full path for the OS and can denote a file with extensions `clj`, `edn`, `json`, or `csv`. These extensions indicate the file types - no other checking is done. For the `clj`, `edn`, and `json` the content must be a vector of maps, where each map is a record of data fields and their values. CSV files are converted to vectors of maps, each map built from the column names and a row's corresponding values.

For `csv`, a `type-vec-or-map` may be provided as the second value of a vector pair (tuple). This can be either a vector of type indicators in 1-1 correspondence with the column names of the csv file, or a map where each key is one of the column names and the value is a type indicator. Type indicators supported are (the strings): "string", "int", "float", and "double". In either case, the strings in a row associated with the type indicators are converted per the indicator. The default type indicator for the case of maps is "string", i.e., return the original string value.

Example:

```Clojure
(hc/xform my-plot-template
  :FDATA ["/Data/RNAseq/Exp-xyz/Out/dge-xyz.csv" {"dge" "float"}])
```

This will create a vector of maps from the csv content, where the "dge" field in each map will have its value converted to a floating point number.


## Walk through example of transformation

It's worth having a look at what happens with the simple [car chart](#simple-cars) template example and its transformations. The value of `ht/point-chart` is:

```Clojure
(def point-chart
  (assoc view-base
         :mark (merge mark-base {:type "circle"})))
```

We have already seen ([above](#example-predefined-templates)) what `view-base`, `data-options`, and `mark-base` are, so we know that the starting value of `point-chart` is:

```Clojure
{:usermeta :USERDATA
 :title :TITLE
 :height :HEIGHT
 :width :WIDTH
 :background :BACKGROUND
 :mark {:type "circle"
        :point :POINT
        :size :MSIZE
        :color :MCOLOR
        :filled :MFILLED},
 :selection :SELECTION
 :data {:values :VALDATA, :url :UDATA, :name :NDATA}
 :transform :TRANSFORM
 :encoding :ENCODING}
 ```

We already have seen what the [default values](#example-predefined-substitution-keys) of the substitution keys here are. In the transform, explicit values are given for `:UDATA`, `:X`, `:Y`, and `:COLOR`. We will see about `:COLOR` a bit later, but with the values for the first three, conceptually, we have a "first version":

```Clojure
{:height 300
 :width 400
 :background "floralwhite"
 :mark {:type "circle"}
 :data {:url "data/cars.json"}
 :encoding {:x {:field "Horsepower"
                :type "quantitative"
                :axis {:title :XTITLE, :grid :XGRID, :format :XFORMAT}}
            :y {:field "Miles_per_Gallon"
                :type "quantitative"
                :axis {:title :YTITLE, :grid :YGRID, :format :YFORMAT}}
            :row {:field :ROW :type :ROWTYPE}
            :column {:field :COLUMN :type :COLTYPE}
            :color :COLOR
            :tooltip [{:field "Horsepower" :type "quantitative"}
                      {:field "Miles_per_Gallon" :type "quantitative"}]}}
```

By the [second rule](#basic-transformation-rules) of transformation, we also know that the `:axis`, `:row`, and `:column` fields will be replaced first by `{}` (since all of their fields default to `RMV`) and then by the [third rule](#basic-transformation-rules) they will be removed. So, conceptually, the 'next' version is:

```Clojure
{:height 300, :width 400, :background "floralwhite"
 :mark {:type "circle"}
 :data {:url "data/cars.json"}
 :encoding {:x {:field "Horsepower" :type "quantitative"}
            :y {:field "Miles_per_Gallon" :type "quantitative"}
            :color :COLOR
            :tooltip [{:field "Horsepower" :type "quantitative"}
                      {:field "Miles_per_Gallon" :type "quantitative"}]}}
```

OK, `:COLOR` is all we have left. As shown in the section on [substitution key functions](#subtitution-key-functions) Hanami has a default such function for the `hc/color-key` (default `:COLOR`). If the `subval` of the key is a string, then this function returns the value `(xform ht/default-mark-props (assoc xkv :MPFIELD subval))`. As we know from above, the value of `ht/default-mark-props` is `{:field :MPFIELD :type :MPTYPE}` and so the function here returns `{:field "Origin" :type "nominal"}`. So, the final value of the transformation is:

```Clojure
{:height 300, :width 400, :background "floralwhite"
 :mark {:type "circle"}
 :data {:url "data/cars.json"}
 :encoding {:x {:field "Horsepower" :type "quantitative"}
            :y {:field "Miles_per_Gallon" :type "quantitative"}
            :color {:field "Origin" :type "nominal"}
            :tooltip [{:field "Horsepower" :type "quantitative"}
                      {:field "Miles_per_Gallon" :type "quantitative"}]}}
```

The reason for caveat uses of 'conceptually' in the above description is the [final rule](#basic-transformation-rules) of transformation. Which says that the actual transformation process is via a depth first walk and replacement. So, for example, `:encoding` would have only had its full final value, not any intermediate value(s).




# Application Construction

Hanami is an visualization application developement enabler. It is a library with optional framework aspects provided for both server side (Clojure) and client side (ClojureScript) development. The client is most typically expected to be in the browser, but technically may not be.

The library portion of Hanami centers on

* [Templates, Substitution Keys and Transformations](#templates-substitution-keys-and-transformations)
* Simple, potent, _clean_ [messaging](#messages) system
* Clean single point [Reagent](http://reagent-project.github.io/) (React lifecycle) component for compiling, _both_ Vega and Vega-Lite and rendering the result.


These bits are not opionated in how to go about developing a domain specific visualization application. There is no constraint on how page(s) are to be laid out, what sort of ancillary and support components should be used, what CSS is used, etc. You can use Hiccup or [Re-Com](https://github.com/Day8/re-com) or whatever to layout and structure your application. Nor is there any opinion about the structure of the server side. And while there are a set of defaults, there are no requirements or expectations about the form, makeup, or content of templates and substitution keys. You can replace, augment, or chaange any or all of the defaults.


The framework portion of Hanami _is_ opinionated, though not too stringently. It consists of

* Client side application [header](#header) as provided by a user supplied function of zero arguments, which is expected to return a hiccup/re-com value which lays out the page header of the application. This value can simply be empty if you don't want this.

* Named [sessions](#sessions)


## Header


## Tabs


## Sessions


## Messages


### Registration


### Tab updates


### User messages


## Picture Frames

Picture frames are simply a way to automatically encase your visualizations with 'meta' level instrumentation and / or arbitrary annotations. They are composed of four individual parts corresponding to the top, bottom, left, and right quadrants. Picture frames (or simply frames) are specified in the `usermeta` data of a specification via the `:frame` key. By default Hanami uses the substitution key `:USERDATA` for this. So, the format is:

```Clojure
{...
 :USERDATA {...
            :frame {:top ...
                    :bottom ...
                    :left ...
                    :right ...}
            ...
            }
}
```

![Hanami picture frame](resources/public/images/picture-frame-layout.png?raw=true)

All of the quadrants are optional and `:frame {}` is legal. The value of a quadrant can be any legal mix of strings, hiccup, and / or active components. Where 'legal' here means 'yields a legal DOM branch'. A great resource for active components, which Hanami provides as part of its package, is [Re-Com](https://github.com/Day8/re-com). This is especially true if you are not a CSS/flex and / or component savant.

As of version 0.5.0, picture frames may be 'empty' in that they do not need to have an associated visualization. To make this a bit simpler, there is a new template for these cases `ht/empty-chart`.

You can specifiy frames from either the server or client side of your application. Working on the client side from within ClojureScript can make this more 'natural', as you are in the actual environment (browser/DOM) where things are running and rendering. However, specifying from the server side is fully supported, as long as Re-Com use is quoted (or more typical and useful, backquoted).

**NOTE**: if you are _instrumenting_ your visualization (using active components - input box, dropdowns, selection lists, etc.) the _functions_ updating the relevant model(s) of / for these components _must_ be written over on the ClojureScript side (client). This is because, Hanami does not use a self hosted ClojureScript, but rather the cross compiled (with Google Closure) variant. Hence, you cannot, for example, write function code on the server side and have it eval'd on the client!

Picture frames are fully automatic if you use the default tab system. If you use custom tabs or completely custom layout, and want to make use of frames, you should call `vis-list` on the client side (Cljs) for automatic rendering. As always, if you do not want to use any of this, you should use the `vgl` reagent vega/vega-lite component.

A couple of examples. These are actually taken from [Saite](https://github.com/jsa-aerial/saite), which is an interactive, exploratory and ad-hoc visualization application written with Hanami. Worth noting is that Saite assumes an interactive REPL model of exploration on the server side, pushing visualizations to the client. Hence, the only active components that can be used are those that are self contained, like information buttons or modal panels.


```Clojure
(let [_ (hc/add-defaults
         :NAME #(-> :SIDE % name cljstr/capitalize)
         :STYLE hc/RMV)
      frame-template {:frame
                      {:SIDE `[[gap :size :GAP]
                               [p {:style :STYLE}
                                "This is the " [:span.bold :NAME]
                                " 'board' of a picture "
                                [:span.italic.bold "frame."]]]}}
      frame-top (hc/xform
                 frame-template :SIDE :top :GAP "10px")

      frame-left (hc/xform
                  frame-template :SIDE :left :GAP "10px"
                  :STYLE {:width "100px" :min-width "50px"})
      frame-right (merge-with merge
                   (hc/xform
                    frame-template :SIDE :right :GAP "2px"
                    :STYLE {:width "100px" :min-width "50px"})
                   (hc/xform
                    frame-template :SIDE :left :GAP "2px"
                    :STYLE {:width "100px" :min-width "50px"
                            :color "white"}))
      frame-bot (hc/xform
                 frame-template :SIDE :bottom :GAP "10px")]
  (->> (mapv #(hc/xform ht/point-chart
                :HEIGHT 200 :WIDTH 250
                :USERDATA (merge (hc/get-default :USERDATA) %)
                :UDATA "data/cars.json"
                :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
             [frame-top frame-left frame-bot frame-right])
       hmi/sv!))
```

![Hanami picture frame](resources/public/images/picture-frame-quads.png?raw=true)

```Clojure
(let [text "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsum, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."
      frame {:frame
             {:top `[[gap :size "150px"]
                     [p "An example showing a "
                      [:span.bold "picture "] [:span.italic.bold "frame"]
                      ". This is the top 'board'"
                      [:br] ~text]]
              :left `[[gap :size "10px"]
                      [p {:style {:width "100px" :min-width "50px"}}
                       "Some text on the " [:span.bold "left:"] [:br] ~text]]
              :right `[[gap :size "2px"]
                       [p {:style {:width "200px" :min-width "50px"
                                   :font-size "20px" :color "red"}}
                        "Some large text on the " [:span.bold "right:"] [:br]
                        ~(.substring text 0 180)]]
              :bottom `[[gap :size "200px"]
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
                                    :target "_blank"]]]]]]}}]
  (->> [(hc/xform ht/point-chart
          :USERDATA
          (merge
           (hc/get-default :USERDATA) frame)
          :UDATA "data/cars.json"
          :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")]
       hmi/sv!))
```

![Hanami picture frame](resources/public/images/picture-frame-all-quads-ex.png?raw=true)


### Empty Frames

As of version 0.5.0, picture frames can now be 'empty', i.e., they do not need an associated visualization. The basic layout is the same as for frames with visualizations:

![Hanami empty frame](resources/public/images/empty-picframe-layout.png?raw=true)

To make use of these simpler (and easier) there is a new standard template `ht/empty-chart` which can be used to create them. Empty frames enable a more general document structure supporting paragraphs of html/'text oriented' material along side or in place of standard visualizations with or without frames. Here are a couple of examples showing the basic capability:

This example shows an empty frame with all 4 picture frame elements (areas) with various html/text information

```Clojure
(let [text "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsum, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."
      frame {:frame
             {:top `[[gap :size "50px"]
                     [p {:style {:width "600px" :min-width "50px"}}
                      "An example empty picture frame showing all four areas."
                      " This is the " [:span.bold "top"] " area. "
                       ~text ~text ~text]]
              :left `[[gap :size "50px"]
                      [p {:style {:width "300px" :min-width "50px"}}
                       "The " [:span.bold "left "] "area as a column of text. "
                       ~text ~text ~text ~text]]
              :right `[[gap :size "70px"]
                      [p {:style {:width "300px" :min-width "50px"}}
                       "The " [:span.bold "right "] "area as a column of text. "
                       ~text ~text ~text ~text]]
              :bottom `[[gap :size "50px"]
                        [v-box
                         :children
                         [ [p {:style {:width "600px" :min-width "50px"}}
                           "The " [:span.bold "bottom "]
                           "area showing a variety of text. "
                           [:span.italic ~text] [:span.bold ~text]]
                          [p {:style {:width "600px" :min-width "50px"
                                      :color "red"}}
                           ~text]]]]}}]
  (->> (hc/xform
        ht/empty-chart
        :TID :picframes
        :USERDATA (merge (hc/get-default :USERDATA) frame))
       hmi/sv!))
```

![Hanami empty picframe](resources/public/images/picframe-empty.png?raw=true)

This next eample shows a tab/page with two picture frames. The left being a frame with an associated chart, while the one on the right shows how you can structure text in columns.

```Clojure
(let [text "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsum, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."
      frame1 {:frame
              {:top `[[gap :size "50px"]
                      [p "Here's a 'typical' chart/plot filled picture frame."
                       "It only has the top area"
                       [:br] ~text]]}}
      frame2 {:frame
              {:left `[[gap :size "20px"]
                       [p {:style {:width "200px" :min-width "50px"}}
                        "This is an empty frame with a " [:span.bold "left "]
                        "column of text" [:br] ~text ~text ~text ~text]]
               :right `[[gap :size "30px"]
                        [p {:style {:width "200px" :min-width "50px"}}
                         "And a " [:span.bold "right "]
                         "column of text"
                         [:br] ~text ~text ~text ~text]]}}]
  (->> [(hc/xform ht/point-chart
          :USERDATA (merge (hc/get-default :USERDATA) frame1)
          :TID :picframes :UDATA "data/cars.json"
          :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
        (hc/xform ht/empty-chart
          :USERDATA (merge (hc/get-default :USERDATA) frame2)
          :TID :picframes)]
       hmi/sv!))
```

![Hanami picframe chart and empty](resources/public/images/picframe-chart-and-empty.png?raw=true)


## Data Streaming


## API

As noted, with respect to abstracting visualizations (ala' something like [Altair](https://altair-viz.github.io/)) there isn't much of an API and no classes, objects, or methods are involved. Most of what API there is centers on application development and start up. This is split across the server and the client side.


### Templates and Substitution keys

This applies across both the server and client - the facilities are available in both server and client. They are in `aerial.hanami.common`, in this documentation aka `hc`. **NOTE** this namespace, along with `aerial.hanami.templates` (aka `ht`), is available on _both_ the client and server.

* `(defn update-subkeyfns [k vfn & kvfns])`: Updates the [substitution key function map](#subtitution-key-functions). `k` is a substitution key, `vfn` is either a function `(fn [submap, subkey, subval] ...)` or the special `hc/RMV` value. The latter case will _remove_ any current key `k` and its value.

* `(defn update-defaults [k v & kvs])`: Updates the default [substitution key map](#example-predefined-substitution-keys). `k` is a substitution key, `v` is some appropriate value, including `hc/RMV`, which will remove the key.

* `(defn get-default [k])`: Returns the current default value of substitution key `k`.

* `(defn xform ([spec submap]) ([spec k v & kvs]) ([spec]))`: The template/specification transformation function. Generally, you will only call the second two arity signatures. The first is used for recursive transformation actions, so in the first signature, `spec` is generally the current value of a recursive transformation. In the second and third signatures, `spec` is a template. [Recall](#templates-substitution-keys-and-transformations) legal templates may be already complete fully realized Vega or Vega-Lite specifications - this latter case is for what the third signature is intended. The second signature is what you will use in nearly all cases. `k` is a substitution key and `v` is the value it will have in the transformation. Hence you can override defaults and add specific keys for just a given transformation. Returns the fully transformed final value.


### Message system

This applies across both the server and client - the facilities are available in both server and client. They are in `aerial.hanami.core`, in this documentation aka `hc`. **NOTE** this namespace, exists on _both_ the client and server.

* `(defn send-msg [ws app-msg]): Inherited from [Hanasu](https://github.com/jsa-aerial/hanasu). `ws` is the websocket/channel
* `(defmulti user-msg :op)`: Multimethod for encoding application specific message envelopes (see [Hanasu](https://github.com/jsa-aerial/hanasu) for details). Specifically,




# Example Transform 'Gallery'

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

![Hanami pic 4.1](resources/public/images/real-vs-binomial-col.png?raw=true)

![Hanami pic 4.2](resources/public/images/real-vs-binomial-row.png?raw=true)


This is a nice example of how one visualization (the row grouping) can bring out the salient information so much better than another (the col grouping)


The next is a visualization for an investigation into using lowess smoothing of TNSeq fitness data.

```Clojure
(hc/xform ht/layer-chart
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

![Hanami pic 5](resources/public/images/lowess-tnseq-smoothing.png?raw=true)


We can do something more interesting here in this case, as we may want to get close ups of various sections of such a plot. Instead of looking at the entire genome, we can focus on chunks of it with selection brushes using an overlay+detail display:

```Clojure
(hc/xform ht/vconcat-chart
   :TITLE "Raw vs 1-4 lowess smoothing" :TOFFSET 5
   :DATA (concat base-xy lowess-1 lowess-2 lowess-3 lowess-4)
   :VCONCAT [(hc/xform ht/layer-chart
               :LAYER
               (mapv #(hc/xform
                       ht/gen-encode-layer :WIDTH 1000
                       :MARK (if (= "NoL" %) "circle" "line")
                       :TRANSFORM [{:filter {:field "NM" :equal %}}]
                       :XSCALE {:domain {:selection "brush"}}
                       :COLOR "NM", :XTITLE "Position", :YTITLE "Count")
                     ["NoL" "L1" "L2" "L3"]))
             (hc/xform ht/gen-encode-layer
               :MARK "circle"
               :WIDTH 1000, :HEIGHT 60
               :TRANSFORM [{:filter {:field "NM" :equal "NoL"}}]
               :SELECTION {:brush {:type "interval", :encodings ["x"]}}
               :COLOR "NM" :XTITLE "Position", :YTITLE "Count")])
```

Here are two snapshots of the resulting interactive visualization:

![Hanami pic 5.1](resources/public/images/lowess-overlay-detail-1.png?raw=true)
![Hanami pic 5.2](resources/public/images/lowess-overlay-detail-2.png?raw=true)



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
              ht/point-chart
              :TITLE "MA Plot"
              :MSIZE 40
              :SELECTION (merge  (hc/xform ht/interval-scales :INAME "grid1")
                                 mdwn-brush)
              :X "baseMean", :Y "log2FoldChange"
              :COLOR color, :SIZE size, :TOOLTIP tooltip)
             (hc/xform
              ht/point-chart
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

