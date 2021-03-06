(ns wombats-web-client.components.modals.game-full-modal
  (:require [re-frame.core :as re-frame]
            [wombats-web-client.events.games :refer [get-all-games]]
            [wombats-web-client.utils.forms :refer [submit-modal-input]]
            [wombats-web-client.utils.errors :refer [game-full-error]]))

(defn close-modal []
  (get-all-games)
  (re-frame/dispatch [:set-modal nil]))

(defn game-full-modal []
  (fn []
    [:div.modal.game-full-modal
     [:div.title "GAME FULL"]
     [:div.modal-content
      [:div.desc game-full-error]]
     [:div.action-buttons
      [submit-modal-input "OKAY" close-modal]]]))
