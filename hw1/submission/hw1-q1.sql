CREATE TABLE Edges (Source integer, Destination integer);

INSERT INTO Edges (Source, Destination) VALUES (10, 5);
INSERT INTO Edges VALUES (6, 25);
INSERT INTO Edges VALUES (1, 3), (4, 4);

SELECT * FROM Edges;
SELECT Source FROM Edges;
SELECT * FROM Edges WHERE Source > Destination;

INSERT INTO Edges VALUES ('-1', '2000');

/*
No error is displayed since sqlite automatically converts the text data into integer before inserting into the table.
*/
