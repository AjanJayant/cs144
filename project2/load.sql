-- Ajan Jayant, 904039631
-- load.sql

LOAD DATA LOCAL INFILE 'nodupItem.dat' INTO TABLE Item
FIELDS TERMINATED BY '|*|';

LOAD DATA LOCAL INFILE 'nodupUser.dat' INTO TABLE User
FIELDS TERMINATED BY '|*|';

LOAD DATA LOCAL INFILE 'nodupItemCategory.dat' INTO TABLE ItemCategory
FIELDS TERMINATED BY '|*|';

LOAD DATA LOCAL INFILE 'nodupBid.dat' INTO TABLE Bid
FIELDS TERMINATED BY '|*|';