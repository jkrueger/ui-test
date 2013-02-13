(ns main
  (:require [bot       :as b]
            [jayq.core :as jayq]))

(defn class-toggler [class]
  (fn [item]
    (-> (jayq/$ (:view item))
        (jayq/toggle-class :open))))

(b/behavior :open-menu
  :triggers [:menu-button.click]
  :reaction (class-toggler :open))

(b/presenter :menu-button
  :triggers  []
  :behaviors []
  :label     "none"
  :factory   (fn [this]
               [:a.dropdown-toggle {:href "#"}
                (:label this)
                [:b.caret]]))

(b/presenter :menu-item
  :triggers  [:menu-button.click]
  :behaviors [:open-menu]
  :label     "none"
  :menu      nil
  :factory   (fn [this]
               (let [label (:label this)]
                 (if-let [menu (:menu this)]
                   [:li.dropdown
                    (b/view (b/make :menu-button :label label))
                    (b/view menu)]
                   [:li [:a {:href "#"} label]]))))

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

(defn file-menu []
  (b/make :menu
          :submenu? true
          :items    [(b/make :menu-item
                             :label "Save")
                     (b/make :menu-item
                             :label "Load")]))

(defn top-menu []
  (b/make :menu
          :items [(b/make :menu-item
                          :label "File"
                          :menu  (file-menu))
                  (b/make :menu-item
                          :label "Edit")]))

(defn menu-bar []
  (b/make :menu-bar
          :menu (top-menu)))

(defn ^:export init []
  (-> (jayq/$ :#content)
      (jayq/append (b/view (menu-bar)))))
