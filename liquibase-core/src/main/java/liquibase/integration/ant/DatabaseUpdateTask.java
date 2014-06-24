package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.util.ui.UIFactory;
import org.apache.tools.ant.BuildException;

import java.io.Writer;

/**
 * Ant task for migrating a database forward.
 */
public class DatabaseUpdateTask extends BaseLiquibaseTask {
    private boolean dropFirst = false;

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        if (!shouldRun()) {
            return;
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

            if (isPromptOnNonLocalDatabase()
                    && !liquibase.isSafeToRunUpdate()
                    && UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
                throw new BuildException("Chose not to run against non-production database");
            }

            Writer writer = createOutputWriter();
            if (writer == null) {
                if (isDropFirst()) {
                    liquibase.dropAll();
                }

                liquibase.update(new Contexts(getContexts()), new LabelExpression(getLabels()));
            } else {
                if (isDropFirst()) {
                    throw new BuildException("Cannot dropFirst when outputting update SQL");
                }
                liquibase.update(new Contexts(getContexts()), new LabelExpression(getLabels()), writer);
                writer.flush();
                writer.close();
            }

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}
