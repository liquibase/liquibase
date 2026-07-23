package liquibase.logging.core;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;

public class JavaLogService extends AbstractLogService {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    private final Map<Class, JavaLogger> loggers = new HashMap<>();

    private java.util.logging.Logger parent;

    @Override
    public Logger getLog(Class clazz) {
        JavaLogger logger = loggers.get(clazz);
        if (logger == null) {
            java.util.logging.Logger utilLogger = java.util.logging.Logger.getLogger(getLogName(clazz));
            utilLogger.setUseParentHandlers(true);
            if (parent != null && !parent.getName().equals(utilLogger.getName())) {
                utilLogger.setParent(parent);
            }
            logger = new JavaLogger(utilLogger);
            setChannelLogLevel(getLogName(clazz));

            this.loggers.put(clazz, logger);
        }

        return logger;
    }

    /**
     *
     * Use the LIQUIBASE_LOG_CHANNELS property value to set the log levels.  Because of this code
     * is called while the configuration classes are being initialized, the logChannels and logLevel
     * values are passed in via the Scope, from LiquibaseCommandLine.execute()
     *
     * @param channelName    The name of the channel to set logging for
     *
     */
    private void setChannelLogLevel(String channelName) {
        final String configuredChannels = Scope.getCurrentScope().get("logChannels", String.class);
        if (configuredChannels == null) {
            return;
        }
        Level logLevel = Scope.getCurrentScope().get("logLevel", Level.class);
        if (logLevel == Level.OFF) {
            return;
        }
        List<String> channels;
        if (! configuredChannels.equalsIgnoreCase("all")) {
            channels = StringUtil.splitAndTrim(configuredChannels.toLowerCase(), ",");
            if (channels.contains(channelName.toLowerCase())) {
                java.util.logging.Logger.getLogger(channelName).setLevel(logLevel);
            }
        }
    }

    /**
     * Because java.util.logging differentiates between the log name and the class/method logging,
     * we can collapses the log names to a simpler/smaller set to allow configuration to rely on the class name less.
     * <br><br>
     * This implementation always returns the 2nd level liquibase package name for the class passed in OR from any liquibase interfaces/classes it implements.
     * For example, all {@link liquibase.change.Change} classes will return a log name of "liquibase.change" no matter what class name or package name they have.
     */
    protected String getLogName(Class clazz) {
        if (clazz == null) {
            return "unknown";
        }

        if (clazz.getPackage() == null) {
            return clazz.getName();
        }

        final String classPackageName = clazz.getPackage().getName();
        if (classPackageName.equals("liquibase")) {
            return "liquibase";
        }

        if (classPackageName.startsWith("liquibase.")) {
            return classPackageName.replaceFirst("(liquibase.\\w+)\\.?.*", "$1");
        }

        for (Class iface : clazz.getInterfaces()) {
            if (iface.equals(LiquibaseSerializable.class)) { //don't use liquibase.serializable just because it implements LiquibaseSerializable
                continue;
            }
            final String interfaceLog = getLogName(iface);
            if (!interfaceLog.equals("liquibase")) {
                return interfaceLog;
            }
        }

        final Class superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            final String superclassLogName = getLogName(superclass);
            if (!superclassLogName.equals("liquibase")) {
                return superclassLogName;
            }
        }

        return "liquibase";
    }

    public java.util.logging.Logger getParent() {
        return parent;
    }

    /**
     * Explicitly control the parent logger for all {@link java.util.logging.Logger} instances created.
     */
    public void setParent(java.util.logging.Logger parent) {
        this.parent = parent;
    }

    public Formatter getCustomFormatter() {
        return null;
    }

    /**
     * Returns the {@link Formatter} to be applied to the console (stdout/stderr) handler only.
     * <p>
     * Overrides the {@link liquibase.logging.LogService} interface default to return the value of
     * {@link #getCustomFormatter()}, preserving current OSS behaviour: when no custom formatter is set
     * both the console handler and any file handler continue to use the JUL default ({@link java.util.logging.SimpleFormatter}).
     * <p>
     * Pro subclasses may override this method to return a colour-capable formatter for the console
     * while leaving {@link #getCustomFormatter()} returning {@code null} (so file handlers stay plain).
     *
     * @return the console-specific {@link Formatter}, or {@code null}
     * @since 5.2
     */
    @Override
    public Formatter getConsoleFormatter() {
        return getCustomFormatter();
    }

    /**
     * Set the formatter for the supplied handler if the supplied log service
     * is a JavaLogService and that service specifies a custom formatter.
     */
    public static void setFormatterOnHandler(LogService logService, Handler handler) {
        if (logService instanceof JavaLogService && handler != null) {
            Formatter customFormatter = ((JavaLogService) logService).getCustomFormatter();
            if (customFormatter != null) {
                handler.setFormatter(customFormatter);
            }
        }
    }

    /**
     * Set the <em>console-specific</em> formatter on the supplied handler.
     * <p>
     * Unlike {@link #setFormatterOnHandler(LogService, Handler)} (which uses {@link #getCustomFormatter()}),
     * this method reads {@link LogService#getConsoleFormatter()}.  When the console formatter is {@code null}
     * it falls back to {@link #getCustomFormatter()} so that existing behaviour is preserved for any
     * {@link LogService} implementation that does not override {@link LogService#getConsoleFormatter()}.
     * <p>
     * Intended to be called in {@code configureLogging()} exclusively for {@link java.util.logging.ConsoleHandler}
     * instances, keeping file handlers on the plain formatter path.
     *
     * @param logService the active log service (may be any implementation)
     * @param handler    the handler to configure (must be non-{@code null})
     * @since 5.2
     */
    public static void setConsoleFormatterOnHandler(LogService logService, Handler handler) {
        if (logService == null || handler == null) {
            return;
        }
        Formatter consoleFormatter = logService.getConsoleFormatter();
        if (consoleFormatter != null) {
            handler.setFormatter(consoleFormatter);
            return;
        }
        // Fall back to the existing custom-formatter path so existing impls are unchanged.
        if (logService instanceof JavaLogService) {
            Formatter customFormatter = ((JavaLogService) logService).getCustomFormatter();
            if (customFormatter != null) {
                handler.setFormatter(customFormatter);
            }
        }
    }
}
