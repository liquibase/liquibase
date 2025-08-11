package liquibase.change;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * The standard configuration used by Change classes to represent a constraints on a column.
 */
public class ConstraintsConfig extends AbstractLiquibaseSerializable {

    private Boolean nullable;
    private String notNullConstraintName;
    private Boolean primaryKey;
    private String primaryKeyName;
    private String primaryKeyTablespace;
    private String references;
    @Setter
    @Getter
    private String referencedTableCatalogName;
    @Getter
    @Setter
    private String referencedTableSchemaName;
    @Setter
    @Getter
    private String referencedTableName;
    @Setter
    @Getter
    private String referencedColumnNames;
    private Boolean unique;
    private String uniqueConstraintName;
    private String checkConstraint;
    private Boolean deleteCascade;
    private String foreignKeyName;
    private Boolean initiallyDeferred;
    private Boolean deferrable;
    private Boolean validateNullable;
    private Boolean validateUnique;
    private Boolean validatePrimaryKey;
    private Boolean validateForeignKey ;

    /**
     * Returns if the column should be nullable. Returns null if unspecified.
     */
    public Boolean isNullable() {
        return nullable;
    }

    public ConstraintsConfig setNullable(Boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    /**
     * Set the nullable parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setNullable(String nullable) {
        this.nullable = parseBoolean(nullable);

        return this;
    }

    /**
     * If {@link #isNullable()} is 'false' and database supports named not null constraints
     * @return not null constraint name
     * @see #getUniqueConstraintName()
     */
    public String getNotNullConstraintName() {
        return notNullConstraintName;
    }

    public ConstraintsConfig setNotNullConstraintName(String notNullConstraintName) {
        this.notNullConstraintName = notNullConstraintName;
        return this;
    }

    /**
     * Returns true if the column should be part of the primary key. Returns null if unspecified
     */
    public Boolean isPrimaryKey() {
        return primaryKey;
    }

    public ConstraintsConfig setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    /**
     * Set the primaryKey parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setPrimaryKey(String primaryKey) {
        this.primaryKey = parseBoolean(primaryKey);

        return this;
    }


    /**
     * Returns the name to use for the primary key constraint. Returns null if not specified
     */
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public ConstraintsConfig setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
        return this;
    }

    /**
     * Returns the "references" clause to use for the foreign key. Normally a string of the format TABLE(COLUMN_NAME).
     * Returns null if not specified
     */
    public String getReferences() {
        return references;
    }

    public ConstraintsConfig setReferences(String references) {
        this.references = references;
        return this;
    }

    /**
     * Returns if the column is part of a unique constraint. Returns null if not specified
     */
    public Boolean isUnique() {
        return unique;
    }

    public ConstraintsConfig setUnique(Boolean unique) {
        this.unique = unique;
        return this;
    }

    /**
     * Set the unique parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setUnique(String unique) {
        this.unique = parseBoolean(unique);

        return this;
    }


    /**
     * Returns the name to use for the unique constraint. Returns null if not specified
     */
    public String getUniqueConstraintName() {
        return uniqueConstraintName;
    }

    public ConstraintsConfig setUniqueConstraintName(String uniqueConstraintName) {
        this.uniqueConstraintName = uniqueConstraintName;
        return this;
    }

    /**
     * Returns the check constraint to use on this column. Returns null if not specified
     */
    public String getCheckConstraint() {
        return checkConstraint;
    }

    public ConstraintsConfig setCheckConstraint(String checkConstraint) {
        this.checkConstraint = checkConstraint;
        return this;
    }


    /**
     * Returns if a foreign key defined for this column should cascade deletes. Returns null if not specified.
     */
    public Boolean isDeleteCascade() {
        return deleteCascade;
    }

    public ConstraintsConfig setDeleteCascade(Boolean deleteCascade) {
        this.deleteCascade = deleteCascade;
        return this;
    }

    /**
     * Set the deleteCascade parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setDeleteCascade(String deleteCascade) {
        this.deleteCascade = parseBoolean(deleteCascade);

        return this;
    }

    /**
     * Returns the name to use for the columns foreign key constraint. Returns null if not specified.
     */
    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public ConstraintsConfig setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
        return this;
    }

    /**
     * Returns if a foreign key defined for this column should be "initially deferred". Returns null if not specified.
     */
    public Boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public ConstraintsConfig setInitiallyDeferred(Boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    /**
     * Set the initiallyDeferred parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setInitiallyDeferred(String initiallyDeferred) {
        this.initiallyDeferred = parseBoolean(initiallyDeferred);

        return this;
    }

    /**
     * Returns if a foreign key defined for this column should deferrable. Returns null if not specified.
     */
    public Boolean isDeferrable() {
        return deferrable;
    }

    public ConstraintsConfig setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

    /**
     * Set the validateNullable field based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if an invalid value is passed
     */
    public ConstraintsConfig setValidateNullable(String validateNullable) {
        this.validateNullable = parseBoolean(validateNullable);
        return this;
    }

    /**
     * Returns whether a NotNullConst defined for this column should validate.
     * Returns null if not setValidateNullable has not been called.
     */
    public Boolean getValidateNullable() {
        return validateNullable;
    }

    public ConstraintsConfig setValidateNullable(Boolean validateNullable) {
        this.validateNullable = validateNullable;
        return this;
    }

    /**
     * Set the validateUnique field based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if an invalid value is passed
     */
    public ConstraintsConfig setValidateUnique(String validateUnique) {
        this.validateUnique = parseBoolean(validateUnique);
        return this;
    }

    /**
     * Returns whether a UniqueConst defined for this column should validate.
     * Returns null if not setValidateUnique has not been called.
     */
    public Boolean getValidateUnique() {
        return validateUnique;
    }

    public ConstraintsConfig setValidateUnique(Boolean validateUnique) {
        this.validateUnique = validateUnique;
        return this;
    }

    /**
     * Set the validatePrimaryKey field based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if an invalid value is passed
     */
    public ConstraintsConfig setValidatePrimaryKey(String validatePrimaryKey) {
        this.validatePrimaryKey = parseBoolean(validatePrimaryKey);
        return this;
    }

    /**
     * Returns whether a PrimaryKeyConst defined for this column should validate.
     * Returns null if not setValidatePrimaryKey has not been called.
     */
    public Boolean getValidatePrimaryKey() {
        return validatePrimaryKey;
    }

    public ConstraintsConfig setValidatePrimaryKey(Boolean validatePrimaryKey) {
        this.validatePrimaryKey = validatePrimaryKey;
        return this;
    }

    /**
     * Set the validateForeignKey field based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if an invalid value is passed
     */
    public ConstraintsConfig setValidateForeignKey(String validateForeignKey) {
        this.validateForeignKey= parseBoolean(validateForeignKey);
        return this;
    }

    /**
     * Returns whether a ForeignKeyConst defined for this column should validate.
     * Returns null if not setValidateForeignKey has not been called.
     */
    public Boolean getValidateForeignKey() {
        return validateForeignKey;
    }

    public ConstraintsConfig setValidateForeignKey(Boolean validateForeignKey) {
        this.validateForeignKey = validateForeignKey;
        return this;
    }

    /**
     * Set the deferrable parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setDeferrable(String deferrable) {
        this.deferrable = parseBoolean(deferrable);

        return this;
    }

    /**
     * Returns the tablespace to use for the defined primary key. Returns null if not specified.
     */
    public String getPrimaryKeyTablespace() {
        return primaryKeyTablespace;
    }

    public ConstraintsConfig setPrimaryKeyTablespace(String primaryKeyTablespace) {
        this.primaryKeyTablespace = primaryKeyTablespace;
        return this;
    }

    private Boolean parseBoolean(String value) {
        value = StringUtil.trimToNull(value);
        if ((value == null) || "null".equalsIgnoreCase(value)) {
            return null;
        } else {
            if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
                return true;
            } else if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
                return false;
            } else {
                throw new UnexpectedLiquibaseException("Unparsable boolean value: "+value);
            }

        }
    }

    @Override
    public String getSerializedObjectName() {
        return "constraints";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        throw new RuntimeException("TODO");
    }
}
