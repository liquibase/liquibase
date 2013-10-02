package liquibase.precondition.core;

import liquibase.util.StringUtils;
import liquibase.util.StreamUtil;
import liquibase.database.Database;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class PreconditionContainer extends AndPrecondition {

    public enum FailOption {
        HALT("HALT"),
        CONTINUE("CONTINUE"),
        MARK_RAN("MARK_RAN"),
        WARN("WARN");

        String key;

        FailOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum ErrorOption {
        HALT("HALT"),
        CONTINUE("CONTINUE"),
        MARK_RAN("MARK_RAN"),
        WARN("WARN");

        String key;

        ErrorOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }


    public enum OnSqlOutputOption {
        IGNORE("IGNORE"),
        TEST("TEST"),
        FAIL("FAIL");

        String key;

        OnSqlOutputOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    private FailOption onFail = FailOption.HALT;
    private ErrorOption onError = ErrorOption.HALT;
    private OnSqlOutputOption onSqlOutput = OnSqlOutputOption.IGNORE;
    private String onFailMessage;
    private String onErrorMessage;

    public FailOption getOnFail() {
        return onFail;
    }

    public void setOnFail(String onFail) {
        if (onFail == null) {
            this.onFail = FailOption.HALT;
        } else {
            for (FailOption option : FailOption.values()) {
                if (option.key.equalsIgnoreCase(onFail)) {
                    this.onFail = option;
                    return;
                }
            }
            List<String> possibleOptions = new ArrayList<String>();
            for (FailOption option : FailOption.values()) {
                possibleOptions.add(option.key);
            }
            throw new RuntimeException("Unknown onFail attribute value '"+onFail+"'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
        }
    }

    public ErrorOption getOnError() {
        return onError;
    }

    public void setOnError(String onError) {
        if (onError == null) {
            this.onError = ErrorOption.HALT;
        } else {
            for (ErrorOption option : ErrorOption.values()) {
                if (option.key.equalsIgnoreCase(onError)) {
                    this.onError = option;
                    return;
                }
            }
            List<String> possibleOptions = new ArrayList<String>();
            for (ErrorOption option : ErrorOption.values()) {
                possibleOptions.add(option.key);
            }
            throw new RuntimeException("Unknown onError attribute value '"+onError+"'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
        }
    }

    public OnSqlOutputOption getOnSqlOutput() {
        return onSqlOutput;
    }

    public void setOnSqlOutput(String onSqlOutput) {
        if (onSqlOutput == null) {
            setOnSqlOutput((OnSqlOutputOption)null);
            return;
        }
        
        for (OnSqlOutputOption option : OnSqlOutputOption.values()) {
            if (option.key.equalsIgnoreCase(onSqlOutput)) {
                setOnSqlOutput(option);
                return;
            }
        }
        List<String> possibleOptions = new ArrayList<String>();
        for (OnSqlOutputOption option : OnSqlOutputOption.values()) {
            possibleOptions.add(option.key);
        }
        throw new RuntimeException("Unknown onSqlOutput attribute value '" + onSqlOutput + "'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
    }

    public void setOnSqlOutput(OnSqlOutputOption onSqlOutput) {
        if (onSqlOutput == null) {
            this.onSqlOutput = OnSqlOutputOption.IGNORE;
        } else {
            this.onSqlOutput = onSqlOutput;
        }
    }

    public String getOnFailMessage() {
        return onFailMessage;
    }

    public void setOnFailMessage(String onFailMessage) {
        this.onFailMessage = onFailMessage;
    }

    public String getOnErrorMessage() {
        return onErrorMessage;
    }

    public void setOnErrorMessage(String onErrorMessage) {
        this.onErrorMessage = onErrorMessage;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        String ranOn = String.valueOf(changeLog);
        if (changeSet != null) {
            ranOn = String.valueOf(changeSet);
        }

        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            // Three cases for preConditions onUpdateSQL:
            // 1. TEST: preConditions should be run, as in regular update mode
            // 2. FAIL: the preConditions should fail if there are any
            // 3. IGNORE: act as if preConditions don't exist
            boolean testPrecondition = false;
                if (executor.updatesDatabase()) {
                    testPrecondition = true;
                } else {
                    if (this.getOnSqlOutput().equals(PreconditionContainer.OnSqlOutputOption.TEST)) {
                        testPrecondition = true;
                    } else if (this.getOnSqlOutput().equals(PreconditionContainer.OnSqlOutputOption.FAIL)) {
                        throw new PreconditionFailedException("Unexpected precondition in updateSQL mode with onUpdateSQL value: "+this.getOnSqlOutput(), changeLog, this);
                    } else if (this.getOnSqlOutput().equals(PreconditionContainer.OnSqlOutputOption.IGNORE)) {
                        testPrecondition = false;
                    }
                }

            if (testPrecondition) {
                super.check(database, changeLog, changeSet);
            }
        } catch (PreconditionFailedException e) {
            StringBuffer message = new StringBuffer();
            message.append("     ").append(e.getFailedPreconditions().size()).append(" preconditions failed").append(StreamUtil.getLineSeparator());
            for (FailedPrecondition invalid : e.getFailedPreconditions()) {
                message.append("     ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());
            }

            if (getOnFailMessage() != null) {
                message = new StringBuffer(getOnFailMessage());
            }
            if (this.getOnFail().equals(PreconditionContainer.FailOption.WARN)) {
                LogFactory.getLogger().info("Executing: " + ranOn + " despite precondition failure due to onFail='WARN':\n " + message);
            } else {
                if (getOnFailMessage() == null) {
                    throw e;
                } else {
                    throw new PreconditionFailedException(getOnFailMessage(), changeLog, this);
                }
            }
        } catch (PreconditionErrorException e) {
            StringBuffer message = new StringBuffer();
            message.append("     ").append(e.getErrorPreconditions().size()).append(" preconditions failed").append(StreamUtil.getLineSeparator());
            for (ErrorPrecondition invalid : e.getErrorPreconditions()) {
                message.append("     ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());
            }

            if (this.getOnError().equals(PreconditionContainer.ErrorOption.CONTINUE)) {
                LogFactory.getLogger().info("Continuing past: " + toString() + " despite precondition error:\n " + message);
                throw e;
            } else if (this.getOnError().equals(PreconditionContainer.ErrorOption.WARN)) {
                LogFactory.getLogger().warning("Continuing past: " + toString() + " despite precondition error:\n " + message);
            } else {
                if (getOnErrorMessage() == null) {
                    throw e;
                } else {
                    throw new PreconditionErrorException(getOnErrorMessage(), e.getErrorPreconditions());
                }                
            }
        }
    }
}
