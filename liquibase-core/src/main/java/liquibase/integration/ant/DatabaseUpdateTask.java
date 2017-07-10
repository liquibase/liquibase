package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Ant task for migrating a database forward.
 */
public class DatabaseUpdateTask extends AbstractChangeLogBasedTask {
    private boolean dropFirst;
    private String toTag;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Writer writer = null;
        Liquibase liquibase = getLiquibase();
        try {
            FileResource outputFile = getOutputFile();
            if(outputFile != null) {
                writer = getOutputFileWriter();
                liquibase.update(toTag, new Contexts(getContexts()), getLabels(), writer);
            } else {
                if(dropFirst) {
                    liquibase.dropAll();
                }
                liquibase.update(toTag, new Contexts(getContexts()), getLabels());
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to update database. " + e.toString(), e);
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Unable to generate update SQL. Encoding [" + getOutputEncoding() + "] is not supported.", e);
        } catch (IOException e) {
            throw new BuildException("Unable to generate update SQL. Error creating output writer.", e);
        } finally {
            FileUtils.close(writer);
        }
    }

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    public String getToTag() {
        return toTag;
    }

    public void setToTag(String toTag) {
        this.toTag = toTag;
    }
}
