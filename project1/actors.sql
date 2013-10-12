CREATE TABLE Actors(
	name varchar(40),
	movie varchar(40),
	year int,
	role varchar(40));

LOAD DATA LOCAL INFILE '~/ebay-data/actors.csv' INTO TABLE Actors
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"';

SELECT name FROM Actors WHERE movie='Die Another Day';

DROP TABLE Actors;
