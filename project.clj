(defproject aerial.hanami "0.12.1"
  :description "Clojure(Script) client/server vega/vega-lite based dynamic interactive plots and charts"
  :url "https://github.com/jsa-aerial/hanami"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.439"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/data.csv "0.1.3"]

                 [ring/ring-defaults "0.3.1"]
                 [bk/ring-gzip "0.2.1"]
                 [ring-cljsjs "0.1.0"]

                 [com.rpl/specter "1.1.3"]

                 [aerial.hanasu "0.2.4"]

                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [re-com "2.5.0"]
                 [reagent "0.8.1"]

                 [cljsjs/vega "5.4.0-0"]
                 [cljsjs/vega-lite "3.4.0-0"]
                 [cljsjs/vega-embed "4.0.0-0"]
                 [cljsjs/vega-tooltip "0.18.1-0"]
                 ]

  :plugins [[lein-figwheel "0.5.16"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src/cljc" "src/clj" "src/cljs"]

  :prep-tasks ["compile" ["cljsbuild" "once" "min"]]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :jar true

                ;; The presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "aerial.hanami.core/on-js-reload"
                           ;; :open-urls will pop open your application
                           ;; in the default browser once Figwheel has
                           ;; started and compiled your application.
                           ;; Comment this out once it no longer serves you.
                           :open-urls #_["http://localhost:3449/indexH.html"]
                           ["http://localhost:3003"]}

                :compiler {:main aerial.hanami.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/hanami.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           ;; To console.log CLJS data-structures make
                           ;; sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}
               ;; This next build is a compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/compiled/hanami.js"
                           :main aerial.hanami.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this

             ;; doesn't work for you just run your own server :) (see lein-ring)

             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads
             ;; up display you will need to put a script on your path.
             ;; that script will have to take a file path and a line
             ;; number ie. in ~/bin/myfile-opener #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you are using emacsclient you can just use
             ;; :open-file-command "emacsclient"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"

             ;; to pipe all the output to the repl
             ;; :server-logfile false
             }

  ;; Setting up nREPL for Figwheel and ClojureScript dev
  ;; Please see:
  ;; https://github.com/bhauman/
  ;;   (uri cont'd) lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.16"]
                                  [cider/piggieback "0.3.3"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   :repl-options {:nrepl-middleware
                                  [cider.piggieback/wrap-cljs-repl]}
                   ;; need to add the compliled assets to the :clean-targets
                   :clean-targets ^{:protect false}
                   ["resources/public/js/compiled" :target-path]}})
