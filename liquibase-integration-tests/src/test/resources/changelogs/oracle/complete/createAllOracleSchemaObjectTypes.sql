SELECT DISTINCT object_type FROM all_objects ORDER BY 1;

SELECT * FROM dba_objects WHERE object_name LIKE 'BIN$%';

SELECT * FROM user_recyclebin;

CREATE CLUSTER personnel
   (department NUMBER(4))
SIZE 512 
STORAGE (initial 100K next 50K);


CREATE AND COMPILE JAVA SOURCE NAMED "HelloWorld" AS
   public class WelcomeWorld {
      public static String hello() {
         return "Hello, World!";   } }
/


CREATE FUNCTION number_is_different(num1 NUMBER, num2 NUMBER) 
RETURN NUMBER AS
BEGIN
   IF num1 <> num2 THEN 
     RETURN 1;
   ELSE
     RETURN 0;
   END IF;
END;
/

CREATE OPERATOR diff_op
   BINDING (NUMBER, NUMBER) 
   RETURN NUMBER 
   USING number_is_different; 
   
/* @todo: CREATE INDEXTYPE */

CREATE EDITION some_edition;

DROP EDITION some_edition;
ALTER SESSION SET EDITION=some_edition;   

CREATE EDITIONING VIEW e_view AS
  SELECT 42 AS dummy FROM sys.DUAL;
ALTER SESSION SET EDITION=ORA$BASE;

CREATE OR REPLACE PACKAGE pkg_test
IS
  PROCEDURE hello_world;
END;
/

CREATE OR REPLACE PACKAGE BODY pkg_test
IS
  PROCEDURE hello_world IS
  BEGIN
    DBMS_OUTPUT.PUT_LINE('Hello, world!');
  END;
END;
/

CREATE CONTEXT ctx_test USING pkg_test;
DROP CONTEXT ctx_test;
  