(ns vk-common-groups.events
  (:require
   [re-frame.core :as re-frame]
   [vk-common-groups.db :as db]
   [vk-common-groups.coeffects :as cofx]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reitit.frontend.controllers :as rfc]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-fx
 ::push-state
 (fn [_ [_ & route]]
   {:push-state route}))

(re-frame/reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

(re-frame/reg-event-fx
 ::restore-local-state
 [(re-frame/inject-cofx :local-db)]
 (fn [{:keys [db local-db]} _]
   {:db (merge db local-db)}))

(re-frame/reg-event-fx
 ::logout
 (fn [_]
   (let [new-db db/default-db]
     {:local-save new-db
      :db new-db})))

(re-frame/reg-event-fx
 ::new-auth
 [(re-frame/inject-cofx :url-params)]
 (fn [{:keys [db url-params]} _]
   (let [{:keys [access_token user_id]} url-params
         new-db (assoc db
                       :user-token access_token
                       :user-id user_id)]
     {:db new-db
      :local-save new-db
      :redirect "/"})))

(re-frame/reg-event-fx
 ::change-input
 [(re-frame/inject-cofx :last-input-id)]
 (fn [{:keys [db last-input-id]} [_ id value]]
   (let [last-id (if (keyword? last-input-id) 
                   (js/parseInt (name last-input-id)) 
                   last-input-id)
         valued (assoc-in db [:inputs id] value)
         new-db (cond
                  (= value "") (update-in valued [:inputs] dissoc id)
                  (= id last-input-id) (assoc-in valued [:inputs (+ 1 last-id)] "")
                  :else valued)]
     {:db (assoc new-db :result-groups {})})))

(defn users-get-request
  [screen-names token]
  {:method "users.get"
   :params {:user_ids (reduce #(str %1 "," %2) screen-names)
            :fields "photo_50"
            :v "5.130"
            :access_token token}
   :handler ::handle-users-get})

(defn groups-get-request
  ([user-id token] (groups-get-request user-id token 0))
  ([user-id token offset]
   {:method "groups.get"
    :params {:user_id user-id
             :count "1000"
             :fields "members_count"
             :extended "1"
             :offset offset
             :v "5.130"
             :access_token token}
    :handler ::handle-groups-get}))

(re-frame/reg-event-fx
 ::submit
 (fn [{:keys [db]} _]
   (let [new-queue (conj [] (users-get-request (vals (:inputs db)) (:user-token db)))]
     {:db (assoc db
                 :result-groups {}
                 :response-groups {}
                 :response-users {}
                 :loading? true
                 :request-queue new-queue)
      :fx [[:dispatch [::request-from-queue]]]})))

(re-frame/reg-event-fx 
 ::request-from-queue
 (fn [{:keys [db]} _]
   (let [queue (:request-queue db)
         item (first queue)
         dequeued (rest queue)]
     (if (not (nil? item))
       {:db (assoc db :request-queue dequeued)
        :jsonp-vk-request item}
       (let [new-db (assoc db :loading? false)]
         {:db new-db
          :local-save new-db})))))

(re-frame/reg-event-fx
 ::handle-users-get
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (let [response-users (->> response 
                           (map #(assoc-in {} [(keyword (str (:id %)))] %))
                           (reduce #(merge %1 %2)))
         new-queue (map #(groups-get-request (:id %) (:user-token db)) response)]
     {:db (assoc db
                 :response-users response-users
                 :request-queue new-queue)
      :fx [[:dispatch-later {:ms 250
                             :dispatch [::request-from-queue]}]]})))


;new-queue (if (> (:count response) 1000)
;            (into req-queue
;                  (reduce
;                   #(conj %1 (groups-get-request (:user_id params) (:access_token params) (* %2 1000)))
;                   []
;                   (range 1 (quot (:count response) 1000))))
;            req-queue)

(re-frame/reg-event-fx
 ::handle-groups-get
 (fn [{:keys [db]} [_ {:keys [params response]}]]
   (let [resp-groups (reduce #(into %1 {(keyword (str (:id %2))) %2}) {} (:items response))
         res-groups (reduce #(into %1 {(keyword (str (:id %2))) [(:user_id params)]}) {} (:items response))]
     {:db (assoc db
                 :response-groups (merge (:response-groups db) resp-groups)
                 :result-groups (merge-with into res-groups (:result-groups db)))
      :fx [[:dispatch-later {:ms 250
                             :dispatch [::request-from-queue]}]]})))