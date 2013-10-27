-- Ajan Jayant, 904039631
-- queries.sql

-- 13422
SELECT COUNT(*)
FROM User;

-- 80
SELECT COUNT(DISTINCT User.UserID)
FROM User
INNER JOIN Item
ON User.UserID = Item.UserID
WHERE BINARY Location = 'New York';

-- 8365
SELECT COUNT(*)
FROM
(SELECT COUNT(*) as c FROM ItemCategory
        GROUP BY ItemID
        HAVING c = 4)
as b;

-- 1046740686
SELECT Bid.ItemID
FROM Bid
INNER JOIN Item
ON Bid.ItemID = Item.ItemID
WHERE Ends > '2001-12-20 00:00:01'
AND Amount =
        (SELECT MAX(Amount) FROM Bid);

-- 3130
SELECT COUNT(DISTINCT User.UserID)
FROM User
INNER JOIN Item
ON User.UserID = Item.UserID
WHERE BINARY Rating > 1000;

-- 6717
SELECT COUNT(DISTINCT Item.UserID)
FROM Item
INNER JOIN Bid
ON Item.UserID = Bid.UserID;

-- 150
SELECT COUNT(DISTINCT Category)
FROM ItemCategory
INNER JOIN Bid
ON ItemCategory.ItemID = Bid.ItemID
WHERE Amount > 100
AND Bid.ItemID
IN
(SELECT Bid.ItemID
FROM Item
INNER JOIN Bid
ON Bid.ItemID = Item.ItemID);