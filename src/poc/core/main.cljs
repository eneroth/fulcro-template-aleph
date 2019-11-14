(ns poc.core.main
  (:require ;; Components
            [poc.session.user :as user]
            [poc.component.login-form :refer [LoginForm]]
            [poc.component.current-user :refer [CurrentUser current-user]]
            [poc.component.app :as app]

            ;; Plumbing
            [poc.core.router :as router]

            ;; Fulcro
            [com.fulcrologic.fulcro-css.css-injection :as inj]
            [com.fulcrologic.fulcro-css.css :as css]
            [com.fulcrologic.fulcro.ui-state-machines :as uism]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
            [com.fulcrologic.fulcro.components :as comp
             :refer [defsc get-query get-initial-state factory]]
            [com.fulcrologic.fulcro-css.localized-dom :as dom :refer [div]]))


(defsc Home [_this {{:person/keys [given-names]} :session/current-user}]
  {:query [{[:session/current-user '_] (get-query CurrentUser)}]
   :ident (fn [] [:component/id :home])
   :route-segment ["home"]
   :initial-state {}
   :css [[:.home {:margin "12px"}]]}
  (let [{:keys [home]} (css/get-classnames Home)]
    (div {:classes [home]}
      (str "Hello " given-names "!"))))


(defrouter MainRouter [_this _props]
  {:router-targets [LoginForm Home]})

(def main-router (factory MainRouter))


(defsc Root
  [this
   {:root/keys [router]
    session-data :session/current-user}]
  {:query [{:root/router (get-query MainRouter)}
           {:session/current-user (get-query CurrentUser)}
           [::uism/asm-id ::user/session]]
   :initial-state (fn [_]
                    {:root/router (get-initial-state MainRouter)})
   :css [[:.toolbar {:display "flex"
                     :align-items "flex-end"
                     :flex-direction "column"
                     :background-color "#f7f8fb"}]]}
  (let [{:keys [toolbar]} (css/get-classnames Root)
        session-state (uism/get-active-state this ::user/session)
        ready? (not= :state/initial session-state)]
    (when ready?
      (comp/fragment
        (div
          (div {:classes [toolbar]}
            (current-user session-data))
          (main-router router))
        (inj/style-element {:component Root
                            ;; Ensure that CSS is refreshed in dev, but not prod
                            :react-key (when goog.DEBUG (str (random-uuid)))
                            ;; Ensure that CSS is readable in dev, but minified in prod
                            :garden-flags {:pretty-print? goog.DEBUG}})))))


;; Entry point
;; ##################################
(defn ^:export run
  "Entry point."
  []
  (app/mount! Root))


(defn ^:export runonce
  []
  (enable-console-print!)
  (uism/begin! app/app user/session-machine ::user/session
    {:actor/user CurrentUser
     :actor/login-form LoginForm})
  (run)
  (router/start!))
