(ns poc.session.user
  (:require [poc.session.db :as db]
            [taoensso.timbre :as timbre :refer [spy info]]
            [poc.core.router :as router]
            [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]))


(defn handle-login [{::uism/keys [event-data] :as env}]
  (info "Handle login")
  (info event-data)
  (-> env
      (uism/activate :state/checking-credentials)
      (uism/trigger-remote-mutation :actor/login-form `poc.session.resolvers/login
        (merge event-data
          {:com.fulcrologic.fulcro.mutations/returning (uism/actor-class env :actor/user)
           :com.fulcrologic.fulcro.algorithms.data-targeting/target [:poc.component.current-user :current-user]
           ::uism/ok-event    :event/ok
           ::uism/error-event :event/error}))))


(defn handle-logout [{::uism/keys [event-data] :as env}]
  (info "Handle logout")
  (info event-data)
  (router/route-to! "/login")
  (-> env
      (uism/trigger-remote-mutation :actor/login-form `poc.session.resolvers/logout {})
      (uism/apply-action assoc-in [:poc.component.current-user/session :current-user] db/nil-user)
      (uism/activate :state/logged-out)))


(defstatemachine session-machine
  {;; ------------------------------------------------------------------------
   ::uism/actor-name #{:actor/user
                       :actor/login-form}

   ::uism/aliases {:logged-in?       [:actor/user :user/id]
                   :bad-credentials? [:actor/login-form :ui/bad-credentials]}

   ::uism/states
   {;; INITIAL
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

    ;; CHECKING EXISTING SESSION
    :state/checking-existing-session
    {::uism/events
     {:event/ok
      {::uism/target-states #{:state/logged-in
                              :state/logged-out}
       ::uism/handler (fn [env]
                        (let [logged-in? (uism/alias-value env :logged-in?)]
                          (router/route-to! (if logged-in? "/home" "/login"))
                          (if logged-in?
                            (uism/activate env :state/logged-in)
                            (uism/activate env :state/logged-out))))}
      :event/error
      {::uism/target-states #{:state/server-failed}}}}

    ;; CHECKING CREDENTIALS
    :state/checking-credentials
    {::uism/events
     {:event/ok
      {::uism/target-states #{:state/logged-in
                              :state/logged-out}
       ::uism/handler (fn [env]
                        (let [logged-in? (uism/alias-value env :logged-in?)]
                          (router/route-to! (if logged-in? "/home" "/login"))
                          (if logged-in?
                            (-> env
                                (uism/activate :state/logged-in)
                                (uism/assoc-aliased :bad-credentials? false))
                            (-> env
                                (uism/activate :state/logged-out)
                                (uism/assoc-aliased :bad-credentials? true)))))}
      :event/error
      {::uism/target-states #{:state/server-failed}}}}

    ;; LOGGED IN
    :state/logged-in
    {::uism/events {:event/logout {::uism/target-states #{:state/logged-out}
                                   ::uism/handler handle-logout}}}

    ;; LOGGED OUT
    :state/logged-out
    {::uism/events {:event/login  {::uism/target-states #{:state/checking-credentials}
                                   ::uism/handler handle-login}}}

    ;; SERVER FAILED
    :state/server-failed
    {::uism/events {:event/login {::uism/target-states #{:state/checking-credentials}
                                  ::uism/handler handle-login}}}}})
