(ns main
  (:require [bot       :as b]
            [jayq.core :as jayq]))

(defn toggler [class]
  (fn [item]
    (-> (jayq/$ (:view item))
        (jayq/toggle-class class))))

(b/behavior :open-menu
  :triggers [:menu-button.click]
  :reaction (toggler :open))

(b/behavior :set-item-label
  :triggers [:menu-button.click]
  :reaction (fn [item]
              (.log js/console "TEST")
              (-> (jayq/$ (:view item))
                  (jayq/text "Clicked"))))

(b/presenter :menu-button
  :triggers  []
  :behaviors []
  :label     "none"
  :factory   (fn [this]
               [:a.dropdown-toggle {:href "#"}
                (:label this)
                [:b.caret]]))

(b/presenter :submenu
  :triggers  [:menu-button.click]
  :behaviors [:open-menu]
  :label     "none"
  :menu      nil
  :factory   (fn [this]
               (let [label (:label this)
                     menu  (:menu this)]
                 [:li.dropdown
                  (b/view (b/make :menu-button :label label))
                  (b/view menu)])))

(b/presenter :menu-item
  :triggers  [:menu-item.click]
  :behaviors []
  :label     "none"
  :factory   (fn [this]
               (let [label (:label this)]
                 [:li [:a {:href "#"} label]])))

(defn menu-item [label behavior]
  (-> (b/make :menu-item :label label)
      (b/add-behavior behavior)))

(b/presenter :menu
  :triggers  []
  :behaviors []
  :submenu?  false
  :items     []
  :factory   (fn [this]
                (apply conj
                       (if (:submenu? this)
                         [:ul.dropdown-menu]
                         [:ul.nav])
                       (map b/view (:items this)))))

(b/presenter :menu-bar
  :triggers  []
  :behaviors []
  :menu      nil
  :factory   (fn [this]
               [:div.navbar
                [:div.navbar-inner
                 (b/view (:menu this))]]))

(def file-menu
  (b/make :menu
          :submenu? true
          :items    [(menu-item "Save" :set-item-label)
                     (menu-item "Load" :set-item-label)]))

(def top-menu
  (b/make :menu
          :items [(b/make :submenu
                          :label "File"
                          :menu  file-menu)
                  (b/make :menu-item
                          :label "Edit")]))

(def menu-bar
  (b/make :menu-bar
          :menu top-menu))

(defn ^:export init []
  (-> (jayq/$ :#content)
      (jayq/append (b/view menu-bar))))
