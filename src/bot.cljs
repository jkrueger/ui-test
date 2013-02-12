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

(def view (comp :view ->object))

(defn- ->behaviors [presenter]
  (map (partial fetch :behavior) (:behaviors presenter)))

(defn- event-handler [trigger type id]
  (fn [evt]
    (let [object (->object id)
          target (-> (jayq/$ (.-target evt))
                     (jayq/attr :botid)
                     (int)
                     (->object target-id))]
      (when (= (:type target) type)
        (doseq [behavior (->behaviors object)]
          (when (contains? (:triggers behavior) trigger)
            ((:reaction behavior) object evt)))))))

(defn- register-triggers [presenter id $view]
  (doseq [trigger (:triggers presenter)]
    (let [[type event] (str/split (name trigger) #"\.")]
      (jayq/on $view event
        (event-handler trigger (keyword type) id)))))

(defn make [type & args]
  (when-let [presenter (fetch :presenter type)]
    (let [id       (next-id)
          view     (crate/html ((:factory presenter) presenter))
          $view    (jayq/$ view)]
      (jayq/attr $view :botid id)
      (register-triggers presenter id $view)
      (add-to-state :objects :id
        (assoc presenter
          :id   id
          :view view))
      id)))
