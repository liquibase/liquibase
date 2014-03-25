package liquibase;

import liquibase.database.Database;
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

        catalogAndSchema = catalogAndSchema.correct(accordingTo);
        CatalogAndSchema thisCatalogAndSchema = this.correct(accordingTo);

        boolean catalogMatches;
        if (catalogAndSchema.getCatalogName() == null) {
            catalogMatches = (thisCatalogAndSchema.getCatalogName() == null);
        } else {
            catalogMatches = catalogAndSchema.getCatalogName().equalsIgnoreCase(thisCatalogAndSchema.getCatalogName());
        }

        if (!catalogMatches) {
            return false;
        }

        if (accordingTo.supportsSchemas()) {
            if (catalogAndSchema.getSchemaName() == null) {
                return thisCatalogAndSchema.getSchemaName() == null;
            } else {
                return catalogAndSchema.getSchemaName().equalsIgnoreCase(thisCatalogAndSchema.getSchemaName());
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
     */
    public CatalogAndSchema correct(Database accordingTo) {
        String catalogName = StringUtils.trimToNull(getCatalogName());
        String schemaName = StringUtils.trimToNull(getSchemaName());

        if (!accordingTo.supportsCatalogs()) {
            return new CatalogAndSchema(null, null);
        }

        if (catalogName != null && catalogName.equalsIgnoreCase(accordingTo.getDefaultCatalogName())) {
            catalogName = null;
        }

        if (accordingTo.supportsSchemas()) {
            if (schemaName != null && schemaName.equalsIgnoreCase(accordingTo.getDefaultSchemaName())) {
                schemaName = null;
            }
        } else {
            schemaName = null;
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
}
