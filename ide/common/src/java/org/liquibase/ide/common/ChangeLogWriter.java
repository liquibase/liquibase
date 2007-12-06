package org.liquibase.ide.common;

import liquibase.ChangeSet;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;

public interface ChangeLogWriter {
    void appendChangeSet(ChangeSet changeSet);

    void createEmptyChangeLog(String changeLogFile) throws ParserConfigurationException, IOException;

    void createEmptyChangeLog(OutputStream outputStream) throws ParserConfigurationException, IOException;
}
