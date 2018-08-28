# hanami

Interactive arts and charts visualizations with Clojure(Script), Vega-lite, and Vega. Flower viewing 花見 (hanami)

<a href="https://hanami.github.io"><img src="https://github.com/jsa-aerial/hanami/blob/master/resources/public/Himeji_sakura.jpg" align="left" hspace="10" vspace="6" alt="hanami logo" width="150px"></a>


**Hanami** is a Clojure(Script) library and application for creating interactive visulaizations based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer).

In keeping with this central data oriented tenet, Hanami eschews the typical API approach for generating specifications in favor of using recursive transforms of parameterized templates. This is also in keeping with the data transformation focus in functional programming, which is espcially nice in Clojure(Script).

An important aspect of this approach is that parameterized templates can be used to build other such templates by being higher level substitutions. In addition templates can be composed and this is another important idiomatic use. The result in examples becoming drop in reusable code.


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



## Templates

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
In the above transform we specified values for `:UDATA`, `:X`, `:Y`, and `COLOR`. First, none of these are anywhere to be seen in `ht/simple-point-chart` so where do they come from? Second, what happened to all those other fields like `:usermeta` and those values like `:SELECTION`. Both questions have answers rooted in the map of defaults for transformations. There is nothing special about these defaults and a user can completely change them if they do not like the key names or their values. However, out of the box, here is a subset of these options that answer our two questions and where some other values came from:

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

As noted, there isn't much of a functional/procedural API and no objects or protocols (classes/interfaces) are involved. There are three primary functions. One on the server side, one on the browser/client side and one common to both. There are hanful of other ancillary functions common to both sides involving the abiltiy to change default substitution map.

### Primary

#### Common

In name space `aerial.hanami.common`

```Clojure
(xform
  ([x xkv] ...)
  ([x k v & kvs] ...))
```

This is the recursive transformation function. `x` is a _template_ (see above). In both the 2 and 3+ argument cases, the remaining arguments involve providing _substitution keys_ and values. In the two argument case these are supplied as a `map`, while the 3+ argument case you provide them in the usual key/value pair `rest` style of clojure. Each key should correspond to a _substitution key_ (see above in **Templates**) while the value will be what is inserted into a template during the transformation sequence.

