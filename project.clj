(defproject gift-list "0.0.1"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [compojure "1.1.5"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [hiccup "1.0.3"]
                 [domina "1.0.2-SNAPSHOT"]
                 [com.novemberain/monger "1.5.0"]
                 [crate "0.2.4"]]

  :min-lein-version "2.0.0"
  :hooks [leiningen.cljsbuild]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :cljsbuild  {:builds
               [{:source-paths ["src"]
                 :compiler {:pretty-print true
                            :output-to "resources/public/js/cljs.js"
                            :optimizations :whitespace}
                 :jar true}]})