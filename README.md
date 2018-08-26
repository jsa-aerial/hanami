# hanami

Interactive arts and charts visualizations with Clojure(Script), Vega-lite, and Vega. Flower viewing 花見 (hanami)

<a href="https://altair-viz.github.io"><img src="https://github.com/jsa-aerial/hanami/blob/master/resources/public/Himeji_sakura.jpg" align="left" hspace="10" vspace="6" alt="hanami logo" width="150px"></a>


**Hanami** is a Clojure(Script) library and application for creating interactive visulaizations based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer).

With parameterized templates, examples become reusable code.

## Installation

To install, add the following to your project `:dependencies`:

    [aerial.hanami "0.2.0"]

## Features

* Parameterized templates with recursive transformations
  * Takes the place of the typical static procedural/functional API
  * Completely open ended - users may define their with their own defaults
  * More general in scope than an API and arbitrarily more specific in detail
* Named visulization groupings (tabbed navigation)
* Multiple simultaneous independent and dependent visulizations per grouping
* Enables specific application construction as lib
  * Application level page header instrumentation (re-com enabled)
  * Application level external instrumentation of charts (re-com enabled)
  * Multiple simultaneous (named) applications
  * Multiple sessions per application
  * Streaming data capable - user extensible messages
* Default application for exploratory work
* Built on light weight websocket general messaging system


