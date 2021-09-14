SELECT DISTINCT f1.origin_city AS city
  FROM Flights AS f1
 WHERE f1.origin_city NOT IN 
	  (SELECT DISTINCT f.origin_city AS origin
		 FROM Flights AS f
	    WHERE f.actual_time >= 180)
 ORDER BY f1.origin_city;

 -- 109 Rows, 31 Seconds
/** 
"Aberdeen SD"
"Abilene TX"
"Alpena MI"
"Ashland WV"
"Augusta GA"
"Barrow AK"
"Beaumont/Port Arthur TX"
"Bemidji MN"
"Bethel AK"
"Binghamton NY"
"Brainerd MN"
"Bristol/Johnson City/Kingsport TN"
"Butte MT"
"Carlsbad CA"
"Casper WY"
"Cedar City UT"
"Chico CA"
"College Station/Bryan TX"
"Columbia MO"
"Columbus GA"
**/