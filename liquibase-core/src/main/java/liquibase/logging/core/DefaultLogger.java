package liquibase.logging.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation now based on slf4j API.
 * <p/>
 * <p>
 * Important notes:
 * <ol>
 * <li>
 * The actual binding to a slf4j implementation is left to the user. We recommend logback,
 * and that's why logback classic is added as an optional dependency
 * </li>
 * <li>
 * This code may just as well be merged with the base class, AbstractLogger, we kept the override
 * for convenience only.
 * </li>
 * </ol>
 * </p>
 */
public class DefaultLogger extends AbstractLogger {

    private static final String DEFAULT_NAME = "liquibase";

    private String changeLogName = null;
    private String changeSetName = null;

    private Logger logger;

    public DefaultLogger() {
        logger = LoggerFactory.getLogger(DEFAULT_NAME);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void setName(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    @Override
    public void setLogLevel(String logLevel, String logFile) {
        setLogLevel(logLevel);
    }

    @Override
    public void severe(String message) {
        if (logger.isErrorEnabled()) {
            logger.error(buildMessage(message));
        }

    }

    @Override
    public void severe(String message, Throwable e) {
        if (logger.isErrorEnabled()) {
            logger.error(buildMessage(message), e);
        }
    }

    @Override
    public void warning(String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(buildMessage(message));
        }
    }

    @Override
    public void warning(String message, Throwable e) {
        if (logger.isWarnEnabled()) {
            logger.warn(buildMessage(message), e);
        }
    }

    @Override
    public void info(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(buildMessage(message));
        }
    }

    @Override
    public void info(String message, Throwable e) {
        if (logger.isInfoEnabled()) {
            logger.info(buildMessage(message), e);
        }
    }

    @Override
    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(buildMessage(message));
        }
    }

    @Override
    public void debug(String message, Throwable e) {
        if (logger.isDebugEnabled()) {
            logger.debug(buildMessage(message), e);
        }
    }

    @Override
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
        changeLogName = (databaseChangeLog == null) ? null : databaseChangeLog.getFilePath();
    }

    @Override
    public void setChangeSet(ChangeSet changeSet) {
        changeSetName = (changeSet == null) ? null : changeSet.toString(false);
    }

    @Override
    protected String buildMessage(String message) {
        StringBuilder msg = new StringBuilder();
        if (changeLogName != null) {
            msg.append(changeLogName).append(": ");
        }
        if (changeSetName != null) {
            msg.append(changeSetName.replace(changeLogName + "::", "")).append(": ");
        }
        msg.append(message);
        return msg.toString();
    }
}
