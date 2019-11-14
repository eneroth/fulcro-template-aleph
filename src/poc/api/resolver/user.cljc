(ns poc.api.resolver.user
  (:require
   #?(:clj  [clojure.core.async :refer [<!!]])

   ;; Logging
   [taoensso.timbre :as timbre :refer [spy info]]

   ;; Components
   #?(:cljs [poc.component.current-user :refer [CurrentUser]])
   #?(:cljs [poc.component.app :as app])

   ;; Plumbing
   #?(:cljs [poc.core.router :as router])

   ;; Pathom
   #?(:clj  [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]])

   ;; Fulcro
   #?(:clj  [com.fulcrologic.fulcro.server.api-middleware :as fulcro-server])
   #?(:cljs [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]])
   #?(:cljs [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]])

   ;; Reader workarounds
   #?(:clj  [poc.util.placebo :as uism])
   #?(:cljs [poc.util.placebo :as pc])))


;; "Database"
;; ##################################
(def users {1 {:user/id 1
               :user/password "letmein"
               :contact/email "loretta@example.com"
               :person/given-names "Loretta"
               :person/surname "Fong-Phrymp"}
            2 {:user/id 2
               :user/password "letmein"
               :contact/email "foo@bar.com"
               :person/given-names "Foo"
               :person/surname "Bar"}})



;; User resolvers
;; ##################################
#?(:clj
   (def session-user-return-fields
     [:user/id
      :contact/email
      :person/given-names
      :person/surname]))



#?(:clj
   (defn id->user [id]
     (select-keys (get users id) session-user-return-fields)))


#?(:clj
   (defn email->user [email]
     (let [subject (first (filter #(= email (:contact/email %)) (vals users)))]
       (select-keys subject session-user-return-fields))))


#?(:clj
   (defresolver user-resolver [env {:user/keys [id]}]
     {::pc/input #{:user/id}
      ::pc/output session-user-return-fields}
     (id->user id)))


#?(:clj
   (defresolver current-user-resolver [env _]
     {::pc/output [{:session/current-user (conj session-user-return-fields :user/valid?)}]}
     (let [{:user/keys [id] :as session} (get-in env [:request :session])]
       (info (str "Finding user by ID " id))
       {:session/current-user
        (if id
          (merge (select-keys session session-user-return-fields)
                 {:user/valid? true})
          {:user/id nil
           :contact/email nil
           :user/valid? false})})))


;; Login mutations
;; ##################################
#?(:clj
   (defmutation login [env {:contact/keys [email]
                            :user/keys [password]}]
     {::pc/params #{:contact/email
                    :contact/password}
      ::pc/output (conj session-user-return-fields :user/valid?)}
     (let [subject         (email->user email)
           stored-password (:user/password (get users (:user/id subject)))]
       (info (str "Logging in user " email))
       (Thread/sleep 500)
       (if (and subject (= password stored-password))
         (fulcro-server/augment-response
           (merge subject
                  {:user/valid? true})
           (fn [ring-resp]
             (update ring-resp :session merge subject)))
         {:user/valid? false}))))


;; Logout mutations
;; ##################################
#?(:clj
   (defmutation logout [env _args]
     {::pc/output [:user/id :user/valid?]}
     (let [{:contact/keys [email]} (get-in env [:request :session])]
       (info (str "Logging out user " email))
       (fulcro-server/augment-response
         {:user/id nil
          :user/valid? false}
         (fn [ring-resp]
           (assoc ring-resp :session {}))))))

#?(:clj
   (def resolvers [user-resolver current-user-resolver login logout]))


;; Session state machine
;; ##################################
#?(:cljs
   (defn handle-login [{::uism/keys [event-data] :as env}]
     (info "Handle login")
     (info event-data)
     (-> env
         (uism/activate :state/checking-credentials)
         (uism/trigger-remote-mutation :actor/login-form `login
           (merge event-data
             {:com.fulcrologic.fulcro.mutations/returning (uism/actor-class env :actor/user)
              :com.fulcrologic.fulcro.algorithms.data-targeting/target [:poc.component.current-user :current-user]
              ::uism/ok-event    :event/ok
              ::uism/error-event :event/error})))))


#?(:cljs
   (defn handle-logout [{::uism/keys [event-data] :as env}]
     (info "Handle logout")
     (info event-data)

     (router/route-to! "/login")
     (-> env
         (uism/trigger-remote-mutation :actor/login-form `logout {})
         (uism/apply-action assoc-in [:poc.component.current-user/session :current-user] {:user/id nil :user/valid? false})
         (uism/activate :state/idle))))


#?(:cljs
   (def session-result-events
     {:event/ok
      {::uism/target-states #{:state/idle}
       ::uism/handler (fn [env]
                        (let [logged-in? (uism/alias-value env :logged-in?)]
                          (if logged-in?
                            (router/route-to! "/home")
                            (router/route-to! "/login"))
                          (-> env
                              (uism/activate :state/idle)
                              (uism/assoc-aliased :bad-credentials? (not logged-in?)))))}
      :event/error
      {::uism/target-states #{:state/server-failed}}}))


#?(:cljs
   (defstatemachine session-machine
     {;; ------------------------------------------------------------------------
      ::uism/actor-name #{:actor/user
                          :actor/login-form}

      ::uism/aliases {:logged-in?       [:actor/user :user/valid?]
                      :bad-credentials? [:actor/login-form :ui/bad-credentials]}

      ::uism/states
      {;; ------------------------------------------------------------------------
       :initial
       {::uism/handler (fn [env]
                         (println "Running initital…")
                         (let [new-env (-> env
                                           (uism/load :session/current-user :actor/user
                                             {::uism/ok-event    :event/ok
                                              ::uism/error-event :event/error})
                                           (uism/activate :state/checking-existing-session))]
                           (println "Done!")
                           new-env))}

       ;; ------------------------------------------------------------------------
       :state/checking-existing-session
       {::uism/events
        {:event/ok
         {::uism/target-states #{:state/idle}
          ::uism/handler (fn [env]
                           (let [logged-in? (uism/alias-value env :logged-in?)]
                             (router/route-to! (if logged-in? "/home" "/login"))
                             (uism/activate env :state/idle)))}
         :event/error
         {::uism/target-states #{:state/server-failed}}}}



       ;; ------------------------------------------------------------------------
       :state/idle
       {::uism/events {:event/login  {::uism/target-states #{:state/checking-credentials}
                                      ::uism/handler handle-login}
                       :event/logout {::uism/target-states #{:state/idle}
                                      ::uism/handler handle-logout}}}

       ;; ------------------------------------------------------------------------
       :state/checking-credentials
       {::uism/events
        {:event/ok
         {::uism/target-states #{:state/idle}
          ::uism/handler (fn [env]
                           (let [logged-in? (uism/alias-value env :logged-in?)]
                             (router/route-to! (if logged-in? "/home" "/login"))
                             (-> env
                                 (uism/activate :state/idle)
                                 (uism/assoc-aliased :bad-credentials? (not logged-in?)))))}
         :event/error
         {::uism/target-states #{:state/server-failed}}}}


       ;; ------------------------------------------------------------------------
       :state/server-failed
       {::uism/events {:event/login {::uism/target-states #{:state/checking-credentials}
                                     ::uism/handler handle-login}}}}}))
