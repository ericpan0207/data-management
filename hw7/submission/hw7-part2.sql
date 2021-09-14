CREATE TABLE if not exists Sales (
  name varchar(10),
  discount varchar(4),
  month varchar(3),
  price integer
);

.separator \t
.import mrFrumbleData.txt Sales

-- name -> price, no rows returned 
Select s.name
  From Sales s
Group By s.name
Having count(distinct s.price) > 1;

-- month -> discount, no rows returned 
Select s.month
  From Sales s
Group By s.month
Having count(distinct s.discount) > 1;

-- Based on the two FDs above: we also have name,month -> price,discount
--                                        name,discount -> price
--                                        name,month -> price
--                                        month,price -> discount
--                                        name,discount,month -> price
--                                        name,month,price -> discount

-- For sanity check:
-- name,month -> price,discount, no rows returned 
Select s.name, s.month
  From Sales s
Group By s.name, s.month
Having count(distinct s.price) > 1 or count(distinct s.discount) > 1;

-- name,discount -> price, no rows returned 
Select s.name, s.discount
  From Sales s
Group By s.name, s.discount
Having count(distinct s.price) > 1;

-- name,month -> price, no rows returned 
Select s.name, s.month
  From Sales s
Group By s.name, s.month
Having count(distinct s.price) > 1;

-- month,price -> discount, no rows returned 
Select s.month, s.price
  From Sales s
Group By s.month, s.price
Having count(distinct s.discount) > 1;

-- name,discount,month -> price, no rows returned 
Select s.name, s.discount, s.month
  From Sales s
Group By s.name, s.discount, s.month
Having count(distinct s.price) > 1;

-- name,month,price -> discount, no rows returned
Select s.name, s.month, s.price
  From Sales s
Group By s.name, s.month, s.price
Having count(distinct s.discount) > 1;


CREATE TABLE if not exists Product (
  name varchar(10) PRIMARY KEY,
  price integer
);

CREATE TABLE if not exists Sale (
  month varchar(3) PRIMARY KEY,
  discount varchar(4)
);

CREATE TABLE if not exists OnSale (
  os_name varchar(10),
  os_month varchar(3),
  FOREIGN KEY (os_name) REFERENCES Product (name),
  FOREIGN KEY (os_month) REFERENCES Sale (month)
);

INSERT INTO Product (name, price)
SELECT DISTINCT s.name, s.price
  FROM Sales AS s;

INSERT INTO Sale (month, discount)
SELECT DISTINCT s.month, s.discount
  FROM Sales AS s;

INSERT INTO OnSale (os_name, os_month)
SELECT DISTINCT s.name, s.month
  FROM Sales As s;

-- 37 Rows
SELECT count(*) From Product;
-- 13 Rows
SELECT count(*) From Sale;
-- 427 Rows
Select count(*) From OnSale;
