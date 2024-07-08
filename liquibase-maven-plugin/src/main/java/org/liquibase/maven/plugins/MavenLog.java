package org.liquibase.maven.plugins;

import org.apache.maven.plugin.logging.Log;

public class MavenLog implements Log {

    public enum Level {
        DEBUG(0), debug(0), FINE(0),  fine(0),
        INFO(1), info(1),
        WARNING(2), warning(2),
        ERROR(3), error(3)
        ;

        private final int level;

        Level(int level) {
            this.level = level;
        }

        public boolean isAtLeast(Level other) {
            return this.level <= other.level;
        }
    }

    private final Log actualLog;
    private final Level level;

    public MavenLog(Log actualLog, Level level) {
        this.actualLog = actualLog;
        this.level = level;
    }

    public MavenLog(Log actualLog, String level) {
        this(actualLog, Level.valueOf(level));
    }

    // Debug
    // =====

    @Override
    public boolean isDebugEnabled() {
        return level.isAtLeast(Level.DEBUG) && actualLog.isDebugEnabled();
    }

    @Override
    public void debug(CharSequence charSequence) {
        if (isDebugEnabled()) {
            actualLog.debug(charSequence);
        }
    }

    @Override
    public void debug(CharSequence charSequence, Throwable throwable) {
        if (isDebugEnabled()) {
            actualLog.debug(charSequence, throwable);
        }
    }

    @Override
    public void debug(Throwable throwable) {
        if (isDebugEnabled()) {
            actualLog.debug(throwable);
        }
    }

    // Info
    // ====

    @Override
    public boolean isInfoEnabled() {
        return level.isAtLeast(Level.INFO) && actualLog.isInfoEnabled();
    }

    @Override
    public void info(CharSequence charSequence) {
        if (isInfoEnabled()) {
            actualLog.info(charSequence);
        }
    }

    @Override
    public void info(CharSequence charSequence, Throwable throwable) {
        if (isInfoEnabled()) {
            actualLog.info(charSequence, throwable);
        }
    }

    @Override
    public void info(Throwable throwable) {
        if (isInfoEnabled()) {
            actualLog.info(throwable);
        }
    }

    // Warn
    // ====

    @Override
    public boolean isWarnEnabled() {
        return level.isAtLeast(Level.WARNING) && actualLog.isWarnEnabled();
    }

    @Override
    public void warn(CharSequence charSequence) {
        if (isWarnEnabled()) {
            actualLog.warn(charSequence);
        }
    }

    @Override
    public void warn(CharSequence charSequence, Throwable throwable) {
        if (isWarnEnabled()) {
            actualLog.warn(charSequence, throwable);
        }
    }

    @Override
    public void warn(Throwable throwable) {
        if (isWarnEnabled()) {
            actualLog.warn(throwable);
        }
    }

    // Error
    // =====

    @Override
    public boolean isErrorEnabled() {
        return level.isAtLeast(Level.ERROR) && actualLog.isErrorEnabled();
    }

    @Override
    public void error(CharSequence charSequence) {
        if (isErrorEnabled()) {
            actualLog.error(charSequence);
        }
    }

    @Override
    public void error(CharSequence charSequence, Throwable throwable) {
        if (isErrorEnabled()) {
            actualLog.error(charSequence, throwable);
        }
    }

    @Override
    public void error(Throwable throwable) {
        if (isErrorEnabled()) {
            actualLog.error(throwable);
        }
    }
}
