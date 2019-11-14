(ns poc.component.app
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.networking.http-remote :as http]))


(defonce app (app/fulcro-app {:remotes {:remote (http/fulcro-http-remote {})}}))

(defn mount! [root]
  (app/mount! app root "app"))
