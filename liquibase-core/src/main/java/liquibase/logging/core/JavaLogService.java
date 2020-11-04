package liquibase.logging.core;

import liquibase.logging.Logger;
import liquibase.serializer.LiquibaseSerializable;

import java.util.HashMap;
import java.util.Map;

public class JavaLogService extends AbstractLogService {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    private Map<Class, JavaLogger> loggers = new HashMap<>();

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
            logger = new JavaLogger(utilLogger, this.filter);

            this.loggers.put(clazz, logger);
        }

        return logger;
    }

    /**
     * Because java.util.logging differentiates between the log name and the class/method logging,
     * we can collapses the log names to a simpler/smaller set to allow configuration to rely on the class name less.
     * <br><br>
     * This implementation always returns the 2nd level liquibase package name for the class passed in OR from any liquibase interfaces/classes it implements.
     * For example, all {@link liquibase.change.Change} classes will return a log name of "liquibase.change" no matter what class name or package name they have.
     */
    protected String getLogName(Class clazz) {
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
}
