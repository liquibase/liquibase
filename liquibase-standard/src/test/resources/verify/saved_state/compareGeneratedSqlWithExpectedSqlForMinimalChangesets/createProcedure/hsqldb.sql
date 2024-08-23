-- Database: hsqldb
-- Change Parameter: procedureBody=CREATE PROCEDURE new_customer(firstname VARCHAR(50), lastname VARCHAR(50))
--    MODIFIES SQL DATA
--    INSERT INTO CUSTOMERS (first_name, last_name) VALUES (firstname, lastname)
CREATE PROCEDURE new_customer(firstname VARCHAR(50), lastname VARCHAR(50))
   MODIFIES SQL DATA
   INSERT INTO CUSTOMERS (first_name, last_name) VALUES (firstname, lastname);
