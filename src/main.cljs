(ns main
  (:require [bot       :as b]
            [jayq.core :as jayq]))

(defn nothing [])

(defn toggler [class]
  (fn [object]
    (-> (jayq/$ (:view object))
        (jayq/toggle-class class))))

(defn caller [property]
  (fn [object]
    (let [f (get object property)]
      (f))))

(b/behavior :open-on-click
  :triggers [:menu-button.click]
  :reaction (toggler :open))

(b/behavior :open-on-hover
  :triggers [:menu-button.mouseover]
  :reaction (toggler :open))

(b/behavior :close-on-leave
  :triggers [:menu-button.mouseout]
  :reaction (toggler :open))

(b/behavior :action-on-click
  :triggers [:.click]
  :reaction (caller :action))

(b/behavior :prevent-default
  :triggers [:.click]
  :reaction #(.preventDefault %2))

(b/presenter :menu-button
  :triggers  [:.click]
  :behaviors [:prevent-default]
  :label     "none"
  :factory   (fn [this]
               [:a.dropdown-toggle {:href "#"}
                (:label this)
                [:b.caret]]))

(b/presenter :submenu
  :triggers  [:menu-button.click]
  :behaviors [:open-on-click]
  :label     "none"
  :menu      nil
  :factory   (fn [this]
               (let [label (:label this)
                     menu  (:menu this)]
                 [:li.dropdown
                  (b/view (b/make :menu-button :label label))
                  (b/view menu)])))

(b/presenter :menu-item
  :triggers  [:.click]
  :behaviors [:action-on-click :prevent-default]
  :label     "none"
  :action    nothing
  :factory   (fn [this]
               (let [label (:label this)]
                 [:li [:a {:href "#"} label]])))

(b/presenter :menu
  :submenu?  false
  :items     []
  :factory   (fn [this]
                (apply conj
                       (if (:submenu? this)
                         [:ul.dropdown-menu]
                         [:ul.nav])
                       (map b/view (:items this)))))

(b/presenter :menu-bar
  :menu      nil
  :factory   (fn [this]
               [:div.navbar
                [:div.navbar-inner
                 (b/view (:menu this))]]))

(defn bar [menu]
  (b/make :menu-bar :menu menu))

(defn menu [& items]
  (b/make :menu
          :items (vec items)))

(defn sub [& items]
  (b/make :menu
          :submenu? true
          :items    (vec items)))

(def file-menu
  (sub
    (b/make :menu-item
            :label "Save"
            :action #(.log js/console "Save"))
    (b/make :menu-item
            :label "Load"
            :action #(.log js/console "Load"))))

(def top-menu
  (menu
    (b/make :submenu
            :label "File"
            :menu  file-menu)
    (b/make :menu-item
            :label "Edit")))

(def menu-bar
  (-> (bar top-menu)
      (b/view)))

(defn ^:export init []
  (-> (jayq/$ :#content)
      (jayq/append menu-bar)))
