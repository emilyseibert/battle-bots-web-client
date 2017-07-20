(ns wombats-web-client.components.simulator.configure
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [wombats-web-client.utils.forms :as f]
            [wombats-web-client.components.select-input :refer [select-input]]
            [wombats-web-client.utils.forms :refer [cancel-modal-input
                                                    submit-modal-input
                                                    optionize]]
            [wombats-web-client.components.radio-select
             :refer [radio-select]]
            [wombats-web-client.utils.errors :refer [required-field-error]]))

(defn- callback-success [state]
  "closes modal on success"
  (re-frame/dispatch [:reset-modal]))

(defonce radio-keys {:frame "Full Arena" :mini-map "Wombat POV"})

(defn- update-simulator-configuration!
  [state]
  (let [wombat-id (:wombat-id @state)
        wombat-id-error (:wombat-id-error @state)
        template-id (:template-id @state)
        template-id-error (:template-id-error @state)
        view-mode (:view-mode @state)]
    (re-frame/dispatch [:simulator/change-view
                        (get (clojure.set/map-invert radio-keys) view-mode)])
    (cond
      (and template-id (nil? wombat-id))
      (swap! state assoc :wombat-id-error required-field-error)
      (and wombat-id (nil? template-id))
      (swap! state assoc :template-id-error required-field-error)
      (and wombat-id template-id)
      (do
        (re-frame/dispatch [:simulator/initialize-simulator
                            {:simulator/template-id template-id
                             :simulator/wombat-id wombat-id}])
        (callback-success state))
      (and (nil? template-id) (nil? wombat-id))
      (callback-success state))))

(defn configuration-panel []
  (let [wombats (re-frame/subscribe [:my-wombats])
        sim-templates (re-frame/subscribe [:simulator/templates])
        selected-wombat (re-frame/subscribe [:simulator/wombat-id])
        selected-template (re-frame/subscribe [:simulator/template-id])
        wombat-view (re-frame/subscribe [:simulator/get-view-mode])
        form-state (reagent/atom {:wombat-id @selected-wombat
                                  :wombat-id-error nil
                                  :template-id @selected-template
                                  :template-id-error nil
                                  :view-mode (get radio-keys @wombat-view)})]
    (fn []
      (let [wombats @wombats
            templates @sim-templates]
        [:div {:class-name "configuration-panel"}
         [:div.title "CONFIGURATION"]
         [:div.panel-content
          [select-input {:form-state form-state
                         :form-key :wombat-id
                         :error-key :wombat-id-error
                         :option-list
                         (optionize
                          [:wombat/id]
                          [:wombat/name]
                          wombats)
                         :label "Wombat"}]
          [select-input {:form-state form-state
                         :form-key :template-id
                         :error-key :template-id-error
                         :option-list
                         (optionize
                          [:simulator-template/id]
                          [:simulator-template/arena-template :arena/name]
                          templates)
                         :label "Arena"}]
          [radio-select {:class "view-mode"
                         :name "view-mode"
                         :label "View"
                         :state form-state
                         :radios (vals radio-keys)}]]
         [:button.update-btn
          {:on-click #(update-simulator-configuration! form-state)}
          "START"]]))))

(defn configuration-modal []
  (let [wombats (re-frame/subscribe [:my-wombats])
        sim-templates (re-frame/subscribe [:simulator/templates])
        selected-wombat (re-frame/subscribe [:simulator/wombat-id])
        selected-template (re-frame/subscribe [:simulator/template-id])
        wombat-view (re-frame/subscribe [:simulator/get-view-mode])
        form-state (reagent/atom {:wombat-id @selected-wombat
                                  :wombat-id-error nil
                                  :template-id @selected-template
                                  :template-id-error nil
                                  :view-mode (get radio-keys @wombat-view)})]

    (reagent/create-class
     {:component-will-unmount #(re-frame/dispatch [:update-modal-error nil])
      :display-name "configuration-modal"
      :reagent-render
      (fn []
        (let [wombats @wombats
              templates @sim-templates]

          [:div.configure {:class "modal configuration-modal"}
           [:div.title "CONFIGURATION"]

           [:div.modal-content
            [select-input {:form-state form-state
                           :form-key :wombat-id
                           :error-key :wombat-id-error
                           :option-list
                           (optionize
                            [:wombat/id]
                            [:wombat/name]
                            wombats)
                           :label "Wombat"}]
            [select-input {:form-state form-state
                           :form-key :template-id
                           :error-key :template-id-error
                           :option-list
                           (optionize
                            [:simulator-template/id]
                            [:simulator-template/arena-template :arena/name]
                            templates)
                           :label "Arena"}]
            [radio-select {:class "view-mode"
                           :name "view-mode"
                           :label "View"
                           :state form-state
                           :radios (vals radio-keys)}]]
           [:div.action-buttons
            [cancel-modal-input]
            [submit-modal-input
             "DONE"
             #(update-simulator-configuration! form-state)]]]))})))

(defn open-configure-simulator-modal []
  (re-frame/dispatch [:set-modal {:fn configuration-modal
                                  :show-overlay true}]))
