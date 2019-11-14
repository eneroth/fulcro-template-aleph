(ns poc.session.model)

;; "Database"
;; ##################################
(def users
  {1 {:user/id 1
      :user/password "letmein"
      :contact/email "loretta@example.com"
      :person/given-names "Loretta"
      :person/surname "Fong-Phrymp"}
   2 {:user/id 2
      :user/password "letmein"
      :contact/email "foo@bar.com"
      :person/given-names "Foo"
      :person/surname "Bar"}})



;; User resolvers
;; ##################################
(def user-return-fields
  ;; Declares the fields to select from the user database when
  ;; returning information to the frontend. Essentially a screening
  ;; list to ensure that passwords etc. are not returned.
  [:user/id
   :contact/email
   :person/given-names
   :person/surname])


(def nil-user
  ;; Used to return an empty user if login fails.
  (reduce #(assoc %1 %2 nil) {} user-return-fields))
