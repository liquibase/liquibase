package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.FileUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;

/**
 * Ant task for rolling back a database.
 */
public class DatabaseRollbackTask extends AbstractChangeLogBasedTask {
    private Date rollbackDate;
    @Getter
    private String rollbackTag;
    @Getter
    private Integer rollbackCount;
    @Setter
    @Getter
    private String rollbackScript;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Writer writer = null;
        Liquibase liquibase = getLiquibase();
        try {
            FileResource outputFile = getOutputFile();
            if(rollbackCount != null) {
                if(outputFile != null) {
                    writer = getOutputFileWriter();
                    liquibase.rollback(rollbackCount, rollbackScript, new Contexts(getContexts()), getLabelFilter(), writer);
                } else {
                    liquibase.rollback(rollbackCount, rollbackScript, new Contexts(getContexts()), getLabelFilter());
                }
            } else if(rollbackTag != null) {
                if(outputFile != null) {
                    writer = getOutputFileWriter();
                    liquibase.rollback(rollbackTag, rollbackScript, new Contexts(getContexts()), getLabelFilter(), writer);
                } else {
                    liquibase.rollback(rollbackTag, rollbackScript, new Contexts(getContexts()), getLabelFilter());
                }
            } else if(rollbackDate != null) {
                if(outputFile != null) {
                    writer = getOutputFileWriter();
                    liquibase.rollback(rollbackDate, rollbackScript, new Contexts(getContexts()), getLabelFilter(), writer);
                } else {
                    liquibase.rollback(rollbackDate, rollbackScript, new Contexts(getContexts()), getLabelFilter());
                }
            } else {
                throw new BuildException("Unable to rollback database. No count, tag, or date set.");
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to rollback database: " + e.getMessage(), e);
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

    public void setRollbackDate(String rollbackDateStr) {
        if((rollbackTag != null) || (rollbackCount != null)) {
            throw new BuildException("Unable to rollback database. A tag or count has already been set.");
        }
        try {
             this.rollbackDate = DateUtils.parseIso8601DateTimeOrDate(rollbackDateStr);
        } catch (ParseException e) {
            throw new BuildException("Unable to parse rollback date/time string into a Date object. Please make sure the date or date/time is in ISO 8601 format (yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss).", e);
        }
    }

    public void setRollbackTag(String rollbackTag) {
        if((rollbackDate != null) || (rollbackCount != null)) {
            throw new BuildException("Unable to rollback database. A date or count has already been set.");
        }
        this.rollbackTag = rollbackTag;
    }

    public void setRollbackCount(Integer rollbackCount) {
        if((rollbackDate != null) || (rollbackTag != null)) {
            throw new BuildException("Unable to rollback database. A date or tag has already been set.");
        }
        this.rollbackCount = rollbackCount;
    }

}
