-- 1 Row in the query result
SELECT w.day_of_week AS day_of_week, avg(f.arrival_delay) AS delay
  FROM Flights AS f, Weekdays w
 WHERE f.day_of_week_id = w.did
 GROUP BY w.day_of_week
 ORDER BY avg(f.arrival_delay) DESC
 LIMIT 1;
