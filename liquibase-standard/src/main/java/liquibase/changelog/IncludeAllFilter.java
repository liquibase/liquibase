package liquibase.changelog;

public interface IncludeAllFilter {
    boolean include(String changeLogPath);
}