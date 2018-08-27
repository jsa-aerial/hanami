# hanami

Interactive arts and charts visualizations with Clojure(Script), Vega-lite, and Vega. Flower viewing 花見 (hanami)

<a href="https://hanami.github.io"><img src="https://github.com/jsa-aerial/hanami/blob/master/resources/public/Himeji_sakura.jpg" align="left" hspace="10" vspace="6" alt="hanami logo" width="150px"></a>


**Hanami** is a Clojure(Script) library and application for creating interactive visulaizations based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer).

With parameterized templates, examples become reusable code.

## Installation

To install, add the following to your project `:dependencies`:

    [aerial.hanami "0.2.0"]

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
(hc/xform ht/simple-point-chart
  :UDATA "data/cars.json"
  :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
```

Transforms to:

```Clojure
{:data {:url "data/cars.json"},
 :width 400,
 :background "floralwhite",
 :height 300
 :encoding
   {:x {:field "Horsepower", :type "quantitative"},
    :y {:field "Miles_per_Gallon", :type "quantitative"},
    :color {:field "Origin", :type "nominal"},
    :tooltip
    [{:field "Horsepower", :type "quantitative"}
     {:field "Miles_per_Gallon", :type "quantitative"}]}}
 ```

And when sent to a view, results in:

![Hanami pic 1](resources/public/images/hanami-cars-1.png?raw=true)


## API

