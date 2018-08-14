CREATE user lbuser@localhost identified by 'lbuser';

CREATE DATABASE lbcat;
GRANT ALL PRIVILEGES ON lbcat.* TO 'lbuser'@'localhost';

CREATE DATABASE lbcat2;
GRANT ALL PRIVILEGES ON lbcat2.* TO 'lbuser'@'localhost';

FLUSH privileges;
