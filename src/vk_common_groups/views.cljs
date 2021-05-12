(ns vk-common-groups.views
  (:require
   [re-frame.core :as re-frame]
   [reitit.core :as r]
   [reitit.frontend.easy :as rfe]
   [vk-common-groups.styles :as styles]
   [vk-common-groups.subs :as subs]
   [vk-common-groups.events :as events]
   [vk-common-groups.config :as config]))

(defn route-href
  "Return relative url for given route. Url can be used in HTML links."
  ([k]
   (route-href k nil nil))
  ([k params]
   (route-href k params nil))
  ([k params query]
   (rfe/href k params query)))

(defn auth-panel []
  [:div "Loading..."])

(defn about-panel []
  [:div.block
   [:div.box
    "This app checks groups that users have in common."]])

(defn input-text
  [value placeholder callback-fn]
  [:input.block.input
   {:type      "text"
    :value     value
    :placeholder placeholder
    ;:class (when (not (str/blank? value)) "has-background-light")
    :on-change #(callback-fn (-> % .-target .-value))}])

(defn vk-group-box
  [result-group resp-group users]
  [:div.column.is-4
   [:div.box
    [:div.columns
     [:div.column.is-3
      [:figure.image.is-96x96
      [:img {:src (get resp-group :photo_100)}]]]
     [:div.column
      [:a {:target "_blank"
          :href (str "https://vk.com/" (:screen_name resp-group))}
      (:name resp-group)]
     [:p (:members_count resp-group) " members"]]]
    [:div.columns.is-multiline
     (for [user-id result-group
           :let [user (get users (keyword (str user-id)))]]
       ^{:key user-id} [:div.column.is-4
                        [:figure.image.is-48x48
                         [:img.is-rounded {:src (:photo_50 user)}]]
                        [:div
                         [:a
                          {:target "_blank" :href (str "https://vk.com/id" user-id)}
                          (:first_name user) " " (:last_name user)]]])]]])

(defn input-vk-user
  [id]
  (let [input @(re-frame/subscribe [::subs/input id])]
    [:div.field.has-addons
     [:p.control
      [:a.button.is-static "vk.com/"]]
     [:p.control.is-expanded
      (input-text
       input
       "thedimzone"
       #(re-frame/dispatch [::events/change-input id %]))]]))

(defn result-groups
  []
  (let [sorted-groups @(re-frame/subscribe [::subs/result-groups-sorted])
        users @(re-frame/subscribe [::subs/response-users])
        resp-groups @(re-frame/subscribe [::subs/response-groups])]
    (when (not (empty? sorted-groups))
      [:div
       [:hr]
       [:div.columns.is-multiline
        (for [group sorted-groups
              :let [group-id (first group)]]
          ^{:key group-id} [vk-group-box (second group) (get resp-groups group-id) users])]])))

(defn main-panel []
  (let [ids @(re-frame/subscribe [::subs/all-input-ids])
        loading? @(re-frame/subscribe [::subs/loading?])
        user-token @(re-frame/subscribe [::subs/user-token])]
    (if user-token
      [:div
       [:div.columns
        [:div.column.is-8
         (for [id ids]
           ^{:key id} [input-vk-user id])]
        [:div.column
         [:button.button.is-primary.is-fullwidth
          {:disabled (if (>= (count ids) 3) false true)
           :on-click #(re-frame/dispatch [::events/submit])
           :class (when loading? "is-loading")}
          "Submit"]
         (when (< (count ids) 3)
           [:p "Input two or more users"])]]
       (result-groups)]
      [:div.box "Please log in"])))

(defn menu-component [{:keys [router current-route]}]
  (for [route-name (r/route-names router)
        :let       [route (r/match-by-name router route-name)
                    text (-> route :data :link-text)
                    include? (-> route :data :menu-include?)]]
    (when include?
      [:a.navbar-item
       {:key route-name
        :href (route-href route-name)
        :class (when (= route-name (-> current-route :data :name)) "is-active")}
       text])))

(defn wrapper-panel [{:keys [router]}]
  (let [current-route @(re-frame/subscribe [::subs/current-route])
        user-token @(re-frame/subscribe [::subs/user-token])]
    [:div
     [:nav.navbar.is-light.container.is-fluid.block
      [:div.container
       [:div.navbar-brand
        [:div.navbar-item "VK Common Groups"]]
       [:div.navbar-menu
        [:div.navbar-start
         (menu-component {:router router :current-route current-route})]
        [:div.navbar-end 
         (if user-token
           [:div.navbar-item
            [:div.level
             [:div.level-left
              [:div.level-item "You are logged in"]]
             [:div.level-right
              [:div.level-item " "]
              [:a.button.level-item {:href "" :on-click #(re-frame/dispatch [::events/logout])} "Logout"]]]]
           [:div.navbar-item
            [:a.button {:href config/vk-auth-url} "Login via VK"]])]]]]
     [:div.container (when current-route
                   [(-> current-route :data :view)])]]))

;[:footer.has-background-light {:class (styles/myfooter)}
;  [:div
;   [:a {:href "https://vk.com/thedimzone" :target "_blank"} "Dima"]]]