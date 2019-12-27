(ns poc.api.parser
  (:require ;; Core
            [clojure.core.async :refer [<!!]]

            ;; Resolvers
            [poc.session.resolvers :as session]

            ;; Pathom
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.diplomat.http :as p.http]

            ;; Web
            [poc.api.aleph-driver :as p.http.aleph]))


(defonce indexes (atom nil))


(pc/defresolver index-explorer [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (get env ::pc/indexes)})


(def resolvers
  [session/resolvers
   index-explorer])


(def pathom-parser
  (p/parallel-parser
    {::p/env {::p/reader [p/map-reader
                          pc/parallel-reader
                          pc/open-ident-reader
                          p/env-placeholder-reader]
              ::p/placeholder-prefixes #{">"}
              ::p.http/driver          p.http.aleph/request-async}
     ::p/mutate  pc/mutate-async
     ::p/plugins [(pc/connect-plugin {; we can specify the index for the connect plugin to use
                                      ; instead of creating a new one internally
                                      ::pc/indexes  indexes
                                      ::pc/register resolvers})
                  p/error-handler-plugin
                  p/request-cache-plugin
                  p/trace-plugin]}))


(defn api-parser [request query]
  (let [result (<!! (pathom-parser (assoc request ::parser pathom-parser)
                                   query))]
    ;; Allows requests and responses to be inspected in the shadow-cljs inspector
    (tap> (clojure.datafy/datafy {:request request
                                  :query query
                                  :result result}))
    result))
