(ns poc.api.aleph-driver
  (:require [aleph.http :as client]
            [clojure.core.async :as async]
            [clojure.spec.alpha :as s]
            [com.wsscode.pathom.diplomat.http :as http]
            [com.wsscode.pathom.misc :as p.misc]))

;; TODO: ::http/debug?
(defn build-request-map
  [{::http/keys [url content-type accept as body headers form-params] :as req}]
  (let [q? (partial contains? req)]
    (cond-> {:url    url
             :method (-> req http/request-method keyword)}
            (q? ::http/headers)      (assoc :headers headers)
            (q? ::http/content-type) (assoc :content-type (-> content-type name keyword))
            (q? ::http/accept)       (assoc :accept (http/encode-type->header accept))
            (q? ::http/as)           (assoc :as (-> as name keyword))
            (q? ::http/form-params)  (assoc :form-params form-params)
            (q? ::http/body)         (assoc :body body))))


(defn build-response-map
  [{:keys [status body headers]
    :as   response}]
  (with-meta {::http/status  status
              ::http/body    body
              ::http/headers headers}
             response))

(defn request [req]
  (s/assert ::http/request req)
  (-> req
      build-request-map
      client/request
      deref
      build-response-map))


(defn request-async
  [req]
  (s/assert ::http/request req)
  (let [chan (async/promise-chan)]
    (try
      (async/put! chan (request req))
      (catch Throwable e
        (async/put! chan e)))
    chan))


(when p.misc/INCLUDE_SPECS
  (s/fdef request
    :args (s/cat :request ::http/request)
    :ret ::http/response))
