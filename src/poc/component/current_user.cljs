(ns poc.component.current-user
  (:require ;;Fulcro
            [com.fulcrologic.fulcro.ui-state-machines :as uism]
            [com.fulcrologic.fulcro-css.localized-dom :refer [div span a]]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))


(defsc CurrentUser
  [this {:user/keys [id]
         :person/keys [given-names surname fullname]}]
  {:ident (fn [] [::session :current-user])
   :query [:user/id
           :person/given-names
           :person/surname
           :person/fullname]
   :initial-state {:user/id nil}
   :css [[:.user-widget {:display "flex"
                         :margin "12px"
                         :flex-direction "row"}]
         [:.login-logout-button {:margin-left "6px"}]]}
  (div :.user-widget
    (when id
      (div
        (span :.display-name fullname)
        (a :.login-logout-button
           {:href "/login"
            :onClick #(uism/trigger! this :poc.session.user/session :event/logout)}
           "Logout")))))

(def current-user (comp/factory CurrentUser))
