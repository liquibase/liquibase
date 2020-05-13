/**
 * Although Liquibase tries to provide a wide range of database refactorings, there are times you may want to create
 * your own custom refactoring class.<p>
 * <p>
 * To create your own custom refactoring, simply create a class that implements the
 * {@link liquibase.change.custom.CustomSqlChange} or {@link liquibase.change.custom.CustomTaskChange} interface and
 * use the &lt;custom&gt; tag in your change set.<p>
 * <p>
 * If your change can be rolled back, implement the {@link liquibase.change.custom.CustomSqlRollback} interface as
 * well.<p>
 * <p>
 * For a sample custom change class, see liquibase.change.custom.ExampleCustomSqlChange in the test sources.
 */
package liquibase.change.custom;
