(ns my-exercise.search
      (:require [hiccup.page :refer [html5]]
                [ring.util.anti-forgery :refer [anti-forgery-field]]
                [clojure.string :as str]
                [clj-http.client :as client]
                [my-exercise.home :as home]
                [my-exercise.us-state :as us-state]))

"Create a simple data structure for holding an address"
(defstruct Address :street :street-2 :state :city :zip :county)

"Shorthand for checking a non-empty string"
(def has-value? (complement str/blank?))

"Constant: The basis for all OCD codes"
(def base-ocd-code "ocd-division/country:us")

(defn address-map [request]
      "Process the incoming request parameters into a struct map"
      (struct-map Address
            :state (get (:params request) :state)
            :city (get (:params request) :city)
            :county (get (:params request) :county)
            :zip (get (:params request) :zip)))

(defn format-for-ocd [data]
      (str/replace (str/lower-case data) #" " "_"))

(defn get-state-ocd-code [state]
      "Return the applicable OCD ID for a state, validating against the state/territory list"
      "TO DO: Validate against the US States list"
      (if (has-value? state)
            (format "%s/state:%s" base-ocd-code (format-for-ocd state))
            nil))

(defn get-city-ocd-code [state city]
      "Return the applicable OCD ID for a city/state combo"
      (def state-code (get-state-ocd-code state))
      (if (and (has-value? state-code) (has-value? city))
            (format "%s/place:%s" state-code (format-for-ocd city))
            nil))

(defn get-county-ocd-code [state county]
      "Return the applicable OCD ID for a county/state combo"
      (def state-code (get-state-ocd-code state))
      (if (and (has-value? state-code) (has-value? county))
            (format "%s/county:%s" state-code (format-for-ocd county))
            nil))

(defn get-address-ocd-codes [address-map]
      "Given an address, return all of its applicable OCD IDs as a comma-delimited string"
      (def state (:state address-map))
      (def ocd-codes (filter has-value? [(get-state-ocd-code state)
                                       (get-city-ocd-code state (:city address-map))
                                       (get-county-ocd-code state (:county address-map))]))
      (str/join "," ocd-codes))

(defn get-elections [ocd-codes]
      "Given the formatted query string, executes the HTTP request to get the election information"
      "TO DO: Actually parse out meaningful pieces of this data to show a voter on the page"
      "TO DO: Add exception handling for failed requests"
      (client/get "https://api.turbovote.org/elections/upcoming"
            {:query-params {:district-divisions ocd-codes} }))

(defn search-results
      [ocd-codes]
      "Writes the portion of the web page content containing the election results returned."
      [:div
       [:h1 "Your Elections"]
       "TO DO: Display something nicer than a raw dump of the HTTP response"
       ocd-codes
       (get-elections ocd-codes)
       ])

(defn page [request]
      "Main function for the search page"

      "Generate the server data"
      (def address-map (address-map request))
      (def ocd-codes (get-address-ocd-codes address-map))

      "Render the page using the server data"
      (html5
            (home/header)
            (search-results ocd-codes)))