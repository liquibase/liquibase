/**
 * Technically, a Change expresses an intention for alter the structure of the Database. An example would be:
 * "Drop all foreign keys from a table". However, it abstracts from the way this is actually archieved: While one
 * DBMS might provide a syntax for that (maybe something like "ALTER TABLE myTable DROP ALL FOREIGN KEYS"), others
 * do not have a 1:1 translation and need to perform the change in several steps (e.g. enumerating all foreign keys
 * and then dropping them one-by-one).<p>
 * <p>
 * This package contains all the possible basic Changes that should be possible with every DBMS supported by
 * Liquibase. Most classes here extend the {@link liquibase.change.AbstractChange} class that provides the general
 * frame for the Change.
 */
package liquibase.change.core;
