(defproject wombats-web-client "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.1"]
                 [re-frisk "0.3.2"]
                 [secretary "1.2.3"]
                 [com.cemerick/url "0.1.1"]
                 [cljs-ajax "0.5.4"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [kibu/pushy "0.3.6"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-less "1.7.5"]
            [lein-pdo "0.1.1"]
            [lein-kibit "0.1.3"]
            [lein-auto "0.1.3"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :less {:source-paths ["less"]
         :target-path "resources/public/css"}

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler figwheel-server.core/handler}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.2"]
                   [figwheel-sidecar "0.5.7"]
                   [com.cemerick/piggieback "0.2.1"]
                   [figwheel-pushstate-server "0.1.0"]]

    :plugins      [[lein-figwheel "0.5.7"]
                   [lein-doo "0.1.7"]]}}

  :cljsbuild
  {:builds
   [{:id           "local"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "wombats-web-client.core/mount-root"}
     :compiler     {:main                 wombats-web-client.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/local"
                    :asset-path           "/js/compiled/local"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    :closure-defines      {goog.DEBUG true
                                           wombats-web-client.constants.urls/base-api-url "//localhost:8888"}}}
    {:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "wombats-web-client.core/mount-root"}
     :compiler     {:main                 wombats-web-client.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/dev"
                    :asset-path           "/js/compiled/dev"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    :closure-defines      {goog.DEBUG true
                                           wombats-web-client.constants.urls/base-api-url "//dev.api.wombats.io"}}}
    {:id           "deploy-dev"
     :source-paths ["src/cljs"]
     :compiler     {:main            wombats-web-client.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :output-dir      "resources/public/js/compiled/deploy-dev"
                    :optimizations   :advanced
                    :externs         ["externs.js"]
                    :pretty-print    false
                    :closure-defines {goog.DEBUG false
                                      wombats-web-client.constants.urls/base-api-url "//dev.api.wombats.io"}}}
    {:id           "deploy-qa"
     :source-paths ["src/cljs"]
     :compiler     {:main            wombats-web-client.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :output-dir      "resources/public/js/compiled/deploy-qa"
                    :optimizations   :advanced
                    :externs         ["externs.js"]
                    :pretty-print    false
                    :closure-defines {goog.DEBUG false
                                      wombats-web-client.constants.urls/base-api-url "//qa.api.wombats.io"}}}
    {:id           "deploy-prod"
     :source-paths ["src/cljs"]
     :compiler     {:main            wombats-web-client.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :output-dir      "resources/public/js/compiled/deploy-prod"
                    :optimizations   :advanced
                    :externs         ["externs.js"]
                    :pretty-print    false
                    :closure-defines {goog.DEBUG false
                                      wombats-web-client.constants.urls/base-api-url "//api.wombats.io"}}}
    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          wombats-web-client.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}]}

    :aliases {"run-local"   ["pdo" "clean," ["figwheel" "local"] ["less" "auto"]]
              "run-dev"     ["pdo" "clean," ["figwheel" "dev"]   ["less" "auto"]]
              "deploy-dev"  ["do"  "clean," ["kibit"] ["cljsbuild" "once" "deploy-dev"]  ["less" "once"]]
              "deploy-qa"   ["do"  "clean," ["cljsbuild" "once" "deploy-qa"]   ["less" "once"]]
              "deploy-prod" ["do"  "clean," ["cljsbuild" "once" "deploy-prod"] ["less" "once"]]})
