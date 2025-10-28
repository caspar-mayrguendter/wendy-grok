-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- positive IDs are used starting from high numbers to not interfere with user-entered data

-- Delete all existing data before inserting test data
DELETE FROM horse_parent;
DELETE FROM horse;
DELETE FROM owner;

-- Insert owners first (referenced by horses)
INSERT INTO owner (id, first_name, last_name, email) VALUES
(1001, 'John', 'Smith', 'john.smith@email.com'),
(1002, 'Sarah', 'Johnson', 'sarah.j@email.com'),
(1003, 'Michael', 'Brown', 'm.brown@horses.com'),
(1004, 'Emma', 'Davis', 'emma.davis@email.com'),
(1005, 'Robert', 'Wilson', 'r.wilson@stable.com'),
(1006, 'Lisa', 'Garcia', 'lisa.garcia@email.com'),
(1007, 'David', 'Martinez', 'david.m@email.com'),
(1008, 'Jennifer', 'Anderson', 'j.anderson@horses.com'),
(1009, 'James', 'Taylor', 'james.taylor@email.com'),
(1010, 'Maria', 'Rodriguez', 'maria.r@email.com'),
(1011, 'Christopher', 'Lee', 'chris.lee@stable.com'),
(1012, 'Amanda', 'White', 'amanda.white@email.com');

-- Insert horses in dependency order: grandparents first, then parents, then children
-- Grandparent generation (oldest - no dependencies)
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id) VALUES
(2002, 'Thunder', 'Grand champion racehorse, sire of many winners', '2000-03-15', 'MALE', 1001),
(2003, 'Lightning', 'Legendary broodmare, dam of champions', '2001-05-20', 'FEMALE', 1002),
(2004, 'Storm', 'Powerful stallion with championship pedigree', '1999-08-10', 'MALE', 1003),
(2005, 'Blaze', 'Exceptional mare known for her speed', '2002-01-25', 'FEMALE', 1004);

-- Parent generation (middle - depends on grandparents)
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id) VALUES
(2001, 'Wendy', 'The famous one!', '2012-12-12', 'FEMALE', 1005),
(2006, 'Comet', 'Fast and agile racehorse', '2010-07-08', 'MALE', 1006),
(2007, 'Star', 'Graceful show horse', '2011-09-14', 'FEMALE', 1007),
(2008, 'Rocket', 'Powerful jumper', '2009-11-30', 'MALE', 1008);

-- Children generation (youngest - depends on parents)
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id) VALUES
(2009, 'Nova', 'Promising young filly with great potential', '2018-04-22', 'FEMALE', 1009),
(2010, 'Blitz', 'Energetic colt learning to race', '2019-06-15', 'MALE', 1010),
(2011, 'Luna', 'Beautiful gray mare with a gentle temperament', '2020-02-28', 'FEMALE', 1011),
(2012, 'Titan', 'Strong young stallion in training', '2021-08-12', 'MALE', 1012);

-- Insert parent relationships
INSERT INTO horse_parent (horse_id, parent_id) VALUES
-- Wendy has parents Thunder (father) and Lightning (mother)
(2001, 2002), (2001, 2003),
-- Comet has parents Storm (father) and Blaze (mother)
(2006, 2004), (2006, 2005),
-- Star has parents Storm (father) and Lightning (mother)
(2007, 2004), (2007, 2003),
-- Rocket has parents Thunder (father) and Blaze (mother)
(2008, 2002), (2008, 2005),
-- Nova has parents Wendy (mother) and Comet (father)
(2009, 2001), (2009, 2006),
-- Blitz has parents Star (mother) and Rocket (father)
(2010, 2007), (2010, 2008),
-- Luna has parents Wendy (mother) and Rocket (father)
(2011, 2001), (2011, 2008),
-- Titan has parents Star (mother) and Comet (father)
(2012, 2007), (2012, 2006);
