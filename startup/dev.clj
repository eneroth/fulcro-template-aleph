(ns dev
  (:require [poc.core.main :refer [restart-server]]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]))
            ;[cognitect.rebl :as rebl]))


(defn start-server? [args-map]
  (if (= "false" (get args-map "--start-server"))
    false
    true))

(defn -main [& args]
  (let [args-map (apply hash-map args)]
    (println "Starting interactive compilation…")
    (set! *print-namespace-maps* false)
    (shadow-server/start!)
    (shadow/watch :client)
    (when (start-server? args-map)
      (restart-server))
    ;(cognitect.rebl/-main)
    (println "Ready to go!")))


;; Dev tools
;; ##################################
(comment
 ;; Evaluate to switch REPL to the client

 (do
   ; (require 'dirac.agent)
   ; (dirac.agent/boot!)
   (shadow/repl :client))

 #__)
