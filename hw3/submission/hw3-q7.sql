SELECT DISTINCT c.name AS carrier
  FROM Flights AS f, Carriers AS c
 WHERE f.carrier_id = c.cid
   AND f.origin_city = '"Seattle WA"'
   AND f.dest_city = '"San Francisco CA"'

-- 4 Rows, 6 Seconds
/**
Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America
**/