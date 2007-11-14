package org.liquibase.ide.common;

import liquibase.ChangeSet;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public interface ChangeLogWriter {
    void appendChangeSet(ChangeSet changeSet);

    void createEmptyChangeLog(File file) throws ParserConfigurationException, IOException;
}
