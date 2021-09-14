SELECT DISTINCT fc.name AS carrier
  FROM (SELECT * 
		  FROM Flights as f, Carriers AS c
		 WHERE f.carrier_id = c.cid) AS fc
 WHERE fc.origin_city = '"Seattle WA"'
   AND fc.dest_city = '"San Francisco CA"';

-- 4 Rows, 6 Seconds
/**
Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America
**/