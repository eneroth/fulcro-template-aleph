(ns poc.session.resolvers
  (:require [poc.session.model :as model]
            [taoensso.timbre :as timbre :refer [spy info]]
            [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
            [com.fulcrologic.fulcro.server.api-middleware :as fulcro-server]
            [poc.util.placebo :as uism]))


(defn id->user
  "Given an ID, return a user map."
  [id]
  (select-keys (get model/users id) model/user-return-fields))


(defn email->user
  "Given an email, return a user map."
  [email]
  (let [subject (first (filter #(= email (:contact/email %)) (vals model/users)))]
    (select-keys subject model/user-return-fields)))


(defresolver user-resolver [env {:user/keys [id]}]
  {::pc/input #{:user/id}
   ::pc/output model/user-return-fields}
  (id->user id))


(defresolver current-user-resolver [env _]
  {::pc/output [{:session/current-user model/user-return-fields}]}
  (let [{:user/keys [id] :as session} (get-in env [:request :session])]
    (if id
      (info (str "Found current user with ID " id))
      (info "No current user found"))
    {:session/current-user
     (if id
       (select-keys session model/user-return-fields)
       model/nil-user)}))


(defmutation login [env {:contact/keys [email]
                         :user/keys [password]}]
  {::pc/params #{:contact/email
                 :contact/password}
   ::pc/output model/user-return-fields}
  (let [subject         (email->user email)
        stored-password (:user/password (get model/users (:user/id subject)))]
    (info (str "Logging in user " email))
    (Thread/sleep 500)
    (if (and subject (= password stored-password))
      (fulcro-server/augment-response
        subject
        (fn [ring-resp]
          (update ring-resp :session merge subject)))
      model/nil-user)))


;; Logout mutations
;; ##################################
(defmutation logout [env _args]
  {::pc/output [:user/id]}
  (let [{:contact/keys [email]} (get-in env [:request :session])]
    (info (str "Logging out user " email))
    (fulcro-server/augment-response
      model/nil-user
      (fn [ring-resp]
        (assoc ring-resp :session {})))))

(def resolvers [user-resolver current-user-resolver login logout])
