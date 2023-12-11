/**
 * The change package contains the available database "changes". "Change" was chosen as a term over "refactoring"
 * because refactoring should technically result in the same functionality whereas the database changes do affect
 * functionality.
 * <p></p>
 * Liquibase ships with a set of changes that range from low-level create table style changes to higher level
 * "introduce lookup table" style changes, but additional custom changes can be added via by creating new
 * {@link liquibase.change.Change} implementations and including them in the classpath.
 *
 * @see <a href="http://liquibase.org/extensions">http://liquibase.org/extensions</a>
 */
package liquibase.change;
