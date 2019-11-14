(ns poc.util.component
  (:require [com.fulcrologic.fulcro.components :as comp]
            [poc.util.helpers :as helpers]))


;; Focus handling for components
;; ##################################
(def ident->focus-key
  (memoize
   (fn [ident]
     (helpers/re-namespace-key ident :focus))))

(defn- this->focus-key [this]
  (ident->focus-key (first (comp/get-ident this))))


(def ident->timeout-key
  (memoize
   (fn [ident]
     (helpers/re-namespace-key ident :timeout))))

(defn- this->timeout-key [this]
  (ident->timeout-key (first (comp/get-ident this))))


(defn- maybe-clear-timeout
  "Given a timeout atom, cancels the timeout if the atom contains one."
  [timeout-atom]
  (when-let [current-timeout @timeout-atom]
    ;(println "Clearing timeout" current-timeout)
    (js/window.clearTimeout current-timeout)
    (reset! timeout-atom nil)))


(defn- maybe-create-timeout
  "Given a component 'this', creates an atom under :<ident>/timeout,
   if it doesn't exist already."
  [this]
  (let [timeout-key (this->timeout-key this)
        current-timeout (get (comp/get-state this) timeout-key)]
    (when-not current-timeout
      (let [new-timeout (atom nil)]
        ;(println "Creating timeout")
        (comp/set-state! this {timeout-key new-timeout})
        new-timeout))))


(defn focus
  "Given a component 'this', creates an :<ident>/focus key, which is set to true."
  [this]
  (let [focus-key   (this->focus-key this)
        timeout-key (this->timeout-key this)]
    ;(println "Focusing" (comp/get-ident this))
    (let [timeout (or (maybe-create-timeout this)
                      (get (comp/get-state this) timeout-key))
          current-timeout @timeout]
      (maybe-clear-timeout timeout)
      (comp/update-state! this assoc focus-key true))))


(defn blur
  "Given a component 'this', creates an :<ident>/focus key, which is set to false.
   May be interrupted by a focus event on the same component, directly following the
   unfocus event."
  [this]
  (let [focus-key   (this->focus-key this)
        timeout-key (this->timeout-key this)]
    ;(println "Unfocusing" (comp/get-ident this))
    (let [timeout (or (maybe-create-timeout this)
                      (get (comp/get-state this) timeout-key))
          current-timeout @timeout]
      (maybe-clear-timeout timeout)
      (reset! timeout
        (.setTimeout js/window
         #(comp/update-state! this assoc focus-key false))))))