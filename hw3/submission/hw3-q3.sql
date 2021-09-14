SELECT f.origin_city, 	
	   count(CASE WHEN f.actual_time < 180 THEN 1.0 ELSE NULL END) * 1.0 / count(*) AS percentage
  FROM Flights AS f
 GROUP BY f.origin_city
 ORDER BY percentage;

-- 327 Rows, 9 Seconds
/**
"Guam TT"	0.000000000000
"Pago Pago TT"	0.000000000000
"Aguadilla PR"	0.294339622641
"Anchorage AK"	0.321460373998
"San Juan PR"	0.338903607091
"Charlotte Amalie VI"	0.400000000000
"Ponce PR"	0.419354838709
"Fairbanks AK"	0.506912442396
"Kahului HI"	0.536649985281
"Honolulu HI"	0.549088086922
"San Francisco CA"	0.563076568265
"Los Angeles CA"	0.566041076487
"Seattle WA"	0.577554165533
"Long Beach CA"	0.624541164132
"Kona HI"	0.632821075740
"New York NY"	0.634815197725
"Las Vegas NV"	0.651630092883
"Christiansted VI"	0.653333333333
"Newark NJ"	0.671373555840
"Worcester MA"	0.677419354838
**/