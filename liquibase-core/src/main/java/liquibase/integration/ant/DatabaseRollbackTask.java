package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;

/**
 * Ant task for rolling back a database.
 */
public class DatabaseRollbackTask extends AbstractChangeLogBasedTask {
    private Date rollbackDate;
    private String rollbackTag;
    private Integer rollbackCount;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Writer writer = null;
        Liquibase liquibase = getLiquibase();
        try {
            FileResource outputFile = getOutputFile();
            if(rollbackCount != null) {
                if(outputFile != null) {
                    writer = getOutputFileWriter();
                    liquibase.rollback(rollbackCount, getContexts(), writer);
                } else {
                    liquibase.rollback(rollbackCount, getContexts());
                }
            } else if(rollbackTag != null) {
                if(outputFile != null) {
                    writer = getOutputFileWriter();
                    liquibase.rollback(rollbackTag, getContexts(), writer);
                } else {
                    liquibase.rollback(rollbackTag, getContexts());
                }
            } else if(rollbackDate != null) {
                if(outputFile != null) {
                    writer = getOutputFileWriter();
                    liquibase.rollback(rollbackDate, getContexts(), writer);
                } else {
                    liquibase.rollback(rollbackDate, getContexts());
                }
            } else {
                throw new BuildException("Unable to rollback database. No count, tag, or date set.");
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to rollback database.", e);
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Unable to generate rollback SQL. Encoding [" + getOutputEncoding() + "] is not supported.", e);
        } catch (IOException e) {
            throw new BuildException("Unable to generate rollback SQL. Error creating output writer.", e);
        } finally {
            FileUtils.close(writer);
        }
    }

    public Date getRollbackDate() {
        if (rollbackDate == null) {
            return null;
        }
        return new Date(rollbackDate.getTime());
    }

    public void setRollbackDate(Date rollbackDate) {
        if(rollbackTag != null || rollbackCount != null) {
            throw new BuildException("Unable to rollback database. A tag or count has already been set.");
        }
        this.rollbackDate = new Date(rollbackDate.getTime());
    }

    public String getRollbackTag() {
        return rollbackTag;
    }

    public void setRollbackTag(String rollbackTag) {
        if(rollbackDate != null || rollbackCount != null) {
            throw new BuildException("Unable to rollback database. A date or count has already been set.");
        }
        this.rollbackTag = rollbackTag;
    }

    public Integer getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Integer rollbackCount) {
        if(rollbackDate != null || rollbackTag != null) {
            throw new BuildException("Unable to rollback database. A date or tag has already been set.");
        }
        this.rollbackCount = rollbackCount;
    }
}
