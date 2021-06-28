GRANT ALL PRIVILEGES ON lbcat.* TO 'lbuser'@'%';

DROP TABLE IF EXISTS `authors`;
CREATE TABLE `authors` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `first_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
                           `last_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
                           `email` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
                           `birthdate` date NOT NULL,
                           `added` timestamp NOT NULL DEFAULT current_timestamp(),
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `authors` VALUES ('1','Courtney','Hodkiewicz','borer.edison@example.org','1986-01-22','1983-08-23 14:55:09'),
                             ('2','Marielle','Kuhlman','llakin@example.org','1995-08-08','1984-03-05 01:25:02'),
                             ('3','Emmanuel','Gleichner','jean.zemlak@example.net','1997-05-09','1977-08-09 10:28:04'),
                             ('4','Hertha','Goodwin','hollis.gusikowski@example.org','2014-08-21','2009-01-28 11:02:56'),
                             ('5','Ewald','Sauer','juvenal35@example.com','1988-10-10','2000-11-02 00:37:53');


DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts` (
                         `id` int(11) NOT NULL,
                         `author_id` int(11) NOT NULL,
                         `title` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
                         `description` varchar(500) COLLATE utf8_unicode_ci NOT NULL,
                         `content` text COLLATE utf8_unicode_ci NOT NULL,
                         `inserted_date` date
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `posts` VALUES ('1','1','sit','in','At corporis est sint beatae beatae.','1996-05-04'),
                           ('2','2','nisi','et','Sunt nemo magni et tenetur debitis blanditiis.','2000-05-25'),
                           ('3','3','ratione','blanditiis','Ipsa distinctio doloremque et ut.','1997-09-22'),
                           ('4','4','ad','et','Repudiandae porro explicabo officiis sed quis voluptate et.','1978-12-13'),
                           ('5','5','deserunt','temporibus','Mollitia reiciendis debitis est voluptatem est neque.','1979-12-06');