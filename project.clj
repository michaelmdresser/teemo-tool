(defproject teemo-tool "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "none"
            :url "none"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [irclj "0.5.0-alpha4"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.xerial/sqlite-jdbc "3.32.3"]]
  :main ^:skip-aot teemo-tool.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
