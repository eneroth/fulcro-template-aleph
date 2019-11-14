(ns poc.component.current-user
  (:require ;;Fulcro
            [com.fulcrologic.fulcro.ui-state-machines :as uism]
            [com.fulcrologic.fulcro-css.localized-dom :as dom :refer [div a]]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc get-query factory transact!]]
            [poc.core.router :as router :refer [route-to!]]))


(defsc CurrentUser
  [this {:user/keys [id valid?]
         :contact/keys [email]
         :person/keys [given-names surname]
         :as props}]
  {:ident (fn [] [::session :current-user])
   :query [:user/id
           :contact/email
           :user/valid?
           :person/given-names
           :person/surname]
   :initial-state {:user/id nil
                   :user/valid? false}
   :css [[:.user-widget {:display "flex"
                         :margin "12px"
                         :flex-direction "row"}]
         [:.login-logout-button {:margin-left "6px"}]]}
  (div :.user-widget
    (when valid?
      (div
        (str given-names " " surname)
        (a :.login-logout-button
           {:href "/login"
            :onClick #(uism/trigger! this :poc.api.resolver.user/session :event/logout)}
           "Logout")))))

(def current-user (comp/factory CurrentUser))
