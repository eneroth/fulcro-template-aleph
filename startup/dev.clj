(ns dev
  (:require [poc.core.main :refer [restart-server]]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]))


(defn start-server? [args-map]
  (not= "false" (get args-map "--start-server")))


(defn -main [& args]
  (let [args-map (apply hash-map args)]
    (println "Starting interactive compilation…")
    (set! *print-namespace-maps* false)
    (shadow-server/start!)
    (shadow/watch :client)
    (when (start-server? args-map)
      (restart-server))
    (println "Ready to go!")))


;; Dev tools
;; ##################################
(comment
 ;; Evaluate to switch REPL to the client
 (shadow/repl :client)

 ;; Evaluate to start REBL
 (do
   (require '[cognitect.rebl :as rebl])
   (rebl/-main))

 #__)
