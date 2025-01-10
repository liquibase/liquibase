package liquibase.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import liquibase.change.custom.CustomChange;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.taskdefs.Delete;
import org.springframework.test.annotation.Rollback;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "databaseChangeLog")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonRootName(value = "databaseChangeLog")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class DatabaseChangeLog {

    @XmlElement(name = "include")
    @JsonProperty("include")
    private List<Include> includes;

    @XmlElement(name = "includeAll")
    @JsonProperty("includeAll")
    private List<IncludeAll> includeAlls;

    @XmlElement(name = "property")
    @JsonProperty("property")
    private List<Property> properties;

    @XmlElement(name = "changeSet")
    @JsonProperty("changeSet")
    private List<ChangeSet> changeSets;

    @XmlElement(name = "preConditions")
    @JsonProperty("preConditions")
    private PreConditions preConditions;

    @XmlElement(name = "removeChangeSetProperty")
    @JsonProperty("removeChangeSetProperty")
    private List<RemoveChangeSetProperty> removeChangeSetProperties;

    @XmlElement(name = "modifyChangeSets")
    @JsonProperty("modifyChangeSets")
    private List<ModifyChangeSets> modifyChangeSets;

    @JsonProperty("logicalFilePath")
    private String logicalFilePath;

    @JsonProperty("context")
    private String context;

    @JsonProperty("contextFilter")
    private String contextFilter;

    @JsonProperty("changeLogId")
    private String changeLogId;

    @JsonProperty("objectQuotingStrategy")
    private ObjectQuotingStrategy objectQuotingStrategy;

    @JsonProperty("otherAttributes")
    private Map<String, String> otherAttributes;

    // Nested classes representing complex fields
    @Getter
    @Setter
    public static class Include {
        private String id;
        private String author;
        private String file;
        private BooleanExp relativeToChangelogFile;
        private BooleanExp errorIfMissing;
        private String context;
        private String contextFilter;
        private String labels;
        private String ignore;
        private String created;
        private String logicalFilePath;
        private Map<String, String> otherAttributes;
    }

    @Getter
    @Setter
    public static class IncludeAll {
        private String path;
        private BooleanExp errorIfMissingOrEmpty;
        private BooleanExp relativeToChangelogFile;
        private String resourceComparator;
        private String filter;
        private String context;
        private String labels;
        private String contextFilter;
        private IntegerExp minDepth;
        private IntegerExp maxDepth;
        private String endsWithFilter;
        private String ignore;
        private String logicalFilePath;
        private Map<String, String> otherAttributes;
    }

    @Getter
    @Setter
    public static class Property {
        private String file;
        private BooleanExp relativeToChangelogFile;
        private BooleanExp errorIfMissing;
        private String name;
        private String value;
        private String dbms;
        private String context;
        private String contextFilter;
        private String labels;
        private Boolean global;
        private String target;
        private Map<String, String> otherAttributes;
    }

    @Getter
    @Setter
    public static class PreConditions {
        private List<PreConditionChildren> preConditionChildren;
        private String onFailMessage;
        private String onErrorMessage;
        private OnChangeLogPreconditionErrorOrFail onFail;
        private OnChangeLogPreconditionErrorOrFail onError;
        private OnChangeLogPreconditionOnSqlOutput onSqlOutput;
    }

    @Getter
    @Setter
    public static class ChangeSet {
        private List<ValidCheckSum> validCheckSum;
        private PreConditions preConditions;
        private List<TagDatabase> tagDatabase;
        private List<ChangeSetChildren> changeSetChildren;
        private List<ModifySql> modifySql;
        private ChangeSetAttributes changeSetAttributes;
        private Map<String, String> otherAttributes;
    }

    @Getter
    @Setter
    public static class RemoveChangeSetProperty {
        private String change;
        private String dbms;
        private String remove;
    }

    @Getter
    @Setter
    public static class ModifyChangeSets {
        private List<Include> include;
        private List<IncludeAll> includeAll;
        private String runWith;
        private String runWithSpoolFile;
        private String endDelimiter;
        private Boolean stripComments;
        private Map<String, String> otherAttributes;
    }

    @Getter
    @Setter
    public static class BooleanExp {
        private Boolean value;
        private String expression;
    }

    @Getter
    @Setter
    public static class IntegerExp {
        private Integer value;
        private String expression;
    }

    @Getter
    @Setter
    public static class OnChangeLogPreconditionErrorOrFail {
        private String value;
    }

    @Getter
    @Setter
    public static class OnChangeLogPreconditionOnSqlOutput {
        private String value;
    }

    @Getter
    @Setter
    public static class ValidCheckSum {
        private String value;
    }

    @Getter
    @Setter
    public static class TagDatabase {
        private String tag;
    }

    @Getter
    @Setter
    public static class ChangeSetChildren {
        private Comment comment;
        private List<CreateTable> createTable;
        private List<DropTable> dropTable;
        private List<CreateView> createView;
        private List<RenameView> renameView;
        private List<DropView> dropView;
        private List<Insert> insert;
        private List<AddColumn> addColumn;
        private List<Sql> sql;
        private List<CreateProcedure> createProcedure;
        private List<DropProcedure> dropProcedure;
        private List<SqlFile> sqlFile;
        private List<RenameTable> renameTable;
        private List<RenameColumn> renameColumn;
        private List<DropColumn> dropColumn;
        private List<MergeColumns> mergeColumns;
        private List<ModifyDataType> modifyDataType;
        private List<CreateSequence> createSequence;
        private List<AlterSequence> alterSequence;
        private List<DropSequence> dropSequence;
        private List<RenameSequence> renameSequence;
        private List<CreateIndex> createIndex;
        private List<DropIndex> dropIndex;
        private List<AddNotNullConstraint> addNotNullConstraint;
        private List<DropNotNullConstraint> dropNotNullConstraint;
        private List<AddForeignKeyConstraint> addForeignKeyConstraint;
        private List<DropForeignKeyConstraint> dropForeignKeyConstraint;
        private List<DropAllForeignKeyConstraints> dropAllForeignKeyConstraints;
        private List<AddPrimaryKey> addPrimaryKey;
        private List<DropPrimaryKey> dropPrimaryKey;
        private List<AddLookupTable> addLookupTable;
        private List<AddAutoIncrement> addAutoIncrement;
        private List<AddDefaultValue> addDefaultValue;
        private List<DropDefaultValue> dropDefaultValue;
        private List<AddUniqueConstraint> addUniqueConstraint;
        private List<DropUniqueConstraint> dropUniqueConstraint;
        private List<SetTableRemarks> setTableRemarks;
        private List<SetColumnRemarks> setColumnRemarks;
        private List<CustomChange> customChange;
        private List<Update> update;
        private List<Delete> delete;
        private List<LoadData> loadData;
        private List<LoadUpdateData> loadUpdateData;
        private List<ExecuteCommand> executeCommand;
        private List<Stop> stop;
        private List<Output> output;
        private List<Empty> empty;
        private Rollback rollback;
        private List<Object> any;
    }

    @Getter
    @Setter
    public static class ModifySql {
        private String labels;
        private BooleanExp applyToRollback;
    }

    @Getter
    @Setter
    public static class ChangeSetAttributes {
        private String runWith;
        private String runWithSpoolFile;
    }

    @Getter
    @Setter
    public static class PreConditionChildren {
        // Define the fields and methods
    }

    @Getter
    @Setter
    public static class ObjectQuotingStrategy {
        private String strategy;
    }

    public static class Change {

        @Getter
        @Setter
        public static class ValidCheckSum {
            private String value;
        }

        @Getter
        @Setter
        public static class TagDatabase {
            private String tag;
        }

        @Getter
        @Setter
        public static class ModifySql {
            private String dbms;
            private String context;
            private String contextFilter;
            private String labels;
            private Boolean applyToRollback;
        }

        @Getter
        @Setter
        public static class ChangeSetAttributes {
            private String id;
            private String author;
            private String context;
            private String contextFilter;
            private String labels;
            private String dbms;
            private Boolean runOnChange;
            private Boolean runAlways;
            private Boolean failOnError;
            private String onValidationFail;
            private Boolean runInTransaction;
            private String logicalFilePath;
            private String objectQuotingStrategy;
            private String created;
            private String runOrder;
            private Boolean ignore;
            private String runWith;
            private String runWithSpoolFile;
        }

        @Getter
        @Setter
        public static class PreConditionChildren {
            // Define the fields and methods
        }

        @Getter
        @Setter
        public static class ObjectQuotingStrategy {
            private String strategy;
        }
    }
}
