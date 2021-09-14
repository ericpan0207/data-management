-- The relationship 'insures' is represented in the Vehicles table
-- where there is an attribute called maxLiability. Since this is a
-- many to one relationship, there is no separate relations for 'insures'.

-- The relationship 'drives' is a many to many relation so it has its own
-- separate table whereas the relationship 'operates' is a many to one
-- relation and so it's incorporated in the Truck table.

CREATE TABLE Vehicle (
  licencePlate varchar(50) PRIMARY KEY,
  year integer,
  maxLiability float,
  name varchar(50),
  ssn integer,
  FOREIGN KEY (name) REFERENCES InsuranceCo(name)
  FOREIGN KEY (ssn) REFERENCES Person(ssn)
);

CREATE TABLE Car (
  make varchar(50),
  lp varchar(50),
  FOREIGN KEY (lp) REFERENCES Vehicle(licencePlate)
);

CREATE TABLE Truck (
  capaciy varchar(50),
  lp varchar(50),
  pd_ssn integer,
  FOREIGN KEY (lp) REFERENCES Vehicle(licensePlate),
  FOREIGN KEY (pd_ssn) REFERENCES ProfessionalDriver(pd_ssn)
);

CREATE TABLE InsuranceCo (
  name varchar(50) PRIMARY KEY,
  phone integer
);

CREATE TABLE Person (
  ssn integer PRIMARY KEY,
  name varchar(50)
);

CREATE TABLE Driver (
  driverID integer,
  d_ssn integer,
  FOREIGN KEY (d_ssn) REFERENCES Person(ssn)
);

CREATE TABLE NonProfessionalDriver (
  nd_ssn integer,
  FOREIGN KEY (nd_ssn) REFERENCES Driver(d_ssn)
);

CREATE TABLE ProfessionalDriver (
  medicalHistory varchar(50),
  pd_ssn integer,
  FOREIGN KEY (pd_ssn) REFERENCES Driver(d_ssn)
);

CREATE TABLE Drives (
  nd_ssn integer REFERENCES NonProfessionalDriver,
  lp varchar(50) REFERENCES Car,
  PRIMARY KEY (nd_ssn, lp)
);
