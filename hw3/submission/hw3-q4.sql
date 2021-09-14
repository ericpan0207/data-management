SELECT DISTINCT f2.dest_city AS city
  FROM Flights AS f1, Flights AS f2
 WHERE f2.dest_city NOT IN
	  (SELECT f.dest_city AS dest_city
	     FROM Flights AS f
		WHERE f.origin_city = '"Seattle WA"')
   AND f1.origin_city = '"Seattle WA"'
   AND f1.dest_city = f2.origin_city
   AND f2.dest_city != '"Seattle WA"';

-- 256 Rows, 21 Seconds
/**
"Wichita Falls TX"
"Manchester NH"
"Ponce PR"
"Knoxville TN"
"Kinston NC"
"Dickinson ND"
"Eugene OR"
"Worcester MA"
"Sioux City IA"
"Charlottesville VA"
"Saginaw/Bay City/Midland MI"
"Billings MT"
"Hays KS"
"Pocatello ID"
"Fayetteville NC"
"Muskegon MI"
"Gainesville FL"
"Bristol/Johnson City/Kingsport TN"
"College Station/Bryan TX"
"Pellston MI"
**/