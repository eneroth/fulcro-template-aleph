(ns poc.component.index
  (:require [hiccup.page :refer [include-css include-js]]))

;; To help avoid caching of CSS and JS when pushed to a server
(def js-loc   (str "/js/main.js?" (java.util.UUID/randomUUID)))
(def css-loc (str "/css/main.css?" (java.util.UUID/randomUUID)))


;; Template
;; ##################################
(defn index* []
  [:html
   [:head
    [:title "POC!"]
    [:meta {:charset "utf-8"}]
    [:meta {:name    "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    (include-css
     "https://cdnjs.cloudflare.com/ajax/libs/meyer-reset/2.0/reset.min.css")]
   [:body
    [:div#app
     (include-js js-loc)]]])

(def index (memoize index*))
