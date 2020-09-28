package liquibase.logging.core;

import liquibase.Scope;
import liquibase.logging.LogService;
import liquibase.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeLogService extends AbstractLogService {

    private List<LogService> services = new ArrayList<>();

    public CompositeLogService() {
    }

    public CompositeLogService(boolean includeCurrentScopeLogService, LogService... logService) {
        this.services = new ArrayList<>(Arrays.asList(logService));
        if (includeCurrentScopeLogService) {
            services.add(Scope.getCurrentScope().get(Scope.Attr.logService, LogService.class));
        }
    }

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Logger getLog(Class clazz) {
        List<Logger> loggers = new ArrayList<>();
        for (LogService service : services) {
            loggers.add(service.getLog(clazz));
        }
        return new CompositeLogger(loggers, this.filter);
    }
}
