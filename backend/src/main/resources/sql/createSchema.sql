CREATE TABLE IF NOT EXISTS owner
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS horse
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4095),
  date_of_birth DATE NOT NULL,
  sex ENUM('MALE', 'FEMALE') NOT NULL,
  owner_id BIGINT,
  FOREIGN KEY (owner_id) REFERENCES owner(id)
);

CREATE TABLE IF NOT EXISTS horse_parent
(
  horse_id BIGINT NOT NULL,
  parent_id BIGINT NOT NULL,
  PRIMARY KEY (horse_id, parent_id),
  FOREIGN KEY (horse_id) REFERENCES horse(id) ON DELETE CASCADE,
  FOREIGN KEY (parent_id) REFERENCES horse(id),
  CHECK (horse_id != parent_id)
);
