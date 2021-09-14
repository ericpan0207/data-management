SELECT DISTINCT f.dest_city AS city
  FROM Flights AS f
 WHERE NOT EXISTS 
	  (SELECT *
	     FROM Flights AS f1
		WHERE f1.origin_city = '"Seattle WA"'
		  AND f1.dest_city = f.dest_city)
   AND NOT EXISTS
      (SELECT *
	     FROM Flights AS stop1, Flights AS stop2
		WHERE stop1.origin_city = '"Seattle WA"'		  
		  AND stop1.dest_city = stop2.origin_city
		  AND stop2.dest_city = f.dest_city);

-- 3 Rows, 4 Minute 50 Seconds
/**
"Devils Lake ND"
"Hattiesburg/Laurel MS"
"St. Augustine FL"
**/