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

(defn behavior [id & rest]
  (add-to-state :behavior :type
    (assoc (apply hash-map rest) :type id)))

(defn presenter [id & rest]
  (add-to-state :presenter :type
    (assoc (apply hash-map rest) :type id)))

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

(defn- fill-in-type [trigger-type id]
  (if (empty? trigger-type)
    (type-of id)
    (keyword trigger-type)))

(defn- target-type [evt]
  (-> (.-target evt)
      (->botid)
      (->object)
      (:type)))

(defn- event-handler [trigger-name type id]
  (fn [evt]
    (when (= (target-type evt)
             (fill-in-type type id))
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

(defn update-behaviors [id f & args]
  (apply swap! state
         update-in
         [:objects id :behaviors]
         f
         args)
  id)

(defn add-behaviors [id & behaviors]
  (apply update-behaviors id conj behaviors))

(defn rem-behavior [id & behaviors]
  (update-behaviors id (partial remove (set behaviors))))
