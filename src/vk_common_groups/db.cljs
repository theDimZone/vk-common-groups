(ns vk-common-groups.db)

(def default-db
  {:user-token nil
   :user-id nil
   :current-route nil
   :loading? false
   :inputs {0 ""}
   :request-queue []
   :response-users {}
   :response-groups {}
   :result-groups {}})
