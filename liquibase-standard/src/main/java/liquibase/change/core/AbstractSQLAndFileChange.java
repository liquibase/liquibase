package liquibase.change.core;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.PropertyExpandingStream;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.util.StreamUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static liquibase.GlobalConfiguration.FILE_ENCODING;
import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Abstract base class for SQL file-based changes that supports both inline SQL and external file references.
 * This class provides common functionality for changes like CreateViewChange and CreateProcedureChange.
 */
public abstract class AbstractSQLAndFileChange extends AbstractChange
      implements ReplaceIfExists, HasFileProperty  {
    
    @Setter
    protected String catalogName;
    @Setter
    protected String schemaName;
    @Setter
    private Boolean relativeToChangelogFile;
    @Setter
    protected Boolean replaceIfExists;

    @Getter @Setter @Accessors(fluent = true, chain = false)
    protected String file;

    @Getter @Setter @Accessors(fluent = true, chain = false)
    protected String sql;

	 @DatabaseChangeProperty()
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty()
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(description = "Specifies whether the file path is relative to the changelog file " +
        "rather than looked up in the search path. Default: false.")
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    @Setter
    protected String encoding;// = GlobalConfiguration.FILE_ENCODING.getCurrentValue().toString();

    @DatabaseChangeProperty(exampleValue = "utf8", description = "Encoding used in the file you specify in 'path'")
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the file path and automatically sets relativeToChangelogFile if the path starts with "."
     */
    public void setPath(String file) {
        setFile(file);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    /**
     * Generates checksum for the change. This method handles both inline SQL and file-based SQL.
     */
    @Override
    public CheckSum generateCheckSum() {
        ChecksumVersion version = Scope.getCurrentScope().getChecksumVersion();
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return generateCheckSumV8();
        }
        return generateCheckSumLatest(sql());
    }

    /**
     * Generates checksum for version V8 and earlier.
     */
    @Deprecated
    protected CheckSum generateCheckSumV8() {
        if (file == null) {
            return super.generateCheckSum();
        }
        
        InputStream stream = null;
        try {
            stream = getResource().openInputStream();
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        
        try {
            String sqlText = sql();
            if ((stream == null) && (sqlText == null)) {
                sqlText = "";
            }
            
            String encoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
            if (sqlText != null) {
                try {
                    stream = new ByteArrayInputStream(sqlText.getBytes(encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(encoding + " is not supported by the JVM, " +
                        "this should not happen according to the JavaDoc of the Charset class");
                }
            }
            
            CheckSum checkSum = CheckSum.compute(new NormalizingStreamV8(";", false, false, stream), false);
            
            return CheckSum.compute(super.generateCheckSum().toString() + ":" + checkSum);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Generates checksum for the latest version using the SQL text.
     */
    protected CheckSum generateCheckSumLatest(String sqlText) {
        InputStream stream = null;
        CheckSum checkSum;
        try {
            if (file() == null) {
                Charset encoding = GlobalConfiguration.FILE_ENCODING.getCurrentValue();
                if (sqlText != null) {
                    stream = new ByteArrayInputStream(sqlText.getBytes(encoding));
                }
            } else {
                stream = getResource().openInputStream();
                stream = new PropertyExpandingStream(this.getChangeSet(), stream, encoding);
            }
            checkSum = CheckSum.compute(new AbstractSQLChange.NormalizingStream(stream), false);
            return CheckSum.compute(super.generateCheckSum().toString() + ":" + checkSum);

        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    abstract protected String sqlFieldName();
    
    /**
     * Common validation logic for path vs inline text exclusivity.
     */

    public ValidationErrors validate(Database database, ValidationErrors validate) {
        String sqlText = trimToNull(sql());
        String file = trimToNull(file());
        if ((sqlText != null) && (file != null)) {
            validate.addError("Cannot specify both 'path' and (nested) " + sqlFieldName() +
                  " for " + getSerializedObjectName());
        }
        if ((sqlText == null) && (file == null)) {
            validate.addError("Must specify either 'path' or (nested) " + sqlFieldName() +
                  " for " + getSerializedObjectName());
        }

        return validate;
    }

    public InputStream openSqlStream() throws IOException {
        if (file() == null) {
            return null;
        }
        return getResource().openInputStream();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    /** Get the actual SQL from either the sql property or the file */
    protected String getSqlText() {
        String sqlText;
        if (file() == null) {
            sqlText = trimToNull(sql());
        } else {
            if (getChangeSet() == null) {
                //only try to read a file when inside a changest. Not when analyzing supported
                sqlText = "NO CHANGESET";
            } else {
                try {
                    sqlText = StreamUtil.readStreamAsString(openSqlStream(),
                          getIfNull(encoding, FILE_ENCODING.getCurrentValue().name()));
                    ChangeLogParameters parameters = getChangeSet().getChangeLogParameters();
                    if (parameters != null) {
                        sqlText = parameters.expandExpressions(sqlText, getChangeSet().getChangeLog());
                    }
                } catch (IOException e) {
                    throw new UnexpectedLiquibaseException(e);
                }
            }
        }
        return sqlText;
    }

}