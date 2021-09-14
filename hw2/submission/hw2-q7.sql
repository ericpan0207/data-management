-- 1 Row in the query result
SELECT sum(f.capacity) AS capacity
  FROM Flights AS f, Months AS m
 WHERE f.month_id = m.mid
   AND ((f.origin_city = "Seattle WA"
       AND f.dest_city = "San Francisco CA")
        OR (f.origin_city = "San Francisco CA"
       AND f.dest_city = "Seattle WA"))
   AND m.month = "July"
   AND f.day_of_month = 10;
