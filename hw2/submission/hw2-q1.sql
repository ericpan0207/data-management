-- 3 Rows in the query result
SELECT DISTINCT flight_num AS flight_num
  FROM Flights AS f, Carriers AS c, Weekdays AS w
 WHERE f.origin_city = "Seattle WA"
   AND f.dest_city = "Boston MA"
   AND f.carrier_id = c.cid
   AND c.name = "Alaska Airlines Inc."
   AND f.day_of_week_id = w.did
   AND w.day_of_week = "Monday";
