(ns poc.core.router
  (:require
   [clojure.string :as string]

   ;; Helpers
   [poc.util.helpers :as helpers]

   ;; Logging
   [taoensso.timbre :as timbre :refer [spy warn]]

   ;; Libraries
   [reitit.core :as reitit]

   ;; Components and templates
   #?(:clj [poc.component.index    :refer [index]])
   #?(:clj [poc.component.page-404 :refer [page-404]])

   ;; ClojureScript
   #?(:cljs [pushy.core :as pushy])
   #?(:cljs [com.fulcrologic.fulcro.routing.dynamic-routing :as dr])
   #?(:cljs [poc.component.app :as app])

   ;; Clojure
   #?(:clj [poc.api.parser :refer [api-parser]])
   #?(:clj [reitit.ring :as reitit-ring])
   #?(:clj [com.fulcrologic.fulcro.server.api-middleware :as fulcro-server])
   #?(:clj [ring.middleware.resource :refer [wrap-resource]])
   #?(:clj [ring.middleware.session :refer [wrap-session]])
   #?(:clj [ring.middleware.cookies :refer [wrap-cookies]])
   #?(:clj [ring.middleware.content-type :refer [wrap-content-type]])))


;; Frontend
;; ##################################
#?(:cljs
   (defonce history
     (pushy/pushy
      (fn [p]
        (let [route-segments (vec (rest (string/split p "/")))]
          (spy :info route-segments)
          (dr/change-route app/app route-segments)))
      identity)))


#?(:cljs
   (defn start-pushy! []
     (pushy/start! history)))

#?(:cljs
   (defn start-router! []
     (dr/initialize! app/app)))

#?(:cljs
   (defn start! []
     (start-router!)
     (start-pushy!)))


#?(:cljs
   (defn route-to! [route-string]
     (println "Routing to" route-string)
     (pushy/set-token! history route-string)))


;; Handlers
;; ##################################
#?(:clj
   (defn root
     [_]
     {:status 200
      :headers {"content-type" "text/html"}
      :body (index)}))


;; Routes
;; ##################################
(def routes
  [["/"
    {:get #?(:clj  root
             :cljs helpers/nil-fn)
     :name :root}]])


;; Backend
;; ##################################
#?(:clj
   (defn wrap-api
     [handler]
     (fn [request]
       (if (= "/api" (:uri request))
         (fulcro-server/handle-api-request (:transit-params request)
                                           (partial api-parser {:request request}))
         (handler request)))))


#?(:clj
   (defn resource-ext [resource-name]
     (when-let [pos (string/last-index-of resource-name ".")]
       (subs resource-name pos))))


#?(:clj
   (def resource-exts
     #{".jpg" ".png" ".svg"
       ".css" ".js" ".map" ".ico"}))


#?(:clj
   (defn all-routes-to-index [handler]
     (fn [{:keys [uri] :as req}]
       (if (or (= "/api" uri)
               (get resource-exts (resource-ext uri)))
         (handler req)
         (handler (assoc req :uri "/"))))))


;; Router
;; ##################################
#?(:clj
   (def ^:private not-found-handler
     (fn [req]
       (warn (str (:uri req) " requested, but not found."))
       {:status  404
        :headers {"Content-Type" "text/html"}
        :body (page-404
               {:msg [:span "Sorry, " [:strong (:uri req)]
                      " doesn't seem to be a valid path."]})})))


(def router
  #?(:cljs (reitit/router routes {:conflicts nil})
     :clj
     (reitit-ring/ring-handler
       (reitit-ring/router routes {:conflicts nil})
       not-found-handler
       {:middleware [[all-routes-to-index]
                     [wrap-content-type]
                     [wrap-resource "public"]
                     [wrap-session]
                     [wrap-cookies]
                     [fulcro-server/wrap-transit-response]
                     [fulcro-server/wrap-transit-params]
                     [wrap-api]]})))


;; When running in Datomic Ions, it may be necessary to wrap the router in a function
;; to avoid certain errors. This can be deleted if not intending to run on Ions.
#?(:clj
   (defn route-wrapper [req]
     (router req)))
