package org.liquibase.ide.common;

public interface ProgressMonitor {
    void beginTask(String title, int workTotal);

    void subTask(String title);

    void worked(int amount);

    void done();
}
