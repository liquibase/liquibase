DROP TYPE IF EXISTS mpaa_rating;
DROP TYPE IF EXISTS year;
CREATE TYPE mpaa_rating AS (car varchar);
CREATE TYPE year AS (f1 int4,f2 text);
