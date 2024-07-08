package org.liquibase.maven.plugins;

import liquibase.ui.ConsoleUIService;
import org.apache.maven.plugin.logging.Log;

public class MavenUi extends ConsoleUIService {
    private final Log log;

    public MavenUi(Log log) {
        this.log = log;
    }

    @Override
    public void sendMessage(String message) {
        log.info(message);
    }

    @Override
    public void sendErrorMessage(String message) {
        log.error(message);
    }

    @Override
    public void sendErrorMessage(String message, Throwable exception) {
        log.error(message, exception);
    }
}
