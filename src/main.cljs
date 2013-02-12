(ns main
  (:require [bot       :as b]
            [jayq.core :as jayq]))

(b/behavior :add-label
  :triggers #{:adder.click}
  :reaction
  (fn [presenter evt]
    (let [label (b/make :label)]
      (-> (jayq/$ :#container (:view presenter))
          (jayq/append (b/view label))))))

(b/presenter :label
  :factory #(vector :p "FOO"))

(b/presenter :adder
  :triggers  []
  :behaviors []
  :factory   #(vector :input {:type "button" :value "Add"}))

(b/presenter :container
  :triggers  []
  :behaviors []
  :factory   #(vector :div#container))

(b/presenter :frame
  :triggers  [:adder.click]
  :behaviors [:add-label]
  :factory   (fn [obj]
               (let [container (b/make :container)
                     button    (b/make :adder)]
                 [:div#frame
                   (b/view container)
                   (b/view button)])))

(defn ^:export init []
  (-> (jayq/$ :#content)
      (jayq/append (b/view (b/make :frame)))))
