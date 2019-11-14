(ns poc.component.login-form
  (:require ;; Fulcro
            [com.fulcrologic.fulcro.ui-state-machines :as uism]
            [com.fulcrologic.fulcro.dom :as dom :refer [div label input button]]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc factory transact!]]
            [com.fulcrologic.fulcro.mutations :as m :refer [set-string!]]
            [com.fulcrologic.fulcro-css.css :as css]))

(defsc LoginForm
  [this
   {:ui/keys [email password bad-credentials]
    :as props}]
  {:ident (fn [] [:component/id :login])
   :query [:ui/email :ui/password :ui/bad-credentials
           [::uism/asm-id :poc.session.user/session]]
   :route-segment ["login"]
   :initial-state {:ui/email    "foo@bar.com"
                   :ui/bad-credentials false
                   :ui/password "letmein"}
   :css [[:.login-form {:margin "12px"}]]}
  (let [{:keys [login-form]} (css/get-classnames LoginForm)
        session-state (uism/get-active-state this :poc.session.user/session)
        busy?  (= session-state :state/checking-credentials)
        error? (= session-state :state/server-failed)]
    (div {:classes [login-form]}
      (when error? "ERROR!")
      (when bad-credentials "BAD CREDENTIALS!")
      (when busy? "Loading…")
      (div :.field
        (label "Email")
        (input {:value    email
                :disabled busy?
                :onChange #(set-string! this :ui/email :event %)}))
      (div :.field
        (label "Password")
        (input {:value password
                :disabled busy?
                :onChange #(set-string! this :ui/password :event %)})
        (button {:onClick #(uism/trigger! this :poc.session.user/session :event/login {:contact/email email
                                                                                       :user/password password})}
          "Login")))))


(def login-form (factory LoginForm {:keyfn :component/id}))
