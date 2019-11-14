(ns poc.component.app
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.networking.http-remote :as http]
            [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro-css.css :as css]))


(defonce app (app/fulcro-app {:remotes {:remote (http/fulcro-http-remote {})}}))

(defn mount! [root]
  (app/mount! app root "app"))
