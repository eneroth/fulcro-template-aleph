(ns poc.component.current-user
  (:require ;;Fulcro
            [com.fulcrologic.fulcro.ui-state-machines :as uism]
            [com.fulcrologic.fulcro-css.localized-dom :refer [div a]]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))


(defsc CurrentUser
  [this {:user/keys [id]
         :person/keys [given-names surname]}]
  {:ident (fn [] [::session :current-user])
   :query [:user/id
           :person/given-names
           :person/surname]
   :initial-state {:user/id nil}
   :css [[:.user-widget {:display "flex"
                         :margin "12px"
                         :flex-direction "row"}]
         [:.login-logout-button {:margin-left "6px"}]]}
  (div :.user-widget
    (when id
      (div
        (str given-names " " surname)
        (a :.login-logout-button
           {:href "/login"
            :onClick #(uism/trigger! this :poc.session.user/session :event/logout)}
           "Logout")))))

(def current-user (comp/factory CurrentUser))
