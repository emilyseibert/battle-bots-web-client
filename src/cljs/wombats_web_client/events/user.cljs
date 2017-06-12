(ns wombats-web-client.events.user
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [ajax.core :refer [json-response-format GET PUT POST DELETE]]
            [ajax.edn :refer [edn-request-format edn-response-format]]
            [day8.re-frame.http-fx]
            [pushy.core :as pushy]
            [wombats-web-client.db :as db]
            [wombats-web-client.utils.errors :refer [get-error-message]]
            [wombats-web-client.utils.local-storage :refer [get-token
                                                            remove-token!]]
            [wombats-web-client.constants.urls
             :refer [self-url
                     github-signout-url
                     my-wombats-url
                     my-wombat-by-id-url
                     my-github-repositories-url]]
            [wombats-web-client.routes :refer [history]]
            [wombats-web-client.utils.auth :refer [add-auth-header
                                                   get-current-user-id]]
            [wombats-web-client.utils.socket :as ws])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; AUTH SPECIFIC
(defn sign-out-user
  "signs out user from server and removes their auth token"
  []
  (GET github-signout-url {:response-format (edn-response-format)
                           :keywords? true
                           :headers (add-auth-header {})}))

(defn sign-out-event
  []
  (pushy/set-token! history "/welcome")
  (re-frame/dispatch [:sign-out])
  (sign-out-user))

;; USER WOMBAT SPECIFIC
(defn load-wombats
  "loads all wombats associated with user id"
  [id]
  (let [ch (async/chan)]
    (GET (my-wombats-url id) {:response-format (edn-response-format)
                              :keywords? true
                              :headers (add-auth-header {})
                              :handler #(go (async/>! ch %))
                              :error-handler #(go (async/>! ch nil))})
    ch))

(defn get-all-wombats []
  (let [wombats-ch (load-wombats (get-current-user-id))]
    (go
      (re-frame/dispatch [:update-wombats (async/<! wombats-ch)]))))


(defn post-new-wombat
  "creates and returns a wombat"
  [id name url on-success on-error]
  (POST (my-wombats-url id) {:response-format (edn-response-format)
                             :format (edn-request-format)
                             :keywords? true
                             :headers (add-auth-header {})
                             :handler on-success
                             :params {:wombat/name name
                                      :wombat/url url}
                             :error-handler on-error}))

(defn delete-wombat-by-id
  "deletes wombat from db by id"
  [user-id wombat-id on-success on-error]
  (DELETE (my-wombat-by-id-url
           user-id
           wombat-id)
          {:response-format (edn-response-format)
           :format (edn-request-format)
           :keywoards? true
           :headers (add-auth-header {})
           :handler on-success
           :error-handler on-error}))

(defn edit-wombat
  "edits wombat by id in db"
  [user-id wombat-id name url on-success on-error]
  (PUT (my-wombat-by-id-url
        user-id
        wombat-id)
       {:response-format (edn-response-format)
        :format (edn-request-format)
        :keywords? true
        :headers (add-auth-header {})
        :handler on-success
        :params {:wombat/name name
                 :wombat/url url}
        :error-handler on-error}))

(defn create-new-wombat
  [name url cb-success]
  (post-new-wombat
   (get-current-user-id)
   name
   url
   (fn []
     (get-all-wombats)
     (cb-success))
   #(re-frame/dispatch [:update-modal-error (get-error-message %)])))

(defn edit-wombat-by-id
  [name url wombat-id cb-success]
  (edit-wombat
   (get-current-user-id)
   wombat-id
   name
   url
   (fn []
     (get-all-wombats)
     (cb-success))
   #(re-frame/dispatch [:update-modal-error (get-error-message %)])))

(defn delete-wombat
  [id cb-success]
  (delete-wombat-by-id
   (get-current-user-id)
   id
   (fn []
     (get-all-wombats)
     (cb-success))
   #(re-frame/dispatch [:update-modal-error (get-error-message %)])))

;; USER REPOSITORY SPECIFIC
(defn load-user-repositories
  "loads all repositories that are owned by a user id"
  [id]
  (let [ch (async/chan)]
    (GET (my-github-repositories-url id) {:response-format (edn-response-format)
                              :keywords? true
                              :headers (add-auth-header {})
                              :handler #(go (async/>! ch %))})
    ch))

(defn get-all-repositories []
  (let [repository-ch (load-user-repositories (get-current-user-id))]
    (go
      (re-frame/dispatch [:update-repositories (async/<! repository-ch)]))))

(defn get-repos [cb-success]
  (get-all-repositories)
  (cb-success))

(re-frame/reg-event-db
 :sign-out
 (fn [db [_ _]]
   (remove-token!)
   (assoc db :auth-token nil :current-user nil)))

(re-frame/reg-event-db
 :update-wombats
 (fn [db [_ wombats]]
   (assoc db :my-wombats wombats)))

(re-frame/reg-event-db
 :user-error
 (fn [db [_ error]]
   (print "temporary error")))

(re-frame/reg-event-db
 :update-repositories
 (fn [db [_ repositories]]
   (assoc db :my-repositories repositories)))

(re-frame/reg-event-fx
 :bootstrap-user-data
 (fn [{:keys [db]} [_ user]]
   {:db (assoc db :auth-token (get-token)
                  :bootstrapping false)
    :http-xhrio {:method          :get
                 :uri             (my-wombats-url (:user/id user))
                 :headers         (add-auth-header {})
                 :response-format (edn-response-format)
                 :on-success      [:update-wombats]
                 :on-failure      [:user-error]}
    :dispatch [:update-user user]}))
