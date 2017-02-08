(ns wombats-web-client.panels.account
  (:require [re-frame.core :as re-frame]
            [wombats-web-client.components.add-button :as add-wombat-button]
            [wombats-web-client.components.modals.add-wombat-modal :refer [add-wombat-modal]]
            [wombats-web-client.components.cards.wombat :as wombat-card]))

;; User Account Panel

(defn open-add-wombat-modal []
  (fn []
    (re-frame/dispatch [:set-modal add-wombat-modal])))

(defn header []
  [:div.header
     [:div.title "MY WOMBATS"]
     [:div.logout [:a {:href "#/signout"} "LOG OUT"]]])

(defn welcome []
  (let [my-wombats (re-frame/subscribe [:my-wombats])]
    [:div.account-panel
     [header]
     [:div.wombats (map wombat-card/root @my-wombats)]
     [add-wombat-button/root (open-add-wombat-modal)]]))

(defn login-prompt []
  ;; not str
  [:div (str "You must login to see your account.")])

(defn account []
  (let [current-user (re-frame/subscribe [:current-user])]
    (fn []
      ;; if-not
      (if (nil? @current-user)
        [login-prompt]
        [welcome]))))
