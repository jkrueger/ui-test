(ns bot
  (:require [clojure.string :as str]
            [crate.core     :as crate]
            [jayq.core      :as jayq]))

(def state  (atom {}))
(def cur-id (atom 0))

(defn- add-to-state [type k obj]
  (swap! state assoc-in [type (get obj k)] obj)
  obj)

(defn next-id []
  (swap! cur-id inc))

(defn fetch [type id]
  (get-in @state [type id]))

(defn behavior [id & {:keys [triggers reation] :as m}]
  (add-to-state :behavior :type (assoc m :type id)))

(defn presenter [id & {:keys [triggers behaviors view] :as m}]
  (add-to-state :presenter :type (assoc m :type id)))

(defn ->object [id]
  (fetch :objects id))

(defn type-of [id]
  (get-in @state [:objects id :type]))

(def view (comp :view ->object))

(defn- ->behaviors [presenter]
  (map (partial fetch :behavior) (:behaviors presenter)))

(defn trigger [id trigger-name evt]
  (let [object (->object id)]
    (doseq [behavior (->behaviors object)]
      (when (contains? (set (:triggers behavior)) trigger-name)
        ((:reaction behavior) object evt)))))

(defn- ->botid [dom]
  (let [$dom (jayq/$ dom)
        id   (loop [node $dom]
               (if-let [botid (jayq/attr node :botid)]
                 botid
                 (recur (jayq/parent node))))]
    (int id)))

(defn- event-handler [trigger-name type id]
  (fn [evt]
    (when (= (-> (.-target evt)
                 (->botid)
                 (->object)
                 (:type))
             (if (empty? type)
               (type-of id)
               (keyword type)))
      (trigger id trigger-name evt))))

(defn- register-triggers [presenter id $view]
  (doseq [trigger (:triggers presenter)]
    (let [[type event] (str/split (name trigger) #"\.")]
      (jayq/on $view event
        (event-handler trigger type id)))))

(defn make [type & args]
  (when-let [presenter (fetch :presenter type)]
    (let [id        (next-id)
          presenter (merge presenter (apply hash-map args))
          view      (crate/html ((:factory presenter) presenter))
          $view     (jayq/$ view)]
      (jayq/attr $view :botid id)
      (register-triggers presenter id $view)
      (add-to-state :objects :id
        (assoc presenter
          :id   id
          :view view))
      id)))

(defn add-behavior [id behvior]
  (swap! state
         update-in
         [:objects id :behaviors]
         conj behavior)
  id)
