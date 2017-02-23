(defproject vivi "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.immutant/web "2.1.6"]
                 [compojure "1.1.8"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-devel "1.5.1"]
                 [environ "1.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 ]
  :main vivi.core
  :uberjar-name "vivi-standalone.jar"
  :profiles {:uberjar {:aot [vivi.core]}}
  :min-lein-version "2.4.0")
