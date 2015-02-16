package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.AbstractServiceFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.StreamUtil;
import liquibase.util.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Factory/registry for looking up the correct ActionLogic implementation. Should normally be accessed using {@link Scope#getSingleton(Class)}, not constructed directly.
 */
public class ActionLogicFactory  extends AbstractServiceFactory<ActionLogic> {

    /**
     * Constructor is protected because it should be used as a singleton.
     */
    protected ActionLogicFactory(Scope scope) {
        super(scope);
        try {
            for (TemplateActionLogic templateActionLogic : getTemplateActionLogic(scope)) {
                register(templateActionLogic);
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    protected Class<ActionLogic> getServiceClass() {
        return ActionLogic.class;
    }

    protected TemplateActionLogic[] getTemplateActionLogic(Scope scope) {
        ResourceAccessor resourceAccessor = scope.get(Scope.Attr.resourceAccessor, ResourceAccessor.class);
        Validate.notNull(resourceAccessor, "Scope.resourceAccessor not set");

        for (String packagePath : ServiceLocator.getInstance().getPackages()) {
            try {
                Set<String> files = resourceAccessor.list(null, packagePath.replace(".", "/"), true, false, true);
                if (files == null) {
                    continue;
                }
                for (String resource : files) {
                    if (resource.endsWith(".logic")) {
                        Set<InputStream> streams = resourceAccessor.getResourcesAsStream(resource);
                        Validate.notNull(streams, "Cannot read stream(s) for " + resource);

                        for (InputStream stream : streams) {
                            try {
                                register(new TemplateActionLogic(StreamUtil.getReaderContents(new InputStreamReader(stream))));
                            } catch (TemplateActionLogic.ParseException e) {
                                throw new UnexpectedLiquibaseException(e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LogFactory.getInstance().getLog().warning("Error reading "+packagePath, e);
            }
        }

        return new TemplateActionLogic[0];
    }

    /**
     * Returns the highest priority {@link liquibase.actionlogic.ActionLogic} implementation that supports the given action/scope pair.
     */
    public ActionLogic getActionLogic(final Action action, final Scope scope) {
        return getService(scope, action);
    }

    @Override
    protected int getPriority(ActionLogic obj, Scope scope, Object... args) {
        return obj.getPriority((Action) args[0], scope);
    }
}
