-- add all your SQL setup statements here. 

-- You can assume that the following base table has been created with data loaded for you when we test your submission 
-- (you still need to create and populate it in your instance however),
-- although you are free to insert extra ALTER COLUMN ... statements to change the column 
-- names / types if you like.

-- CREATE TABLE FLIGHTS
-- (
--  fid int NOT NULL PRIMARY KEY,
--  year int,
--  month_id int,
--  day_of_month int,
--  day_of_week_id int,
--  carrier_id varchar(3),
--  flight_num int,
--  origin_city varchar(34),
--  origin_state varchar(47),
--  dest_city varchar(34),
--  dest_state varchar(46),
--  departure_delay double precision,
--  taxi_out double precision,
--  arrival_delay double precision,
--  canceled int,
--  actual_time double precision,
--  distance double precision,
--  capacity int,
--  price double precision
--)
CREATE TABLE Users
(
  username varchar(64) NOT NULL PRIMARY KEY,
  password varchar(64),
  balance int
)

CREATE TABLE Reservations
(
  reserve_id  int         IDENTITY(1, 1)  PRIMARY KEY,
  user_id     varchar(64)                 FOREIGN KEY REFERENCES Users(username),
  paid        int         DEFAULT 0,      -- 0 FOR FALSE, 1 FOR TRUE
  r_fid1      int         NOT NULL        FOREIGN KEY REFERENCES Flights(fid),
  r_fid2      int          FOREIGN KEY REFERENCES Flights(fid)
)

CREATE INDEX o_city ON Flights(origin_city)
CREATE INDEX d_city ON Flights(dest_city)
