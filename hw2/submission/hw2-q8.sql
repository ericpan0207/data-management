-- 22 Rows in the result query
SELECT c.name AS name, sum(f.departure_delay) AS delay
  FROM Flights AS f, Carriers AS c
 WHERE f.carrier_id = c.cid
 GROUP BY c.name;
