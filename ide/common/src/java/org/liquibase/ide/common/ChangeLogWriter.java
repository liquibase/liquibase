package org.liquibase.ide.common;

import liquibase.ChangeSet;

public interface ChangeLogWriter {
    void appendChangeSet(ChangeSet changeSet);
}
