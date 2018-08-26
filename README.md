# hanami

<a href="https://altair-viz.github.io"><img src="https://github.com/jsa-aerial/hanami/blob/master/resources/public/Himeji_sakura.jpg" align="left" hspace="40" vspace="6" alt="Altair logo" width="150px"></a>

Interactive arts and charts visualizations with Clojure, Vega-lite and Vega. Flower viewing 花見 (hanami)*

**Hanami** is a Clojure(Script) library and application for creating interactive visulaizations based in [Vega-Lite](https://vega.github.io/vega-lite/) (VGL) and/or [Vega](https://vega.github.io/vega/) (VG) specifications. These specifications are declarative and completely specified by _data_ (JSON maps). VGL compiles into the lower level grammar of VG which in turn compiles to a runtime format utilizting lower level runtime environments such as [D3](https://d3js.org/), HTML5 Canvas, and [WebGL](https://github.com/vega/vega-webgl-renderer).


## Installation

To install, add the following to your project `:dependencies`:

    [aerial.hanami "0.2.0"]


