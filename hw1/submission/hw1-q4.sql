SELECT * FROM MyRestaurants;

.headers on
/* Use comma-separated form for output 
   .mode csv also works */
.mode list
.separator ", "
SELECT * FROM MyRestaurants;

/* Use list form for output */
.mode list
.separator "|"
SELECT * FROM MyRestaurants;

/* Column form with width 15 */
.mode column
.width 15 15 15 15 15
SELECT * FROM MyRestaurants;

/* Turn off headers and print again */
.headers off

/* Use comma-separated form for output 
   .mode csv also works */
.mode list
.separator ", "
SELECT * FROM MyRestaurants;

/* Use list form for output */
.mode list
.separator "|"
SELECT * FROM MyRestaurants;

/* Column form with width 15 */
.mode column
.width 15 15 15 15 15
SELECT * FROM MyRestaurants;
