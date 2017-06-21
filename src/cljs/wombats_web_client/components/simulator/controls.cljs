(ns wombats-web-client.components.simulator.controls
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [wombats-web-client.utils.socket :as ws]))

(defn- on-step-click!
  [evt sim-state]
  (re-frame/dispatch [:simulator/process-simulation-frame
                      {:game-state @sim-state}]))

(defn render
  [sim-state show-mini-map]
  [:div.simulator-controls
   [add-wombat-button/root (open-configure-simulator-modal)]
   [:button.step {:on-click #(on-step-click! % sim-state)} "Step"]
   [:button.mini-map
    {:on-click #(re-frame/dispatch [:simulator/toggle-simulator-mini-map])}
    (if show-mini-map
      "Show Full View"
      "Show Wombat View")]])
