(ns poc.session.resolvers
  (:require [poc.session.db :as db]
            [taoensso.timbre :as timbre :refer [spy info]]
            [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
            [com.fulcrologic.fulcro.server.api-middleware :as fulcro-server]))





(defresolver user-resolver [_env {:user/keys [id]}]
  {::pc/input #{:user/id}
   ::pc/output db/user-return-fields}
  (db/id->user id))


(defresolver current-user-resolver [env _]
  {::pc/output [{:session/current-user db/user-return-fields}]}
  (let [{:user/keys [id] :as session} (get-in env [:request :session])]
    (if id
      (info (str "Found current user with ID " id))
      (info "No current user found"))
    {:session/current-user
     (if id
       (select-keys session db/user-return-fields)
       db/nil-user)}))


(defmutation login [_env {:contact/keys [email]
                          :user/keys [password]}]
  {::pc/params #{:contact/email
                 :contact/password}
   ::pc/output db/user-return-fields}
  (let [subject         (db/email->user email)
        stored-password (:user/password (get db/users (:user/id subject)))]
    (info (str "Logging in user " email))
    (Thread/sleep 500)
    (if (and subject (= password stored-password))
      (fulcro-server/augment-response
        subject
        (fn [ring-resp]
          (update ring-resp :session merge subject)))
      db/nil-user)))


;; Logout mutations
;; ##################################
(defmutation logout [env _args]
  {::pc/output [:user/id]}
  (let [{:contact/keys [email]} (get-in env [:request :session])]
    (info (str "Logging out user " email))
    (fulcro-server/augment-response
      db/nil-user
      (fn [ring-resp]
        (assoc ring-resp :session {})))))

(def resolvers [user-resolver current-user-resolver login logout])
