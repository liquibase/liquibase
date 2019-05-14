package liquibase.statement.core;

@FunctionalInterface
public interface CurrentDateTimeFunction {
	String getTime();
}
