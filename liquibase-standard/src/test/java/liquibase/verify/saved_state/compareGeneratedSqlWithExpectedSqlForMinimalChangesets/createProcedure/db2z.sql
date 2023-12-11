-- Database: db2z
-- Change Parameter: procedureBody=CREATE OR REPLACE PROCEDURE testHello
--     IS
--     BEGIN
--       DBMS_OUTPUT.PUT_LINE('Hello From The Database!');
--     END;
CREATE PROCEDURE testHello IS BEGIN DBMS_OUTPUT.PUT_LINE('Hello From The Database!'); END;;
