(ns wombats-web-client.routes
    (:require-macros [secretary.core :refer [defroute]])
    (:import goog.History)
    (:require [secretary.core :as secretary]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [pushy.core :as pushy]
              [re-frame.core :as re-frame]
              [wombats-web-client.events.user :refer [sign-out-event]]
              [wombats-web-client.utils.auth :refer [user-is-coordinator?]]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  ;; --------------------
  ;; define routes here

  (defroute "/" []
    (re-frame/dispatch [:set-active-panel :view-games-panel]))

  (defroute "/games/:game-id" {game-id :game-id}
    (re-frame/dispatch [:game/join-game game-id])
    (re-frame/dispatch [:set-active-panel :game-play-panel]))

  (defroute "/config" []
    (if (user-is-coordinator?)
      (re-frame/dispatch [:set-active-panel :config-panel])
      (re-frame/dispatch [:set-active-panel :page-not-found-panel])))

  (defroute "/simulator" []
    (re-frame/dispatch [:set-active-panel :simulator-panel]))

  (defroute "/account" []
    (re-frame/dispatch [:set-active-panel :account-panel]))

  (def history (pushy/pushy secretary/dispatch!
                            (fn [x] (when (secretary/locate-route x) x))))

  ;; --------------------
  (pushy/start! history)
  (hook-browser-navigation!))
