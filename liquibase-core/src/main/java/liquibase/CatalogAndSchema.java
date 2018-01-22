package liquibase;

import liquibase.database.Database;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

/**
 * Object representing a database catalog and schema. This differs from {@link liquibase.structure.core.Schema} in that it has
 * not come from an actual database Schema.
 * <p>
 * A null value for catalogName or schemaName signifies the default catalog/schema.
 */
public class CatalogAndSchema {
    private String catalogName;
    private String schemaName;
    public static final CatalogAndSchema DEFAULT = new CatalogAndSchema(null, null);

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

        catalogAndSchema = catalogAndSchema.customize(accordingTo);
        CatalogAndSchema thisCatalogAndSchema = this.customize(accordingTo);

        boolean catalogMatches;
        if (catalogAndSchema.getCatalogName() == null) {
            catalogMatches = (thisCatalogAndSchema.getCatalogName() == null);
        } else {
            catalogMatches = equals(accordingTo, catalogAndSchema.getCatalogName(),thisCatalogAndSchema.getCatalogName());
        }
        if (!catalogMatches) {
            return false;
        }

        if (accordingTo.supportsSchemas()) {
            if (catalogAndSchema.getSchemaName() == null) {
                return thisCatalogAndSchema.getSchemaName() == null;
            } else {
                return equals(accordingTo, catalogAndSchema.getSchemaName(), thisCatalogAndSchema.getSchemaName());
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
     * Catalog and/or schema names will be upper case.
     *
     * @see {@link CatalogAndSchema#customize(liquibase.database.Database)}
     * */
    public CatalogAndSchema standardize(Database accordingTo) {
        String catalogName = StringUtils.trimToNull(getCatalogName());
        String schemaName = StringUtils.trimToNull(getSchemaName());

        if (!accordingTo.supportsCatalogs()) {
            return new CatalogAndSchema(null, null);
        }

        if (accordingTo.supportsSchemas()) {
            if (schemaName != null && schemaName.equalsIgnoreCase(accordingTo.getDefaultSchemaName())) {
                schemaName = null;
            }
        } else {
            if (catalogName == null && schemaName != null) { //had names in the wrong order
                catalogName = schemaName;
            }
            schemaName = catalogName;
        }

        if (catalogName != null && equals(accordingTo, catalogName, accordingTo.getDefaultCatalogName())) {
            catalogName = null;
        }
        if (schemaName != null && equals(accordingTo, schemaName, accordingTo.getDefaultSchemaName())) {
            schemaName = null;
        }
        if (!accordingTo.supportsSchemas() && catalogName != null && schemaName != null && !catalogName.equals(schemaName)) {
            schemaName = null;
        }

        if (CatalogAndSchemaCase.LOWER_CASE.equals(accordingTo.getSchemaAndCatalogCase())) {
            if (catalogName != null) {
                catalogName = catalogName.toLowerCase();
            }
            if (schemaName != null) {
                schemaName = schemaName.toLowerCase();
            }
        } else if (CatalogAndSchemaCase.UPPER_CASE.equals(accordingTo.getSchemaAndCatalogCase())) {
            if (catalogName != null) {
                catalogName = catalogName.toUpperCase();
            }
            if (schemaName != null) {
                schemaName = schemaName.toUpperCase();
            }
        }
        return new CatalogAndSchema(catalogName, schemaName);

    }

    /**
     * Returns a new CatalogAndSchema object with null/default catalog and schema names set to the
     * correct default catalog and schema. If the database does not support catalogs or schemas they will
     * retain a null value.
     * Catalog and schema capitalization will match what the database expects.
     *
     * @see {@link CatalogAndSchema#standardize(liquibase.database.Database)}
     */
    public CatalogAndSchema customize(Database accordingTo) {
        CatalogAndSchema standard = standardize(accordingTo);

        String catalogName = standard.getCatalogName();
        String schemaName = standard.getSchemaName();

        if (catalogName == null) {
            if (!accordingTo.supportsSchemas() && schemaName != null) {
                return new CatalogAndSchema(accordingTo.correctObjectName(schemaName, Catalog.class), null);
            }
            catalogName = accordingTo.getDefaultCatalogName();
        }

        if (schemaName == null) {
            schemaName = accordingTo.getDefaultSchemaName();
        }

        if (catalogName != null) {
            catalogName = accordingTo.correctObjectName(catalogName, Catalog.class);
        }
        if (schemaName != null) {
            schemaName = accordingTo.correctObjectName(schemaName, Schema.class);
        }

        return new CatalogAndSchema(catalogName, schemaName);
    }

    /**
     * String version includes both catalog and schema. If either is null it returns the string "DEFAULT" in its place.
     */
    @Override
    public String toString() {
        String catalogName = getCatalogName();
        String schemaName = getSchemaName();

        if (catalogName == null) {
            catalogName = "DEFAULT";
        }
        if (schemaName == null) {
            schemaName = "DEFAULT";
        }

        return catalogName+"."+schemaName;
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
