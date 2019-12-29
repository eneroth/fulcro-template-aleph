(ns poc.component.page-404
  (:require [hiccup.page :refer [include-css]]
            [hiccup.core :as hiccup]
            [poc.component.index :refer [css-loc]]))


;; Template
;; ##################################
(defn page-404*
  ([]
   (page-404* "Sorry, page not found."))
  ([{:keys [msg]}]
   (hiccup/html
    [:html
     [:head
      [:title "POC!"]
      [:meta {:charset "utf-8"}]
      [:meta {:name    "viewport"
              :content "width=device-width, initial-scale=1.0"}]
      (include-css
       css-loc)]
     [:body.body-container
      [:div#app
       [:h1 "404"]
       (when msg
         [:p
          msg])]]])))

(def page-404 (memoize page-404*))
