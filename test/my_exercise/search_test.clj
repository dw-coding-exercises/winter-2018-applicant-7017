(ns my-exercise.search-test
      (:require [clojure.test :refer :all]
                [my-exercise.search :refer :all]
                [clojure.string :as str]
                [my-exercise.search :as search]))

"TO DO: Validation tests of formatting for city and county names with spaces and punctuation"
(deftest search-test
         (testing "Are city and county names being propertly formatted for transport?"
               (is (= (search/format-for-ocd "San Diego") "san_diego")))
      (testing "some of the best states and districts are present"
            (is (every? (set postal-abbreviations)
                        #{"CO" "NY" "CA" "KS" "DC" "IL" "WA"}))))
