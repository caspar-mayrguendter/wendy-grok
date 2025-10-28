-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

-- Delete in reverse dependency order: children first, then parents, then grandparents
DELETE FROM horse_parent where horse_id IN (-9, -10, -11, -12) OR parent_id IN (-9, -10, -11, -12); -- children relationships
DELETE FROM horse_parent where horse_id IN (-1, -6, -7, -8) OR parent_id IN (-1, -6, -7, -8); -- parents relationships
DELETE FROM horse_parent where horse_id IN (-2, -3, -4, -5) OR parent_id IN (-2, -3, -4, -5); -- grandparents relationships
DELETE FROM horse where id IN (-9, -10, -11, -12); -- children
DELETE FROM horse where id IN (-1, -6, -7, -8); -- parents (Wendy is -1 now)
DELETE FROM horse where id IN (-2, -3, -4, -5); -- grandparents
DELETE FROM owner where id < 0;

-- Insert owners first (referenced by horses)
INSERT INTO owner (id, first_name, last_name, email) VALUES
(-1, 'John', 'Smith', 'john.smith@email.com'),
(-2, 'Sarah', 'Johnson', 'sarah.j@email.com'),
(-3, 'Michael', 'Brown', 'm.brown@horses.com'),
(-4, 'Emma', 'Davis', 'emma.davis@email.com'),
(-5, 'Robert', 'Wilson', 'r.wilson@stable.com'),
(-6, 'Lisa', 'Garcia', 'lisa.garcia@email.com'),
(-7, 'David', 'Martinez', 'david.m@email.com'),
(-8, 'Jennifer', 'Anderson', 'j.anderson@horses.com'),
(-9, 'James', 'Taylor', 'james.taylor@email.com'),
(-10, 'Maria', 'Rodriguez', 'maria.r@email.com'),
(-11, 'Christopher', 'Lee', 'chris.lee@stable.com'),
(-12, 'Amanda', 'White', 'amanda.white@email.com');

-- Insert horses in dependency order: grandparents first, then parents, then children
-- Grandparent generation (oldest - no dependencies)
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id) VALUES
(-2, 'Thunder', 'Grand champion racehorse, sire of many winners', '2000-03-15', 'MALE', -1),
(-3, 'Lightning', 'Legendary broodmare, dam of champions', '2001-05-20', 'FEMALE', -2),
(-4, 'Storm', 'Powerful stallion with championship pedigree', '1999-08-10', 'MALE', -3),
(-5, 'Blaze', 'Exceptional mare known for her speed', '2002-01-25', 'FEMALE', -4);

-- Parent generation (middle - depends on grandparents)
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id) VALUES
(-1, 'Wendy', 'The famous one!', '2012-12-12', 'FEMALE', -5),
(-6, 'Comet', 'Fast and agile racehorse', '2010-07-08', 'MALE', -6),
(-7, 'Star', 'Graceful show horse', '2011-09-14', 'FEMALE', -7),
(-8, 'Rocket', 'Powerful jumper', '2009-11-30', 'MALE', -8);

-- Children generation (youngest - depends on parents)
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id) VALUES
(-9, 'Nova', 'Promising young filly with great potential', '2018-04-22', 'FEMALE', -9),
(-10, 'Blitz', 'Energetic colt learning to race', '2019-06-15', 'MALE', -10),
(-11, 'Luna', 'Beautiful gray mare with a gentle temperament', '2020-02-28', 'FEMALE', -11),
(-12, 'Titan', 'Strong young stallion in training', '2021-08-12', 'MALE', -12);

-- Insert parent relationships
INSERT INTO horse_parent (horse_id, parent_id) VALUES
-- Wendy has parents Thunder (father) and Lightning (mother)
(-1, -2), (-1, -3),
-- Comet has parents Storm (father) and Blaze (mother)
(-6, -4), (-6, -5),
-- Star has parents Storm (father) and Lightning (mother)
(-7, -4), (-7, -3),
-- Rocket has parents Thunder (father) and Blaze (mother)
(-8, -2), (-8, -5),
-- Nova has parents Wendy (mother) and Comet (father)
(-9, -1), (-9, -6),
-- Blitz has parents Star (mother) and Rocket (father)
(-10, -7), (-10, -8),
-- Luna has parents Wendy (mother) and Rocket (father)
(-11, -1), (-11, -8),
-- Titan has parents Star (mother) and Comet (father)
(-12, -7), (-12, -6);
