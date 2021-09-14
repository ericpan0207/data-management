-- 3 Rows in the query result
SELECT c.name AS carrier, max(f.price) AS max_price
  FROM Flights AS F, Carriers AS c
 WHERE f.carrier_id = c.cid
   AND ((f.origin_city = "Seattle WA"
       AND f.dest_city = "New York NY")
        OR (f.origin_city = "New York NY"
       AND f.dest_city = "Seattle WA"))      
 GROUP BY c.name;
