CREATE TABLE users (
  user_id BIGINT NOT NULL IDENTITY,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (user_id)
);
