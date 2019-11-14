(ns poc.api.resolver.http
  (:require ;; HTTP
            [aleph.http :as http]
            [manifold.deferred :as d]

            ;; Pathom
            [com.wsscode.pathom.connect :as pc :refer [defresolver]]))


(defn http-get [url]
  (-> (http/get url)
      (d/catch Exception (fn [e] {:error e}))))


(defn format-http-get-result [deferred-result]
  {:string/html (let [result @deferred-result]
                  (when (= 200 (:status result))
                    (slurp (:body result))))})


(defresolver get-html [_env input]
  {::pc/input #{:string/url}
   ::pc/output [:string/html]
   ::pc/batch? true}
  (if (sequential? input)
    (->> input
         (map :string/url)
         (map http-get)
         (pmap format-http-get-result)
         vec)
    (format-http-get-result (http-get (:string/url input)))))


(def resolvers [get-html])
