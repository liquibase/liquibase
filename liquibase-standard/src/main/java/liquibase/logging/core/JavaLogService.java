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
}
