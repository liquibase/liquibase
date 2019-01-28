package liquibase.logging.core;

import liquibase.logging.Logger;

public class JavaLogService extends AbstractLogService {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public Logger getLog(Class clazz) {
        return new JavaLogger(java.util.logging.Logger.getLogger(clazz.getName()));
    }

}
