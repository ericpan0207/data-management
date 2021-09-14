-- 6 Rows in the query result
SELECT c.name AS name, count(*) * 1.0 / tot AS percent
  FROM Flights AS f, Carriers AS c, (SELECT c.name AS airline, count(*) AS tot
                                 FROM Flights f, Carriers c
                                WHERE f.carrier_id = c.cid
                                  AND f.origin_city = "Seattle WA"
                                GROUP BY c.name)
 WHERE f.carrier_id = c.cid
   AND c.name = airline
   AND f.origin_city = "Seattle WA"
   AND f.canceled = 1
 GROUP BY c.name
HAVING count(*) * 1.0 / tot > 0.005
 ORDER BY count(*) * 1.0 / tot;
