CREATE TABLE Flights (
  fid integer,
  month_id integer,         -- 1-12
  day_of_month integer,     -- 1-31
  day_of_week_id integer,   -- 1-7, 1 = Monday
  carrier_id varchar(7),
  flight_num integer,
  origin_city varchar(34),
  origin_state varchar(47),
  dest_city varchar(34),
  dest_state varchar(46),
  departure_delay integer,  -- in mins
  taxi_out integer,         -- in mins
  arrival_delay integer,    -- in mins
  canceled integer,         -- 1 means canceled
  actual_time integer,      -- in mins
  distance integer,         -- in miles
  capacity integer,
  price integer,            -- in $
  PRIMARY KEY (fid),
  FOREIGN KEY (carrier_id) REFERENCES Carriers(cid),
  FOREIGN KEY (month_id) REFERENCES Months(mid),
  FOREIGN KEY (day_of_week_id) REFERENCES Weekdays(did)
);

CREATE TABLE Carriers (
  cid varchar(7),
  name varchar(83),
  PRIMARY KEY (cid)
);

CREATE TABLE Months (
  mid int,
  month varchar(9),
  PRIMARY KEY (mid)
);

CREATE TABLE Weekdays (
  did int,
  day_of_week varchar(9),
  PRIMARY KEY (did)
);

PRAGMA foreign_key=ON;
.mode csv
.import ../starter-code/flights-small.csv Flights
.import ../starter-code/carriers.csv Carriers
.import ../starter-code/months.csv Months
.import ../starter-code/weekdays.csv Weekdays
