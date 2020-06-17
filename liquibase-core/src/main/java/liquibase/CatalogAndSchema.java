package liquibase;

import liquibase.database.Database;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.Locale;

/**
 * Object representing a database catalog and schema. This differs from {@link liquibase.structure.core.Schema} in that it has
 * not come from an actual database Schema.
 * <p>
 * A null value for catalogName or schemaName signifies the default catalog/schema.
 */
public class CatalogAndSchema {
    public static final CatalogAndSchema DEFAULT = new CatalogAndSchema(null, null);
    private String catalogName;
    private String schemaName;

    public CatalogAndSchema(String catalogName, String schemaName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
    }

    public enum CatalogAndSchemaCase {
        LOWER_CASE, UPPER_CASE, ORIGINAL_CASE
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public boolean equals(CatalogAndSchema catalogAndSchema, Database accordingTo) {
        if (!accordingTo.supportsCatalogs()) {
            return true;
        }

        CatalogAndSchema workCatalogAndSchema = catalogAndSchema.customize(accordingTo);
        CatalogAndSchema thisCatalogAndSchema = this.customize(accordingTo);

        boolean catalogMatches;
        if (workCatalogAndSchema.getCatalogName() == null) {
            catalogMatches = (thisCatalogAndSchema.getCatalogName() == null);
        } else {
            catalogMatches = equals(accordingTo, workCatalogAndSchema.getCatalogName(),thisCatalogAndSchema.getCatalogName());
        }
        if (!catalogMatches) {
            return false;
        }

        if (accordingTo.supportsSchemas()) {
            if (workCatalogAndSchema.getSchemaName() == null) {
                return thisCatalogAndSchema.getSchemaName() == null;
            } else {
                return equals(accordingTo, workCatalogAndSchema.getSchemaName(), thisCatalogAndSchema.getSchemaName());
            }
        } else {
            return true;
        }
    }


    /**
     * This method returns a new CatalogAndSchema adjusted based on the configuration of the passed database.
     * If the database does not support schemas, the returned object will have a null schema.
     * If the database does not support catalogs, the returned object will have a null catalog.
     * If either the schema or catalog matches the database default catalog or schema, they will be nulled out.
     * Catalog and/or schema names will be upper case unless the database violates the SQL standard by being
     * case-sensitive about some or all unquoted identifiers.
     *
     * @see CatalogAndSchema#customize(liquibase.database.Database)
     * */
    public CatalogAndSchema standardize(Database accordingTo) {
        String workCatalogName = StringUtils.trimToNull(getCatalogName());
        String workSchemaName = StringUtils.trimToNull(getSchemaName());

        if (!accordingTo.supportsCatalogs()) {
            return new CatalogAndSchema(null, null);
        }

        if (accordingTo.supportsSchemas()) {
            if ((workSchemaName != null) && workSchemaName.equalsIgnoreCase(accordingTo.getDefaultSchemaName())) {
                workSchemaName = null;
            }
        } else {
            if ((workCatalogName == null) && (workSchemaName != null)) { //had names in the wrong order
                workCatalogName = workSchemaName;
            }
            workSchemaName = workCatalogName;
        }

        if (workCatalogName != null && equals(accordingTo, workCatalogName, accordingTo.getDefaultCatalogName())) {
            workCatalogName = null;
        }
        if (workSchemaName != null && equals(accordingTo, workSchemaName, accordingTo.getDefaultSchemaName())) {
            workSchemaName = null;
        }
        if (!accordingTo.supportsSchemas() && (workCatalogName != null) && (workSchemaName != null) &&
            !workCatalogName.equals(workSchemaName)) {
            workSchemaName = null;
        }

        if (CatalogAndSchemaCase.LOWER_CASE.equals(accordingTo.getSchemaAndCatalogCase())) {
            if (workCatalogName != null) {
                workCatalogName = workCatalogName.toLowerCase(Locale.US);
            }
            if (workSchemaName != null) {
                workSchemaName = workSchemaName.toLowerCase(Locale.US);
            }
        } else if (CatalogAndSchemaCase.UPPER_CASE.equals(accordingTo.getSchemaAndCatalogCase())) {
            if (!accordingTo.isCaseSensitive()) {
                if (workCatalogName != null) {
                    workCatalogName = workCatalogName.toUpperCase(Locale.US);
                }
                if (workSchemaName != null) {
                    workSchemaName = workSchemaName.toUpperCase(Locale.US);
                }
            }
        }
        return new CatalogAndSchema(workCatalogName, workSchemaName);

    }

    /**
     * Returns a new CatalogAndSchema object with null/default catalog and schema names set to the
     * correct default catalog and schema. If the database does not support catalogs or schemas they will
     * retain a null value.
     * Catalog and schema capitalization will match what the database expects.
     *
     * @see CatalogAndSchema#standardize(liquibase.database.Database)
     */
    public CatalogAndSchema customize(Database accordingTo) {
        CatalogAndSchema standard = standardize(accordingTo);

        String workCatalogName = standard.getCatalogName();
        String workSchemaName = standard.getSchemaName();

        if (workCatalogName == null) {
            if (!accordingTo.supportsSchemas() && (workSchemaName != null)) {
                return new CatalogAndSchema(accordingTo.correctObjectName(workSchemaName, Catalog.class), null);
            }
            workCatalogName = accordingTo.getDefaultCatalogName();
        }

        if (workSchemaName == null) {
            workSchemaName = accordingTo.getDefaultSchemaName();
        }

        if (workCatalogName != null) {
            workCatalogName = accordingTo.correctObjectName(workCatalogName, Catalog.class);
        }
        if (workSchemaName != null) {
            workSchemaName = accordingTo.correctObjectName(workSchemaName, Schema.class);
        }

        return new CatalogAndSchema(workCatalogName, workSchemaName);
    }

    /**
     * String version includes both catalog and schema. If either is null it returns the string "DEFAULT" in its place.
     */
    @Override
    public String toString() {
        String tmpCatalogName = getCatalogName();
        String tmpSchemaName = getSchemaName();

        if (tmpCatalogName == null) {
            tmpCatalogName = "DEFAULT";
        }
        if (tmpSchemaName == null) {
            tmpSchemaName = "DEFAULT";
        }

        return tmpCatalogName + "." + tmpSchemaName;
    }

    /**
     * This method does schema or catalog comparision
     *
     * @param database - it's db object to getSchemaAndCatalogCase
     * @param value1 - schema or catalog to compare with value2
     * @param value2 - schema or catalog to compare with value1
     *
     * @return true if value1 and value2 equal
     */
    private boolean equals(Database database, String value1, String value2) {
        CatalogAndSchemaCase schemaAndCatalogCase = database.getSchemaAndCatalogCase();
        if (CatalogAndSchemaCase.UPPER_CASE.equals(schemaAndCatalogCase) ||
                CatalogAndSchemaCase.LOWER_CASE.equals(schemaAndCatalogCase)) {
            return value1.equalsIgnoreCase(value2);
        }

        return value1.equals(value2);
    }

}
