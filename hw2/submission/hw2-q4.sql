-- 12 Rows in the query result
SELECT distinct c.name AS name --, m.month, f.day_of_month
  FROM Flights f, Carriers c, Months m
 WHERE f.carrier_id = c.cid AND f.month_id = m.mid
 GROUP BY c.name, m.month, f.day_of_month
HAVING count(*) > 1000;
