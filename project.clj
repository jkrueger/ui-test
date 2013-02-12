(defproject ui-test "0.0.1-SNAPSHOT"
  :min-lein-version "2.0.0"
  :plugins          [[lein-cljsbuild "0.3.0"]]
  :dependencies     [[org.clojure/clojure "1.4.0"]
                     [crate "0.2.4"]
                     [jayq  "2.2.0"]]
  :cljsbuild
  {:crossovers     []
   :crossover-path "crossover"
   :builds
   [{:source-paths ["src"],
     :id "main",
     :compiler
     {:pretty-print true,
      :output-to "out/bot.js",
      :optimizations :simple}}]})
