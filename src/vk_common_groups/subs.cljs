(ns vk-common-groups.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub 
 ::user-token
 (fn [db]
   (:user-token db)))

(re-frame/reg-sub 
 ::current-route
 (fn [db]
   (:current-route db)))

(re-frame/reg-sub
 ::inputs
 (fn [db]
   (:inputs db)))

(re-frame/reg-sub
 ::all-input-ids
 (fn [db]
   (keys (:inputs db))))

(re-frame/reg-sub
 ::input
 (fn [db [_ id]]
   (get-in db [:inputs id])))

(re-frame/reg-sub 
 ::loading?
 (fn [db]
   (:loading? db)))

(re-frame/reg-sub
 ::result-groups
 (fn [db]
   (:result-groups db)))

(re-frame/reg-sub
 ::result-groups-sorted
 :<- [::result-groups]
 (fn [groups _]
   (sort-by #(count (val %)) > 
            (filter #(>= (count (second %)) 2) groups))))

(re-frame/reg-sub 
 ::response-groups
 (fn [db]
   (:response-groups db)))

(re-frame/reg-sub 
 ::response-users 
 (fn [db]
   (:response-users db)))