SELECT * 
  FROM MyRestaurants 
 WHERE (rating = 1) 
   AND (date('now', '-3 month') > lastVisit);
