(ns poc.component.page-404
  (:require [clojure.pprint :refer [pprint]]
            [hiccup.page :refer [include-css]]
            [poc.component.index :refer [js-loc css-loc]]))


;; Template
;; ##################################
(defn page-404*
  ([]
   (page-404* "Sorry, page not found."))
  ([{:keys [msg]}]
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
        [:p (str msg)])]]]))

(def page-404 (memoize page-404*))
