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

        if (catalogName != null && catalogName.equalsIgnoreCase(accordingTo.getDefaultCatalogName())) {
            catalogName = null;
        }

        if (!accordingTo.supportsSchemas()) {
            schemaName = null;
        }

        if (catalogName != null) {
            catalogName = catalogName.toUpperCase();
        }
        if (schemaName != null) {
            schemaName = schemaName.toUpperCase();
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
}
