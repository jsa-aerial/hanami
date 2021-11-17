[![Clojars Project](https://img.shields.io/clojars/v/aerial.hanami.svg)](https://clojars.org/aerial.hanami)

# Hanami

Interactive arts and charts visualizations with Clojure(Script), Vega-lite, and Vega. Flower viewing 花見 (hanami)

<a href="https://jsa-aerial.github.io/aerial.hanami/index.html"><img src="https://github.com/jsa-aerial/hanami/blob/master/resources/public/Himeji_sakura.jpg" align="left" hspace="10" vspace="6" alt="hanami logo" width="150px"></a>

**Hanami** is a Clojure(Script) library and framework for creating interactive visualization applications based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer). In addition to VGL and VG, Hanami is built on top of [Reagent](http://reagent-project.github.io/) and [Re-Com](https://github.com/Day8/re-com).


Table of Contents
=================

   * [Overview](#overview)
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
      * [Template local defaults](#template-local-defaults)
      * [Basic transformation rules](#basic-transformation-rules)
      * [Meta data and the :USERDATA key](#meta-data-and-the-userdata-key)
      * [Example predefined templates](#example-predefined-templates)
      * [Example predefined substitution keys](#example-predefined-substitution-keys)
      * [Data Sources](#data-sources)
         * [File data source](#file-data-source)
      * [Walk through example of transformation](#walk-through-example-of-transformation)
   * [Application Construction](#application-construction)
      * [Library overview](#library-overview)
      * [Framework overview](#framework-overview)
         * [framework topology graphic](#framework-topology-graphic)
      * [Resource requirements](#resource-requirements)
      * [Header](#header)
      * [Tabs](#tabs)
         * [Basics](#basics)
         * [Configuration and behavior](#configuration-and-behavior)
         * [Extension tabs](#extension-tabs)
         * [Incremental bodies](#incremental-bodies)
         * [Wrapping functions](#wrapping-functions)
      * [Sessions](#sessions)
      * [Messages](#messages)
         * [Connection](#connection)
         * [Session group](#session-group)
         * [Tab updates](#tab-updates)
         * [User messages](#user-messages)
      * [Picture Frames](#picture-frames)
         * [Empty Frames](#empty-frames)
      * [Data Streaming](#data-streaming)
      * [Client Only Apps](#client-only-apps)
         * [Basic client app requirements](#basic-client-app-requirements)
            * [Require resources](#require-resources)
            * [Application initialization](#application-initialization)
            * [Landing page - index.html](#landing-page---indexhtml)
            * [Application start](#application-start)
         * [Example Client Only Application](#example-client-only-application)
      * [API](#api)
         * [Templates and Substitution keys](#templates-and-substitution-keys)
         * [Startup](#startup)
            * [Server start](#server-start)
            * [Client start](#client-start)
         * [Message system](#message-system)
            * [send msg](#send-msg)
            * [user msg](#user-msg)
         * [Client core](#client-core)
            * [Visualization](#visualization)
            * [Tab system](#tab-system)
            * [Hanami main](#hanami-main)
         * [Server core](#server-core)
   * [Example Transform 'Gallery'](#example-transform-gallery)
      * [Observed vs Binomial Model](#observed-vs-binomial-model)
      * [Lowess smoothing of Tn-Seq fitness data](#lowess-smoothing-of-tn-seq-fitness-data)
         * [With Overview   Detail](#with-overview--detail)
      * [Interactive Cross plot Differential Gene Expression](#interactive-cross-plot-differential-gene-expression)

[toc](https://github.com/ekalinin/github-markdown-toc)

# Overview

**Hanami** is a Clojure(Script) library and framework for creating interactive visualization applications based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer).

In keeping with the central data oriented tenet, Hanami eschews the typical API approach for generating specifications in favor of using recursive transforms of parameterized templates. This is also in keeping with the data transformation focus in functional programming, which is espcially nice in Clojure(Script).

An important aspect of this approach is that parameterized templates can be used to build other such templates by being higher level substitutions. In addition templates can be composed and this is an important idiomatic use. Furthermore, templates may be merged, though typically this is after transformation. The overall result enables the construction of sharable libraries of templates providing reusable plots, charts, and entire visualizations. Generally these will be domain and/or task specific. Hanami itself provides only a small set of very generic templates, which have proven useful in constructing more domain/task specific end results.

Hence, templates are a means to abstract all manner of visualization aspects and requirements. In this sense they are similar to what [Altair](https://altair-viz.github.io/) provides but without the complications and limitations of an OO class/method based approach.




# Installation

To install, add the following to your project `:dependencies`:

    [aerial.hanami "0.15.1"]




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
* Uses light weight websocket [messaging system](https://github.com/jsa-aerial/hanasu)




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

_Substitution Keys_ can be considered or thought of in two ways. They are the keys in the substitution map(s) of the recursive transformer [hc/xform](#templates-and-substitution-keys). There is a default map `hc/_defaults` (which is an atom containing the map) with various default substitution keys and their corresponding default values. This map can be updated and `xform` can also be supplied a variadic list of substitution keys and their values for a given invocation.

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

A more general version of the function as value of a substitution key is provided by the `hc/subkeyfns` map and associated processing. The function [hc/update-subkeyfns](#templates-and-substitution-keys) can be used to register a function for a subtitution key. This is a 'global' function - separate from any value given for the key. These functions take three arguments: `(fn[submap, subkey, subval] ...)`.

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


## Template local defaults

As of version `0.12.9` there is support for default parameterization per template (where 'template' here means any map whatsoever).  A new transformation control key is introduced, `:aerial.hanami.templates/defaults` which can be used to  supply a map, local to the template, containing default values of substitution keys.  The substitution keys may be those specific only to the template or any other enclosing template, or for changing global defaults.  The values are in effect for the duration of the template's processing, unless a more nested template has its own local defaults which override some of the outer's values.  In all cases, the in line (point of call) user specified values to [hc/xform](#templates-and-substitution-keys) take precedent over any template local defaults.

Local defaults can be a nice way to deliver new templates without any need to setup new substitution keys in the global registry or require users to add many key-value pairs to their call to [hc/xform](#templates-and-substitution-keys) using the new templates.  Addtionally they can automatically parameterize the underlying templates (those used in constructing a new template) to reflect the value of them required for the form of a new template.  As an example, let's look at the following, which will show the construction of a new `trend-chart` for plotting base data along with a smoothed trend line for it.

```Clojure
;;; Base layer for trend-chart
(def trend-layer
  (assoc ht/line-chart
         :aerial.hanami.templates/defaults
         {:X :data/x> :XTYPE :xtype>
          :Y :data/y> :YTYPE :ytype>
          :YSCALE {:zero false}
          :DATA hc/RMV
          :WIDTH :width> :HEIGHT :height>
          :USERDATA hc/RMV}))

```

This gives the base form for what the two layers will look like.  It is simply the base `ht/line-chart` parameterized to give the default value of each layer when transformed.  The new trend chart will use `:data/x>` and `:data/y>` as the parameters for x and y data fields, along with corresponding parameters for the types of the fields `:xtype>` and `:ytype>`.  Next we use this base layer template to create the `trend-chart` template:

```Clojure
;;; Trend chart - two layers.  1. base data, 2. loess trend line
;;; Parameters :data/x for x-axis data. :data/y> for y-axis data
;;;            :xtype> and :ytype> for axis types, will default to :XTYPE :YTYPE
;;;            :width>, will default to 700
;;;            :height>, will default to :HEIGHT default
;;;            :trend-color> for loess line, defaults to "firebrick"
(def trend-chart
  (assoc ht/layer-chart
         :description "A two layer plot of base data and its smoothed trend line given by loess transform"
         :aerial.hanami.templates/defaults
         {:LAYER [(hc/xform trend-layer)
                  (hc/xform
                   trend-layer
                   :TRANSFORM [{:loess :data/y> :on :data/x>}]
                   :MCOLOR :trend-color>)]
          :trend-color> "firebrick"
          :xtype> :XTYPE :ytype> :YTYPE
          :width> 700
          :height> (hc/get-defaults :HEIGHT)}))
```

Here we use the underlying templates of the predefined `ht/layer-chart` and the new `trend-layer`.  The layer structure is given by the default for `:LAYER` as being the two transformed cases for `trend-layer`. The first layer transform just ensures the result has all the new parameters in place.  The second also gives the loess transform and provides for a new `trend-color` for custom coloring the trend line, with a default of "firebrick".  The width of the chart gets a new default of 700 while the height defaults to the global `:HEIGHT` default.  The new `:xtype>` and `:ytype>` axis types get defaults of whatever the global `:XTYPE` and `:YTYPE` values are.

This can now be used in a highly simplified form such as the following:

```Clojure
(let [[cols data] (get-trend-data file)]
 (hc/xform
  trend-chart
  :DATA (mapv #(zipmap cols %) data)
  :data/x> :year :xtype> :temporal
  :data/y> :C))

```

Resulting in this output:

![trend-chart](resources/public/images/trend-chart.png?raw=true)



## Basic transformation rules

There are some important rules that guide certain aspects of the recursive transformation process. These aspects are really only important in advanced template contruction, but if you find yourself in such circumstances, they are quite useful and helpful.

* Defaults for substitution keys are always overridden by values given for them in a call to [hc/xform](#templates-and-substitution-keys).

* Any field whose final value is the special `hc/RMV` value, will be removed from the corresponding specification or subcomponent thereof.

* Any field whose final value is an empty collection, will have this value replaced by `hc/RMV` and thus removed from its containing collection and specification. This is a very important transformation rule as it enables highly general substitution values. See for example such values as `ht/data-options`.

* While the simplest way to look at the transformation process is as a continuous transform until the last output is the same as the input, the actual processing is a preorder depth first walk and replacement. Typically, this implementation detail should not factor in to any template authoring, as they should be declarative in nature. However, there may be cases where this ordering can help in constructing proper substitution keys and values.


## Meta data and the `:USERDATA` key

Vega and Vega-Lite support the inclusion of application specific meta data in specifications via the `:usermeta` top level key in a specification. To Vega and Vega-Lite, this key is completely transparent and is not used in any way by their processing. This affords applications using them to include application specific data, in particular, _control_ data.

Hanami defaults the `:usermeta` field to the substitution key `:USERDATA`, which has a default value of `RMV`. So, by default, there is no application meta data. However, several aspects of Hanami's [messaging system](#messages), [session management](#sessions), [picture frames](#picture-frames) and [tab](#tabs) system expect and make use of control data supplied via the `:usermeta` field and associated values for the `:USERDATA` substitution key. So, if you plan on using any of that, you will need to supply a value for this key - either as a default (via `hc/update-defaults`) or explicitly in a transform (`hc/xform`). Of course, if you have your own application specific meta/control data that needs to be supplied to clients, you can provide your own fields and values here.

Hanami understands the following 'special' fields:

* `:tab` - value is a map of fields (`:id`, `:label`, `:opts`) identifying and controlling tabs.
  * `:id` - value is an id for the tab. Typically a keyword
  * `:label` - value is the display name for the tab
  * `:opts` - value is a map of fields (`:order`, `:eltsper`, `:size`, and :wrapfn)
    * `:order` - value is either `:row` or `:col` for either row based grid layouts or column based grid layouts
    * `:eltsper` - value is an integer for the maximum number of row or col entries. For example a value of 2 would mean 2XN grids for rows and Nx2 grids for columns
    * `:size` - value is a flexbox size indicator. Best known values are "auto" for row based grids and "none" for column based grids
    * `:wrapfn` - value is a function which takes the hiccup element for the tab's body (fully structured grid layout) and must return a hiccup/re-com element. The intent is a user may wish to encase the grid body in extra ancillary support layout / active elements. See [wrapping functions](#wrapping-functions)

* `:opts` - value is a map of fields controlling Vega/Vega-Lite options. This is **not** the `[:tab :opts]` path and value!
  * `:export` - value is a map of boolean value fields (`:png`, `:svg`). Setting these true will provide implicit saving options in the popup Vega-Embed options button (typically upper right circle with "..." content).
  * `:scaleFactor` - value is a number indicating how much to scale image when exporting png or svg.
  * `:renderer` - value is either "canvas" or "svg"
  * `:mode` - value is either "vega-lite" or "vega". Indicates whether the specification is a Vega spec or a Vega-Lite spec (that will be compiled to Vega before rendering). Hanami default values are in (`hc/default-opts` :vgl) which defaults to
```Clojure
    {:export {:png true, :svg true}
     :scaleFactor :SCALEFACTOR
     :editor true
     :source false
     :renderer :RENDERER ; either "canvas" or "svg" - see defaults
     :mode :MODE}        ; either "vega-lite" or "vega" - see defaults
```

* `:vid` - value is an application specific identifier of the associated visualization (if any). This is used as the HTML `id` tag for the element directly containing the visualization. Useful when needing to dynamically manipulate the container and/or the visualization.

* `:msgop` - value is one of `:register`, `:tabs`, or some application specific operator. These messages are from the server to the client. `:register` is sent on client connection for [registration](#connection) purposes. `:tabs` is used to [update tabs](#tab-updates) and their content, if tabs are used. [User messages](#user-messages) have application specific operators and are sent when your application deems they should be sent.

* `:session-name` - the name of session(s) which will receive the complete specification (either via a `:tabs` message or some user message).

As an example, [Saite](https://github.com/jsa-aerial/saite) has an init function as part of its start up which sets the values of `:USERDATA` and subsequent defining substitution keys as:


```Clojure
:USERDATA
{:tab {:id :TID, :label :TLBL, :opts :TOPTS},
 :frame {:fid :FID, :top :TOP, :bottom :BOTTOM, :left :LEFT, :right :RIGHT}
 :opts :OPTS,
 :vid :VID,
 :msgop :MSGOP,
 :session-name :SESSION-NAME}

:OPTS (hc/default-opts :vgl)
:SESSION-NAME "Exploring"
:TID :expl1
:TLBL #(-> :TID % name cljstr/capitalize)
:TOPTS (hc/default-opts :tab)
:MSGOP :tabs
```

Where `hc/default-opts` is:

```Clojure
{:vgl {:export {:png true, :svg true}
       :scaleFactor :SCALEFACTOR
       :editor true
       :source false
       :renderer :RENDERER ; either "canvas" or "svg" - see defaults
       :mode :MODE}        ; either "vega-lite" or "vega" - see defaults
 :tab {:order :ORDER       ; either :row or :col - see defaults
       :eltsper :ELTSPER   ; count of elements per row/col - see defaults
       :size "auto"}}
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
         :USERDATA RMV, :MODE "vega-lite", :RENDERER "canvas", :SCALEFACTOR 1
         :TOP RMV, :BOTTOM RMV, :LEFT RMV, :RIGHT RMV
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

## Library overview

The library portion of Hanami centers on

* [Templates, Substitution Keys and Transformations](#templates-substitution-keys-and-transformations)
* Simple, potent, _clean_ [messaging](#messages) system
* Clean single point [Reagent](http://reagent-project.github.io/) (React lifecycle) component for compiling, _both_ Vega and Vega-Lite and rendering the result.


These bits are not opionated in how to go about developing a domain specific visualization application. There is no constraint on how page(s) are to be laid out, what sort of ancillary and support components should be used, what CSS is used, etc. You can use Hiccup or [Re-Com](https://github.com/Day8/re-com) or whatever to layout and structure your application. Nor is there any opinion about the structure of the server side. And while there are a set of defaults, there are no requirements or expectations about the form, makeup, or content of templates and substitution keys. You can replace, augment, or change any or all of the defaults.


## Framework overview

The framework portion of Hanami _is_ opinionated, though not too stringently. It consists of

* Client side application [header](#header) as provided by a user supplied function of zero arguments, which is expected to return a hiccup/re-com value which lays out the page header of the application. This value can simply be empty if you don't want this.

* Named [session](#sessions) groups. The default (client) header function directly supports this by providing an input text box to specify which session group is desired. Any session in a session group of name _name_ will receive any messages sent to that group. This supports dynamic sharing.

* A [tab system](#tabs) for automatically structuring both your application layout (each tab can be a page, chapter, subsection, etc) and the structure and content of each such component.

These combine to form the basic page layout from this 'framework perspective' as shown here:

### framework topology graphic

![Hanami framework layout](resources/public/images/framework-page-structure.png?raw=true)

The header area is constructed by the `:header-fn` argument of the [client start](#client-start) function. The tab bar is dynamically constructed via `:tabs` [messages](#tab-updates) or by explict calls to the [hmi/tabs](#tab-system) client function. The _content_ of each tab's body is also constructed dynamically via these same means. If the tab doesn't exist, it will be created and added to the tab bar at the time it's body is also rendered. Updates to a tab will simply update the existing tab's body.

As an example [Saite](https://github.com/jsa-aerial/saite) makes use of the framework aspects of Hanami and here is an example page layout from a session in it.

![Hanami framework layout](resources/public/images/saite-framework-page.png?raw=true)


## Resource requirements

The basic resource requirements are the various stylesheets, mostly in support of [re-com](https://github.com/Day8/re-com), that are shown in the development 'landing page' [index.html](https://github.com/jsa-aerial/hanami/blob/master/resources/public/Fig/index.html). When you create your own `index.html` landing page - or generate it dynamically - you will need to include these resources.

Also, as indicated in the development `index.html`, you will need to indicate where your final compiled JavaScript implementing your application will be and what it is called.

**NOTE**: If you will be running [serverless](#client-only-apps), you will need to make sure that the resources and final javascript are located relative to where you load your landing page from or that all the links are absolute.


## Header

The client side [start](#client-start) function has a `:header-fn` parameter which is a function of a single parameter that should return the main application header area. The default function for this returns a header which supports multiple named [session groups](#sessions). This is one mechanism where by you can insert global controls, logos, application 'avatars', etc. Alternatively you may choose to have this return nothing and wait for the [application initialization](#connection) message, to perform the application setup. The latter may be more appealing if you compute a number of fields that support the contruction of a more sophisticated header.


## Tabs

The tab system of Hanami is intended to be a fairly general purpose document structuring and layout capability for a variety of visualization applications. But that very fact means it has some level of 'opinion' on such layouts and so you may not want to use it if you need total general / manual layouts. But for the very large subset of cases where it fits nicely it can be very helpful. There is a set of [functions and components](#tab-system) of the client API for dealing with the tab system. **If you use the standard framework main components, you will not have to use this API** - it is all handled for you. The API is given in case you need / want to set up your own components which make use of it.


### Basics

As depicted in the [framework topology graphic](#framework-topology-graphic), the tab system has a dedicated area for the tab bar. Tabs are dynamically added to the tab bar from left to right. They may also be deleted. Any selected tab will have its body displayed in the dedicated content area. If you use Hanami's standard framework, the main components will drive all tab actions and updates.

### Configuration and behavior

To use tabs, the `:usermeta` field of specifications must contain the `:tab` key whose value must be a map. The keys and values of this map are detailed in the tab section of the [USERDATA](#meta-data-and-the-userdata-key) section of this documentation. The `:id` field is the primary key for identifying and manipulating a tab and its body. The `:label` field is for human readable naming of the tab. The `:opts` field details the auto layout format for the tab's body. It is worth noting that if the `:eltsper` field has a value of 1, the effect is to have the body layed out linearly from top to bottom in the order the specifications were given to the tab.

Tabs are added and updated via the [update-tabs](#tab-system) client core function. This function takes a vector of specifications and groups them by the tab id for each. Then for each such group,preprocesses the specifications (including instrumentation and frames) and places this information into the tab database. This database update triggers the Reagent components responsible for rendering any such changes.

### Extension tabs

In addition to 'standard tabs', those described above, the tab system may be extended with 'extension tabs'. The defining characteristic of such tabs is that they have an extra field: `[:opts :extfn]`. The value of this field must be a [Reagent form](https://github.com/reagent-project/reagent/blob/master/doc/CreatingReagentComponents.md) function. Typically it will be a **form-2** and will implement the body of the tab. This functionality is seamlessly integrated with standard tabs and will be implicitly invoked when the tab is selected.

Extension tabs are intended to be built and added on the client side. [add-tab](#tab-system) can be used to manually add such tabs, and the [:app-init user-msg](#connection) is a convenient place to perform this action (as part of any other application initialization). **NOTE** in [client only](#client-only-apps) you can explicitly fire this message as part of on page load code start.

### Incremental bodies

As of version 0.9.0, there is support for _incrementally_ adding to a tab's [content area](#framework-topology-graphic), which is also known as it's _body_. Previously the only way to effect a change on a standard (non extention) tab's body was to perform a full update on it via [update-tabs](#tab-system) or more directly via [vis-list](#tab-system). Both of these required the full list of specifications to be rendered. There are applications where this is simply too restrictive. You may want to simply insert a [picture frame](#picture-frames) at a given point in a page. Or simply extend it, without needing to manage the entire sequence of specifications. There are now two new client core functions [add-to-tab-body](#tab-system) and [remove-from-tab-body](#tab-system) which can adjust the content of a tab at the granularity of picture frames.

### Wrapping functions

As of version 0.9.0 there is now support for per tab user _wrapping functions_. This supports custom layout encompassing or encasing the tab's fully structured grid layout of the tab's sequence of specifications. Wrapping functions are configured by means of the `:wrapfn` key/value pair specifiable in the [tab's options](#meta-data-and-the-userdata-key). This provides a level of customization that is in between fully custom [extension tabs](#extension-tabs) and standard tabs. Wrapping functions must accept one argument, `gridhc`, which is the fully computed hiccup for the grid layout of the tab, and return a hiccup/re-com element which includes the `gridhc` element somewhere inside it (directly or in some child). The result will then be rendered in the tab's content area.


## Sessions

The server side [start-server](#server-start) function has an `:idfn` parameter which is a parameterless function which returns session names for client connections. Upon a client connetion (initial websocket connect), the server will send a [registration](#connection) message to the client. Among other fields in the data sent will be a `:session-name`.  This is the name of the session group the client has been placed in according to the `idfn` function.

It is possible for the client to "opt out" of this session group in favor of another - including a totally new one of it's own making - by sending a [set session](#session-group) message to the server. This also implies that session groups need only contain one session.

As a simple example of how this can be done, the client [start](#client-start) function's default `header-fn` function uses a two stage input area to support changing the server's default assigned session group to some other such group (including a new one if the input does not name an existing group):

```Clojure
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
              [session-input]
              [gap :size "5px"]
              [title
               :level :level3
               :label [:span.bold (get-adb [:main :uid :name])]]
              [gap :size "30px"]]])
```

On connection the client's header contains a header with the default assigned session group name listed along side an input text box. The user can decide to change the assigned group, by typing in a new name. This will fire a [set-session-name](#session-group) message to the server to reassign this session to the desired group.

![message overivew](resources/public/images/session-group-b4-aft.png?raw=true)


Any message sent from the server to a named session group, will result in all current session members getting the message. In particular, messages updating the visualizations and picture frame content of tab bodies, will be sent to all such session members. This is equally true for application specific messages sent via [hmi/send-msg](#send-msg)


## Messages

There are a few builtin messages which are sent between the server and client. These are generally in support of the framework aspects of Hanami. In general, domain specific applications will have a set of their own messages. These can be sent via [send-msg](#send-msg), available on both the client and server, and processed by [user-msg](#user-msg), also available on both the client and server


![message overivew](resources/public/images/messages-overview.png?raw=true)

The messages listed in that graphic overview are roughly in the order they occur from top to bottom.  Let's walk through them so that we know what happens as they occur.

### Connection

Upon a client opening a connection, the server creates a session/app initialization data package, which is sent to the client via a `:register` message. This package is a map composed of two groups of data:

* A standard set of fields and values:
  - `:uid` a uid value `{:uuid (hmi/uuid), :name (idfn)}`
    - `hmi/uuid` generates string names from uuids
    - `idfn` is the [start-server](#server-start) function's `:idfn` argument, which is expected to return the applications [session group](#sessions) names.

  - `:title`, the `start-server` function's `:title` argument
  - `:logo`, the  `start-server` function's `:logo` argument
  - `:img`, the  `start-server` function's `:img` argument
  - `:opts`, the current `hc/default-opts` value

* A, possibly empty, set of user/application specific fields. These are determined by the [start-server](#server-start) function's `:connfn` argument. This is a function of one parameter `(fn [x] ...)` where `x` is the map of standard fields and values. The expectation the function will compute new fields and values and add them to that set and return the total as a single initialization data map. The default function is `identity`.

The client side dispatches two calls upon reception of this message. Both are passed this final initialization data map. The first is `hc/register` which takes the standard fields and sets up the initial client side database. The second is the multimethod [user-msg](#user-msg) where the msg given is `{:op :app-init :data init-data-map}`. If your application implements this method for `:app-init`, it will be called and any application specific work can be done at that point.


### Session group

As described in the section on [sessions](#sessions) and session groups, the client may change the group its session belongs to (named by the `idefn` function of [start-server](#server-start)) as described in the section on [connections](#connection). This is achieved by sending the server a `:set-session-name` message, which has the form:

```Clojure
{:op :set-session-name
 :data {:uid old-uid
        :new-name name}}
```

Where

* `old-uid` is the uid the session currently has and wants shifted
* `name` is the name of the session group the client wants to move to.

Upon receipt of such a message, the server will update its database to reflect the change: the session's `uuid` will be moved to the requested group named by the `:new-name` field.


### Tab updates

The `:tabs` message is the main way to update one or more tabs in a current sesion if you are using the [tab system](#tabs) in a client / server setup. The simplest way to send these messages is via the server [sv!](#server-core) function. Typically this will be from within a server side editor / IDE.

In [client only](#client-only-apps) applications, you can achieve the exact same effect by calling the client side 'mirror' function [sv!](#tab-system).

 As an example, the [simple cars](#simple-cars) case listed in the opening [examples](#examples) was dispatched as:

```Clojure
(-> (hc/xform ht/point-chart
      :UDATA "data/cars.json"
      :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
    hmi/sv!)
```


### User messages

User, also know here as "application specific", messages are sent via the [send-msg](#send-msg) function. This function exists in both the client and server `hmi` name space. The entire point of these messages is to support extension messages that are specific to the needs of an application. All such messgaes will be caught by the receiving party and dispatched to the multimethod [user-msg](#user-msg). There is one such message which is dispatched on the client side implicitly - the [:app-init](#connection) msg on client connection. This supports the any application specific client initialization processing.

Two examples of from [Saite](https://github.com/jsa-aerial/saite)

The [first](https://github.com/jsa-aerial/saite/blob/9aaacc3463604cfd03d239600dc859c48d6c27c7/src/cljs/aerial/saite/core.cljs#L258) is for the `:app-init` message.

```Clojure
(defmethod user-msg :app-init [msg]
  (update-adb [:main :convert-chan] (async/chan))
  (add-tab {:id :xvgl
            :label "<->"
            :opts {:extfn (tab<-> :NA)}}))
```

So, we add a Saite specific async channel to the database in support of conversions and popup renderings. Then we add the [extension tab](#extension-tabs) for the conversion / popup renderer capability.

The second ([client](https://github.com/jsa-aerial/saite/blob/9aaacc3463604cfd03d239600dc859c48d6c27c7/src/cljs/aerial/saite/core.cljs#L245) / [server](https://github.com/jsa-aerial/saite/blob/9aaacc3463604cfd03d239600dc859c48d6c27c7/src/clj/aerial/saite/core.clj#L81) is for messages specifically required by Saite to perform data conversion and transformation operations for the client.

```Clojure
;;; Client sends two versions of :read-clj msg to server.
;;;  One for conversion and one for popup rendering
;;; The popup case
(let [...
      msg {:op :read-clj
           :data {:session-name nm
                  :render? true
                  :cljstg inspec}}
      _ (send-msg msg)
      ...]
      ...
      )

;;; Server user-msg method for this:
(defmethod hmi/user-msg :read-clj [msg]
  (let [{:keys [session-name cljstg render?]} (msg :data)
        clj (try
              (let [clj (->> cljstg clojure.core/read-string)]
                (swap! dbg (fn[m] (assoc m :clj clj)))
                (->> clj xform-cljform eval final-xform))
              (catch Exception e
                {:error (format "Error %s" (or (.getMessage e) e))})
              (catch Error e
                {:error (format "Error %s" (or (.getMessage e) e))}))]
    (swap! dbg (fn[m] (assoc m :xform-clj clj)))
    (hmi/print-when [:pchan :umsg] :CLJ clj)
    (hmi/send-msg session-name :clj-read (assoc clj :render? render?))))

;;; NOTE the send-msg to client with :cli-read

;;; Client user-msg method for this:
(defmethod user-msg :clj-read [msg]
  (let [data (msg :data)
        render? (data :render?)
        clj (dissoc data :render?)
        result (if render?
                 clj
                 (try (-> clj clj->js (js/JSON.stringify nil, 2))
                      (catch js/Error e (str e))))
        ch (get-adb [:main :convert-chan])]
    (go (async/>! ch result))))
```


## Picture Frames

Picture frames are simply a way to automatically encase your visualizations with 'meta' level instrumentation and / or arbitrary annotations. They are composed of four individual parts corresponding to the top, bottom, left, and right quadrants. Picture frames (or simply frames) are specified in the `usermeta` data of a specification via the `:frame` key. By default Hanami uses the substitution key `:USERDATA` for this. So, the format is:

```Clojure
{...
 :USERDATA {...
            :frame {:fid ...
                    :top ...
                    :bottom ...
                    :left ...
                    :right ...}
            ...
            }
}
```

![Hanami picture frame](resources/public/images/picture-frame-layout.png?raw=true)

All of the quadrants are optional and `:frame {}` is legal, which by the third [rule of transformation](#basic-transformation-rules) will result in its removal.

The value of `:fid` should be a globally unique id (keyword or string). If it is not specified the field will be removed and the frame will not have an id. This id will be used as the frame's DOM id as well as the corresponding spec's id. So, if it is not supplied the frame will not be accessible (in particular, it won't be accessible when using [incremental bodies](#incremental-bodies).

The value of a quadrant can be any legal mix of strings, hiccup, markdown (MD) and / or active components. Where 'legal' here means 'yields a legal DOM branch'. A great resource for active components, which Hanami provides as part of its package, is [Re-Com](https://github.com/Day8/re-com). This is especially true if you are not a CSS/flex and / or component savant.

As of version 0.5.0, picture frames may be ['empty'](#empty-frames) in that they do not need to have an associated visualization. To make this a bit simpler, there is a new template for these cases `ht/empty-chart`.

As of version 0.9.0, markdown is directly available via the `md` active component, as such it is an extension to the hiccup used by Hanami (from [Reagent](http://reagent-project.github.io/)). You would use this like / where you would use any other hiccup / re-com component. The form is `[md <optional style map> <string of markdown>]`. The string can include newlines. The style map is for things such as font-size, color, etc.

**ASSIDE**: LaTex is specifically _not_ included in Hanami. This decision is based on three reasons: 1. Many (most?) applications do not need LaTex math rendering.  2. [MathJax](https://www.mathjax.org/) is the go to JS engine for this and is a heavy dependency that would not be used in these applications.  3) React (as of version 16+) has a 'bug' preventing proper running of engines like MathJax (and Google Translate and many many other such rendering engines). It is possible to get around issue 3. but it requires 'monkey patching' React. If you need LaTex, you should consider using [Saite](https://github.com/jsa-aerial/saite) which is an application (built on Hanami) which does include MathJax, fixes 3., and is an application for general graphics and live document creation and sharing. Of course, if you are creating your own domain specific application and need LaTex for it, you will need to navigate issue 3. in your app. However, **note** that the `md` tag (see above markdown discussion) supports LaTex, so you can include it in your markdown (again, see Saite for examples).

You can specifiy frames from either the server or client side of your application. Working on the client side from within ClojureScript can make this more 'natural', as you are in the actual environment (browser/DOM) where things are running and rendering. However, specifying from the server side is fully supported, as long as Re-Com use is quoted (or more typical and useful, backquoted).

**NOTE**: if you are _instrumenting_ your visualization (using active components - input box, dropdowns, selection lists, etc.) the _functions_ updating the relevant model(s) of / for these components _must_ be written over on the ClojureScript side (client). This is because, Hanami does not use a self hosted ClojureScript, but rather the cross compiled (with Google Closure) variant. Hence, you cannot, for example, write function code on the server side and have it eval'd on the client! (_NOTE_: this restriction may be lifted!)

Picture frames are fully automatic if you use the default tab system. If you use custom tabs and want to make use of frames, you should call [hmi/vis-list](#client-core) on the client side (Cljs) for automatic rendering. If you are doing completely custom layouts, you will want to call [hmi/frameit](#client-core) client function. As always, if you do not want to use any of this, you should 'manually' use the [hmi/vgl](#client-core) reagent vega/vega-lite component.

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
          :TID :picframes
          :TOP top :BOTTOM bottom :LEFT left :RIGHT right
          :UDATA "data/cars.json"
          :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")]
       hmi/sv!))
```

![Hanami picture frame](resources/public/images/picture-frame-all-quads-ex.png?raw=true)


### Empty Frames

As of version 0.5.0, picture frames can now be 'empty', i.e., they do not need an associated visualization. The basic layout is the same as for frames with visualizations:

![Hanami empty frame](resources/public/images/empty-picframe-layout.png?raw=true)

To make the use of these simpler (and easier) there is a new standard template `ht/empty-chart` which can be used to create them. Empty frames enable a more general document structure supporting paragraphs of html/'text oriented' material along side or in place of standard visualizations with or without frames. Here are a couple of examples showing the basic capability:

This example shows an empty frame with all 4 picture frame elements (areas) with various html/text information

```Clojure
(let [text "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsum, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."
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
                 [p {:style {:width "400px" :min-width "50px"
                             :font-size "20px"}}
                  "some TeX: " "\\(f(x) = \\sqrt x\\)"]
                 [md {:style {:font-size "16px" :color "blue"}}
                  "#### Some Markup
* **Item 1** Lorem ipsum dolor sit amet, consectetur adipiscing elit.
* **Item 2** Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quod si ita est, sequitur id ipsum, quod te velle video, omnes semper beatos esse sapientes. Tamen a proposito, inquam, aberramus."]
                 [p {:style {:width "600px" :min-width "50px"
                             :color "red"}}
                  ~text]]]]]
  (->> (hc/xform ht/empty-chart
        :TID :picframes :TOP top :BOTTOM bottom :LEFT left :RIGHT right)
       hmi/sv!))
```

![Hanami empty picframe](resources/public/images/picframe-empty.png?raw=true)

This next eample shows a tab/page with two picture frames. The left being a frame with an associated chart, while the one on the right shows how you can structure text in columns.

```Clojure
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
          :TOP top
          :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
        (hc/xform ht/empty-chart
          :TID :picframes
          :LEFT left :RIGHT right)]
       hmi/sv!))
```

![Hanami picframe chart and empty](resources/public/images/picframe-chart-and-empty.png?raw=true)


## Data Streaming


## Client Only Apps

While the most typical use case scenario involves clients and server working together, Hanami also enables the development of client only applications. In this case, the Clojure (server) side of the library (and framework aspects) is not used and all development is done on the client (typically browser) side.

In client only applications, there are no explicit messages, nor is the usual client side [start function](#client-start) used. Application initialization is no longer implicit (via the server to client [register](#connection) message); you will need to explicitly code your own specific initialization.

As noted in [resource requirements](#resource-requirements), you still need to create a landing page (`index.html`) with all the required resources and to have your directory structure setup for relative loading or use absolute URLs in the links.

When running client only applications, the full [template system](#templates-substitution-keys-and-transformations) is available on the client side. Both the `hc` and `ht` namespaces may be required and their resources used. So, you can make full use of templates, substitution keys, and transformations. Further, all the default templates are available for use.


### Basic client app requirements

#### Require resources

You need to require the basic Hanami resources in your name space definition

```Clojure
(ns clientex.core
  (:require
  ...
   [aerial.hanami.core
    :as hmi
    :refer [printchan user-msg
            re-com-xref xform-recom
            default-header-fn default-instrumentor-fn make-instrumentor
            update-adb get-adb
            init-tabs
            hanami-main]]
   [aerial.hanami.common :as hc :refer [RMV]]
   [aerial.hanami.templates :as ht]
   ...))
```

The exact resources you refer will be up to how you typically do such referals. You may just use the `:as hmi` and reference all resources with `hmi/...`. If you want to use [re-com](https://github.com/Day8/re-com) capabilities, you will need to require those as well. For example, something like

```Clojure
 [re-com.core
    :as rcm
    :refer [h-box v-box box gap line h-split v-split scroller
            button row-button md-icon-button md-circle-icon-button info-button
            input-text input-password input-textarea
            label title p
            single-dropdown
            checkbox radio-button slider progress-bar throbber
            horizontal-bar-tabs vertical-bar-tabs
            modal-panel popover-content-wrapper popover-anchor-wrapper]]
```

But again, what you require and refer will be up to what you need for your application and how you want to structure it.

#### Application initialization

You will need to code your application setup and initialization functions. If you use the tab system you will need to call [init-tabs](#tab-system) as part of this. If you want to use [hanami-main](#hanami-main) you will also need to setup the database to have the expected fields. The [example client only app](https://github.com/jsa-aerial/hanami/tree/master/examples/ClientOnly) does this [here](https://github.com/jsa-aerial/hanami/blob/3a0e58d4342b499143ac6685b2bd29ca43750157/examples/ClientOnly/src/cljs/clientex/core.cljs#L44)

#### Landing page - index.html

The most typical way of getting the application loaded is via an explict `index.html` file which is correctly located in the directory `resources/public` of your application. The example application uses this [index.html](https://github.com/jsa-aerial/hanami/blob/master/examples/ClientOnly/resources/public/Fig/index.html)

Once you have your code compiled and located per the `<script ...>` tag in your `index.html`, you can simply use a browser to navigate to this file and your application will be active. If you want to have an interactive session(s) with the application you will need to run [figwheel](https://github.com/bhauman/lein-figwheel) or an equivalent type of capability. The client only [example app](https://github.com/jsa-aerial/hanami/tree/master/examples/ClientOnly) is set up to use figwheel (plus cljs-repl and piggyback)

#### Application start

Your code should have a final block which will start the application upon page load. Generally this will test for a specific DOM element upon which to render (via reagent) your main component. An example of this from the client only example is [here](https://github.com/jsa-aerial/hanami/blob/3a0e58d4342b499143ac6685b2bd29ca43750157/examples/ClientOnly/src/cljs/clientex/core.cljs#L76). In this case the startup first sets several default [substitution keys](#example-predefined-substitution-keys), then calls [app-init](https://github.com/jsa-aerial/hanami/blob/3a0e58d4342b499143ac6685b2bd29ca43750157/examples/ClientOnly/src/cljs/clientex/core.cljs#L62) which sets up the application. At this point you will be able to render visualizations just like from the server, using the same [hc/xform](#templates-and-substitution-keys) transformer of templates, and [hmi/sv!](#tab-system)


### Example Client Only Application

Hanami's github repository contains a client only example application. Found in the `examples` directory [here](https://github.com/jsa-aerial/hanami/tree/master/examples/ClientOnly). To run this you can simply change directory to the `examples/ClientOnly` directory. Then run `lein repl`. When repl is up and running, issue `(use 'figwheel-sidecar.repl-api)` and when ready issue `(start-figwheel!)`. This will open a browser window and you will see:

![Hanami pic 1](resources/public/images/client-only-example-app.png?raw=true)

You can also, at this point, pull up the `clientex.core.cljs` in emacs, connect to the repl, and once connected issue `(cljs-repl)`. Switch buffers to the `clientex.core` name space file, and evaluate the buffer. At this point you will be able to interactively transform and render other specifications (see the 'rich' [comment](https://github.com/jsa-aerial/hanami/blob/6d52c0cc27894969aab0597362f5a1f04572d654/examples/ClientOnly/src/cljs/clientex/core.cljs#L108) for some other examples)




## API

As noted, with respect to abstracting visualizations (ala' something like [Altair](https://altair-viz.github.io/)) there isn't much of an API and no classes, objects, or methods are involved. Most of what API there is centers on application development and start up. This is split across the server and the client side.


### Templates and Substitution keys

This applies across both the server and client - the facilities are available in both server and client. They are in `aerial.hanami.common`, in this documentation aka `hc`. **NOTE** this namespace, along with `aerial.hanami.templates` (aka `ht`), is available on _both_ the client and server.

* `(defn update-subkeyfns [k vfn & kvfns])`: Updates the [substitution key function map](#subtitution-key-functions). `k` is a substitution key, `vfn` is either a function `(fn [submap, subkey, subval] ...)` or the special `hc/RMV` value. The latter case will _remove_ any current key `k` and its value.

* `(defn update-defaults [k v & kvs])`: Updates the default [substitution key map](#example-predefined-substitution-keys). `k` is a substitution key, `v` is some appropriate value including `hc/RMV`.  To remove a key from the global register use the namespace key `::ht/RMV` = `:aerial.hanami.templates/RMV` for `v` which will remove the key.

* `(defn get-default [k])`: Returns the current default value of substitution key `k`.

* `(defn get-defaults [k & ks])`: Returns the current default value of substitution key `k` or a vector of [k v] pairs if given two or more ks.

* `(defn xform ([spec submap]) ([spec k v & kvs]) ([spec]))`: The template/specification transformation function. Generally, you will only call the second two arity signatures. The first is used for recursive transformation actions, so in the first signature, `spec` is generally the current value of a recursive transformation and submap the current state of the transformation.

  In the second and third signatures, `spec` is a template. [Recall](#templates-substitution-keys-and-transformations) legal templates may be already complete fully realized Vega or Vega-Lite specifications - this latter case is for what the third signature is intended. The second signature is what you will use in nearly all cases. `k` is a substitution key and `v` is the value it will have in the transformation. Hence you can override defaults and [template local defaults](#template-local-defaults) and add specific keys for just a given transformation. Returns the fully transformed final value.

  There are currently (as of V0.15.1) three transformation control keys.  These keys affect the transformation process by changing the flow or state of the transformation.

    - `:aerial.hanami.common/use-defaults?` : if given as `true` (the default value) the global register is used as a starting set of default substitution keys and values.  If `false` only the in line kvs and any template local defaults are used.

    - `:aerial.hanami.common/rmv-empty?` : If `true`, the default, follow the [third rule](#basic-transformation-rules) of transformations.  If `false` do **not** remove empty collections.

    - `:aerial.hanami.templates/defaults` : If a template (actually, any map) has this key defined, the value map for it is merged into the current state.  This effects the behaivor of [template local defaults](#template-local-defaults)



### Startup

There are two main start functions. One each for the server and client.

#### Server start
```Clojure
(defn start-server
  [port & {:keys [route-handler idfn title logo img connfn]
           :or {route-handler (hanami-handler (hanami-routes))
                idfn (partial gensym "hanami-")
                connfn identity
                title "花見 Hanami"
                logo "logo.png"
                img "Himeji_sakura.jpg"}}]
  ...)
```

* `port` the port on which to open the websocket messaging system

* `:route-handler` a function for handling http route requests. Hanami uses http-kit and this will be the function passed as the `app` argument to its `run-server`. Currently Hanami directly supports building routes via [compojure](https://github.com/weavejester/compojure), though supporting [bidi](https://github.com/juxt/bidi) is being considered. There are three ancillary functions to support users in creating their application routes.

  - `(defn landing-page [request index-path] ...)` The `request` argument is an http request map, but is not used. `index-path` is the resource path to your `index.html` landing page. Returns a Ring response map: `(content-type {:status 200, :body (io/input-stream (io/resource index-path))}, "text/html")`

  - `(defn hanami-routes [& {:keys [landing-handler index-path]
                             :or {landing-handler landing-page
                                  index-path "public/index.html"}}] ...)` Creates a set of routes that uses `(landing-handler request index-path)` as the value of the `get /` route, and adds the necessary websocket routing to this and finally adds the default resources route `(compojure.route/resources "/")`. Returns the resulting function implementing the routes. Uses `compojure.core/routes` to create the 'rounting function'.

  - `(defn hanami-handler [routes-fn & middle-ware-stack] ...)` Takes a routing function `routes-fn` (as built by `hanami-routes`) and zero or more ring middle-ware wrapping functions. Returns the full wrapped site handler function.

* `:idfn` Function of zero paramters. The [session group](#sessions) name generator for connections

* `:connfn` Function of one parameter (fn [x] ...) where x is the map of standard fields and values of a [connection registration](#connection). Should compute any necessary application specific registration data. This data will be available to the `:app-init` method of [user-msg](#user-messages) multimethod.

* `:title` A application appropriate title. Can be used by the [client start](#client-start) `header-fn`. The default header function of the client uses this to make a title in the [application header area](#framework-topology-graphic)

* `:logo` A glyph/avatar for use in header function processing

* `img` An image resource. Again for use in application initialization as background or other such use.


#### Client start
```Clojure
(defn start [& {:keys [elem port header-fn instrumentor-fn frame-cb]
                :or {header-fn default-header-fn
                     instrumentor-fn default-instrumentor-fn
                     frame-cb default-frame-cb
                     symxlate-cb identity}}]
  ...)
```

* `:elem` the DOM element on which the main Reagent component will be rendered. For example `(js/document.querySelector "#app")`. If using the framwork default, this will be the element on which [hanami-main](#hanami-main) is rendered.

* `:port` the port to make websocket connection with. Generally as part of your page load start code, this port will be determined by `:port js/location.port`.

* `:header-fn` a parameterless function which is intended to perform layout of an application's [header](#header) [area](#framework-overview)

* `:instrumentor-fn` A function `(fn[{:keys [tabid spec opts]}] ...)` which is used to implement custom external instrumentation for visualizations. Instrumentation are active components and typically will be re-com components. See [barchart example](#instrumented-barchart) for an example. Also, CLJS `aerial.hanami.core` has a 'rich' [comment](https://github.com/jsa-aerial/hanami/blob/ee5556d41033a956ef07d3971f9a506e1465fef9/src/cljs/aerial/hanami/core.cljs#L694) which gives a couple full examples. The `default-instrumentor-fn` is a no-op.

* `:frame-cb` A function `(fn ([]...) ([spec frame] ...))` which used to implement post processing of frames. The `[]` case is called on component update. The two arity case is called by [frameit](#visualization) and passed the `spec` and `frame map` containing the frame elements that will be built into a single frame and `:frameid` whose value is the HTML id for the frame. It must passback a `frame map`, which can be the input or a new one with some adjustments. Generally this is used to set up post _mounting_ processing (DOM mutation...) and the input is simply returned. The zero arity case is strictly intended for post mount processing. As an example, [Saite](https://github.com/jsa-aerial/saite#user-tabs) uses this to render LaTex via MathJax.

* `:symxlate-cb` A function `(fn ([sym] ...)` which will be called during translation of symbols in hiccup/re-com vectors (typically from server, but could be client only as well). `sym` is a symbol in such a vector which does _not_ exist in the default translation table. The return should be an application specific value (normally a function defined in the application) or `sym` if no such value exists for the application. The idea here is that users may add extra Reagent _components_ which can then be used in picture frame construction. So, you can use the symbol as denoting a legal component and it will be replaced by the value returned by your `symxlate-cb` function. This will then be used during overall rendering.


### Message system

This applies across both the server and client - the facilities are available in both server and client. They are in `aerial.hanami.core`, in this documentation aka `hmi`. **NOTE** this namespace, exists on _both_ the client and server.

#### send msg

* Server: `(defn send-msg ([to app-msg] ...) ([to op data] ...))`
  - `to` is one of
     - string naming an existing [session group](#sessions)
     - string naming an active Hanami uuid, a [session uuid](#sessions)
     - an active client websocket object

  - `app-msg` is
     - an application specific message with form `{:op opkey, :data data-value}`
       - `opkey` is a msg specific operator
       - `data-value` is some arbitrary data - typically a map of fields

  - `op` is an `opkey` and `data` a `data-value`. This form is converted to a call `(send-msg to {:op op, :data data})`


* Client: `(defn send-msg ([app-msg] ...) ([op data] ...))`
  - `app-msg` is
     - an application specific message with form `{:op opkey, :data data-value}`
       - `opkey` is a msg specific operator
       - `data-value` is some arbitrary data - typically a map of fields

  - `op` is an `opkey` and `data` a `data-value`. This form is converted to a call `(send-msg to {:op op, :data data})`

In both cases, the _receiving_ party will have their [user-msg](#user-msg) multimethod dispatched on the `msg op key`.


#### user msg

* Client and Server `(defmulti user-msg :op)`
  Multimethod for encoding application specific message envelopes (see [Hanasu](https://github.com/jsa-aerial/hanasu) for general details). Specifically, calls with appropriate message arguments to [send-msg](#send-msg) on either the client or server will produce a dispatch in the corresponding party to this multimethod. Intent and purpose of these is to support domain semantics of specific applications. See for example, [Saite](https://github.com/jsa-aerial/saite).


### Client core

#### Visualization

* `(defn visualize [spec elem] ...)`: Function used by `vgl` reagent component to create Vega and Vega-Lite visualizations.
  - `spec` is a Vega or Vega-Lite specification _as Clj/EDN_ which must have a `:usermeta` field with at least the [opts](#meta-data-and-the-userdata-key) field whose value must include at least the `:mode`.
  - `elem` is the DOM element into which the visualization will be inserted.

* `(defn vgl [spec] ...)`: Reagent [Form-3](https://github.com/reagent-project/reagent/blob/master/doc/CreatingReagentComponents.md) which has life cycle methods for mounting, updating, and rendering a visualization.
  - `spec` is a Vega or Vega-Lite specification _as Clj/EDN_ which must have a `:usermeta` field with at least the [opts](#meta-data-and-the-userdata-key) field whose value must include at least the `:mode`.

* `(defn frameit [spec frame] ...)`: Embed visualization inside a [picture frame](#picture-frames).
  - `spec` is a Vega or Vega-Lite specification _as Clj/EDN_ which must have a `:usermeta` field with at least the [opts](#meta-data-and-the-userdata-key) field whose value must include at least the `:mode`.
  - `frame` is the complete prebuilt picture frame with all 4 components: `:top`, `:bottom`, `:left`, and `:right`. One or more of these may be visually empty (for example, `[]` for `:top` and `:bottom` and `[[box :size "0px" :child ""]]` for `:left` and `:right`.

* `(defn vis-list [tabid spec-frame-pairs opts] ...)`: Function for laying out [tab](#tabs) bodies. Used for both standard and [extension tabs](#extension-tabs) Implicitly called by via reactive update events. May be manually called.


#### Tab system

* `(defn get-tab-field ([tid] ...) ([tid field] ...))`: For `[tid]` return entire tab value associated with tab id `tid`. For `[tid field`, return the field value associated with key `field` in tab value associated with tab id `tid`.

* `(defn update-tab-field [tid field value] ...)`: Change (or add) the value of the key `field` in tab value associated with tab id `tid` to the value `value`.

* `(defn get-cur-tab ([]...) ([field]...))`: For `[]` return the tab value of the current tab. For `[field]`, return the value of the key `field` in current tab.

* `(defn set-cur-tab [tid] ...)`: Change the current tab to be that associated with tab id `tid`

* `(defn update-cur-tab [field value] ...)`: Update the value of the key `field` to the value `value` in the current tab

* `(defn add-tab [tabval] ...)`: Add a tab to the tab bar. `tabval` is a complete tab value:
```Clojure
{:label <tab label name>
 :id <tab id>
 :opts <full options - see hc/default-opts>
 :specs <current set of specs for tab body>
 :spec-frame-pairs <current set of (spec,frame) pairs rendered in body>
 }
```

* `(defn replace-tab [tid newdef] ...)`: Like `add-tab`, but replaces the tab value associated with tab id `tid` with `newdef`.

* `(defn del-tab [tid] ...)`: Delete the tab associated with tab id `tid`. If this is the current tab, set current tab to the first remaining tab. If this deletes the only tab, remove tab bar.

* `(defn add-to-tab-body [tid picframe & {:keys [at position opts] :or {at :end position :after opts {}}}] ...)`: Adds to tab id `tid`'s body (content area) a new specification (with a defined picture frame) `picframe` at location `at`, either an existing frame id or special designator `:end`, positioned `position` (either `:before` or `:after`) relative to `at`. If `opts` is supplied it must be a map containing [tab options](#meta-data-and-the-userdata-key) which will override any existing options for the tab. The content area will properly reflect the grid layout for the tab. **Note**, if `opts` specifies a new grid layout, it will be used.

* `(defn remove-from-tab-body [tid fid] ...)`: Removes the picture frame with frame id `fid` from the body (content area) of tab with id `tid`.

* `(defn init-tabs [] ...)`: Initialize the tab system. This is called implcitly when using the client [start](#client-start), via the [:register](#connection) message. If you are building a client only application, and want to use the tab system, you need to call this as part of your initialization.

* `(defn active-tabs [] ...)`: **Reagent component** that sets up the tab bar area. The content area will properly reflect the grid layout for the tab.

* `(defn tabs [] ...)`: **Reagent component** for driving the tab system. This is called impicitly when using [hanami main](#hanami-main) Reagent component. In particular, drives the update, rendering, and display of tab bodies. May be called manually or part of a different main component.

* `(defn update-tabs [specs] ...)`: Function to update the set of tabs. This includes adding new tabs to the set. `specs` is a vector of specifications, each of which must have a [:usermeta](#meta-data-and-the-userdata-key) field with associated `:tab` key and value, which must contain an `:id` field (see [USERDATA](#meta-data-and-the-userdata-key) for details). The specifications in `specs` do **not** need to have the same tab given - they may indicate a mix of tabs. As such, `update-tabs` first groups the specifications by tab id, preprocesses the specs in each set and updates the tab database to reflect the changes. This database update triggers he Reagent components `active-tabs` and `tabs` to fire and render the changes. Typically, in server based applications, this function is implicitly called due to the reception of [:tabs](#tab-updates) messages. For [client](#client-only-apps) only applications, this may be explicitly called, thus invoking the tab system machinery or more cleanly invoked by using the client side `sv!` which mirrors the server [sv!](#server-core).

* `(defn sv! [specs] ...)`: Invokes `update-tabs` after ensuring `specs` is a vector of specifications (`spec` can be a single specification and it will be wrapped in a vector). Provided in order to keep client and server side tab updating the same in look and feel.


#### Hanami main

The main (top level) Reagent component for driving an application using the standard [framework aspects](#framework-overview)

* `(defn hanami-main [] ...)`: Top level **Reagent component**. Renders via `reagent/render` as part of standard [registration](#connections). This component is the main driver for stanard framework usage. It assumes the standard framework [structure](#framework-topology-graphic) and therefore also assumes that the `app-db` has been initialized according to the [register](#connection) function.

Creates a Re-Com [v-box](https://re-com.day8.com.au/#/v-box) consisting of the framework `header` (using the value of calling the client start [header](#header) function), `tab bar` area (via the [active-tabs](#tab-system) component, and `tab body` area (via the [tabs](#tab-system) component). Each is separated by a [line](https://re-com.day8.com.au/#/line).

If you are writing a [client only](#client-only-apps) application you could render this as part of your applications initialization if you want to use Hanami framework capabilities.


### Server core

* `(defn sv! [specs] ...)`: `specs` is a single specification (which will be wrapped in a vector before processing) or a vector of specifications, each of which must include [:usermeta](#meta-data-and-the-userdata-key) data with full [tab data](#tabs), where the [msgop](#meta-data-and-the-userdata-key) must be `:tabs` and the [session-name](#sessions) must be appropriately set. Specifications must be fully transformed! Constructs a [:tabs](#tab-updates) message and sends to client, which will add (or update) the bodies of the tabs to reflect the specifications given to them.


# Example Transform 'Gallery'

A gallery of visualization examples exploiting the abstraction power of Hanami's templates and recursive transformations. This will likely keep growing as neat new things are built.

** If anyone has their own nice examples that they would like to pass along, please don't hesitate to submit a PR!!**  Thanks!

## Observed vs Binomial Model

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


## Lowess smoothing of Tn-Seq fitness data

The next is a visualization for an investigation into using lowess smoothing of Tn-Seq fitness data.

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


### With Overview + Detail

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


## Interactive Cross plot Differential Gene Expression

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

