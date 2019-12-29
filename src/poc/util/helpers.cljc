(ns poc.util.helpers
  (:require
   [clojure.string :as string]
   #?(:cljs [goog.dom :refer [getElement]])
   #?(:cljs [goog.dom.Range :refer [createCaret]])))


(defn re-namespace-key
  "Produces a new b-key, namespaced under the same namespace as a-key."
  [a-key b-key]
  (keyword (str (namespace a-key) "/" (name b-key))))


(defn round [value decimals]
  (let [round-fn #?(:cljs js/Math.round :clj #(Math/round %))
        pow-fn   #?(:cljs js/Math.pow :clj #(Math/pow %1 %2))
        factor (pow-fn 10 decimals)
        result (/ (round-fn (* value factor)) factor)]
    (if (zero? decimals)
      (int result)
      result)))


(defn format-count [a-count]
  (let [count-size (-> a-count str count)]
    (condp = count-size
      0 (str 0)
      1 (str a-count)
      2 (str a-count)
      3 (str a-count)
      4 (str (round (/ a-count 1000) 1) "k")
      5 (str (round (/ a-count 1000) 0) "k")
      6 (str (round (/ a-count 1000) 0) "k")
      7 (str (round (/ a-count 1000000) 1) "m")
      8 (str (round (/ a-count 1000000) 0) "m")
      9 (str (round (/ a-count 1000000) 0) "m")
     10 (str (round (/ a-count 1000000000) 1) "B"))))


(defn deep-merge
  "Takes a collection of maps and deep merges them. I.e...

   [{:a {:b 1}}
    {:a {:c 2}}]

  ... will be merged into ...

  [{:a {:b 1
        :c 2}}]

  If the maps contain vectors for the same path into the map, those
  vectors are concatenated. The behaviour for when the maps contain
  vectors for the same path where other maps contain other values,
  is undefined."
  [& maps]
  (let [f (fn [old new]
            (condp every? [old new]
              map? (deep-merge old new)
              vector? (into old new)
              seq? (concat old new)
              set? (into old new)
              new))]
    (apply merge-with f maps)))


(defn dissoc-in
  "Associates a value in a nested associative structure, where ks is a
  sequence of keys and v is the new value and returns a new nested structure.
  If any levels do not exist, hash-maps will be created."
  {:static true}
  [m [k & ks]]
  (if ks
    (assoc m k (dissoc-in (get m k) ks))
    (dissoc m k)))


(def nil-fn (constantly nil))


(defn after
  "Given a predicate and a collection, for the first item on position n that matches
   the predicate, provides the item on position n+1. If n+1 exceeds the length of the
   collection, provides the item at position 0. Returns nil if there's zero or one elements
   in the collection, or no element matching the predicate."
  ([pred coll]
   (after pred nil coll))
  ([pred value coll]
   (when (some pred coll)
     (let [modified-pred (if value
                           (complement (partial pred value))
                           (complement pred))
           [before after] (split-with modified-pred coll)]
       (if-let [maybe-item (second after)]
         maybe-item
         (first before))))))


(defn before
  "Given a predicate and a collection, for the first item on position n that matches
   the predicate, provides the item on position n-1. If n-1 is lower than 0, provides
   the provides the item at the last position of the collection. Returns nil if there's
   zero or one elements in the collection, or no element matching the predicate."
  ([pred coll]
   (before pred nil coll))
  ([pred value coll]
   (when (and (some pred coll)
              (<= 2 (count coll)))
     (let [modified-pred (if value
                           (complement (partial pred value))
                           (complement pred))
           [before after] (split-with modified-pred coll)]
       (if-let [maybe-item (last before)]
         maybe-item
         (last after))))))


(defn map->args
  "Given a map of parameters to arguments, construct a URL-escaped string,
   on the format '?hello=world&…=…', and therefore suitable to tack on to a URL."
  [a-map]
  (let [encode-uri #?(:clj  #(java.net.URLEncoder/encode % "UTF-8")
                      :cljs js/encodeURI)
        xf (comp (map #(vector (name (get % 0))
                               "="
                               (string/replace (-> (get % 1) str encode-uri) "+" "%20")))
                 (interpose "&"))]
    (str "?" (apply str (transduce xf into [] a-map)))))


#?(:cljs
   (def get-element getElement))


#?(:cljs
   (def create-caret createCaret))


#?(:cljs
   (defn get-element-text [element-id]
     (.-innerText (get-element element-id))))


#?(:cljs
   (defn set-element-text [element-id text]
     (set! (.-innerText (get-element element-id)) text)))


#?(:cljs
   (defn focus-element [element-id]
     (.focus (get-element element-id))))


#?(:cljs
   (defn blur-element [element-id]
     (.blur (get-element element-id))))


#?(:cljs
   (defn stop-propagation [event]
     (.stopPropagation event)))

#?(:cljs
   (defn prevent-default [event]
     (.preventDefault event)))
