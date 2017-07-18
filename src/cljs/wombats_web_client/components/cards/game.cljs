(ns wombats-web-client.components.cards.game
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [wombats-web-client.constants.games :refer [game-type-str-map]]
            [wombats-web-client.components.countdown-timer
             :refer [countdown-timer]]
            [wombats-web-client.components.join-button :refer [join-button]]
            [wombats-web-client.utils.games :refer [get-user-in-game
                                                    get-game-state-str]]
            [wombats-web-client.components.modals.join-wombat-modal
             :refer [join-wombat-modal]]
            [wombats-web-client.components.modals.delete-game-modal
             :refer [delete-game-modal]]
            [wombats-web-client.utils.auth :refer [user-is-coordinator?]]))

(defn open-join-game-modal-fn [game-id]
  (fn [e]
    (.preventDefault e)
    (re-frame/dispatch [:set-modal {:fn #(join-wombat-modal game-id)
                                    :show-overlay true}])))

(defn get-arena-text-info [{:keys [game-type rounds width height]}]
  (let [round-txt (if (= 1 rounds) "Round" "Rounds")
        game-type-str (get game-type-str-map game-type)]
    (str game-type-str " - " rounds " " round-txt " | " width "x" height)))

(defn freq [freq-name amt]
  [:div.freq-object
   [:img {:class (when (= freq-name "food_cherry") "cherry")
          :src (str "/images/" freq-name ".png")}]
   [:div.freq-amt amt]])

(defn get-arena-frequencies [arena joined capacity]
  (let [{food :arena/food
         poison :arena/poison
         zakano :arena/zakano} arena
        ratio-joined (str joined "/" capacity)]
    [:div.arena-freq
     [freq "wombat_orange_right" ratio-joined]
     [freq "zakano_front" zakano]
     [freq "food_cherry" food]
     [freq "poison_vial2" poison]]))

(defn arena-card [{:keys [is-open
                          is-private
                          is-joinable
                          is-full
                          is-playing
                          start-time
                          game-id]}]
  (let [game-state (get-game-state-str is-full is-playing)]

    [:div.arena-preview
     [:img.arena-preview-img {:class (when game-state "has-state")
                              :src "/images/mini-arena.png"}]
     (when game-state
       [:div.game-state game-state])
     (when is-open
       [:div.countdown "Starts in "
        [countdown-timer start-time game-id]])
     (when is-joinable
       [join-button {:is-private is-private
                     :on-click (open-join-game-modal-fn game-id)}])]))

(defn render-my-wombat-icon [player]
  (let [color (:player/color player)]
    [:div.wombat-preview-icon
     [:img {:src (str "images/wombat_" color "_right.png")}]]))

(defn open-delete-game-modal [id]
  (fn []
    (re-frame/dispatch [:set-modal {:fn #(delete-game-modal id)
                                    :show-overlay true}])))


;; CARD STATES
;; is-joinable - OPEN & JOINABLE - :pending-open & not in-game
;; is-full - OPEN & FULL - :pending-closed
;; is-playing - OPEN & ACTIVE - :active
;; is-finished - FINISHED - :closed
;; States effect hoverstate and overlay design.
(defn game-card [game user-in-game is-joinable is-full is-playing num-joined]
  (let [{arena :game/arena
         game-id :game/id
         game-name :game/name
         game-players :game/players
         game-capacity :game/max-players
         game-rounds :game/num-rounds
         game-type :game/type
         game-status :game/status
         game-start :game/start-time
         game-private :game/is-private} game
        {arena-width :arena/width
         arena-height :arena/height} arena
         is-pending-open (= :pending-open game-status)
         is-pending-closed (= :pending-closed game-status)]

    [:div.game-card {:key game-id}
     [:a.link {:href (str "/games/" game-id)}]

     [arena-card {:is-open (or is-pending-open is-pending-closed)
                  :is-private game-private
                  :is-joinable is-joinable
                  :is-full is-full
                  :is-playing is-playing
                  :start-time game-start
                  :game-id game-id}]
     [:div.game-information
      (when (not-empty user-in-game) [render-my-wombat-icon user-in-game])
      [:div.text-info
       [:div.game-name game-name]
       [:div (get-arena-text-info {:game-type game-type
                                   :rounds game-rounds
                                   :width arena-width
                                   :height arena-height})]
       (when (user-is-coordinator?)
         [:input.delete {:type "button"
                         :value "DELETE"
                         :on-click (open-delete-game-modal game-id)}])]
      [get-arena-frequencies arena num-joined game-capacity]]]))
