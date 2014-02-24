package liquibase;

import liquibase.database.Database;

public class CatalogAndSchema {
    private String catalogName;
    private String schemaName;
    public static final CatalogAndSchema DEFAULT = new CatalogAndSchema(null, null);

    public CatalogAndSchema(String catalogName, String schemaName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
    }

    public CatalogAndSchema(String catalogAndOrSchema) {
        String[] split = catalogAndOrSchema.split("\\.");
        if (split.length == 1) {
            schemaName = split[0];
        } else {
            catalogName = split[0];
            schemaName = split[1];
        }

    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public boolean equals(CatalogAndSchema catalogAndSchema, Database database) {
        catalogAndSchema = database.correctSchema(catalogAndSchema);
        CatalogAndSchema thisCatalogAndSchema = database.correctSchema(this);

        return catalogAndSchema.toString().equalsIgnoreCase(thisCatalogAndSchema.toString());
    }

    @Override
    public String toString() {
        if (catalogName == null && schemaName == null) {
            return "DEFAULT";
        }
        if (catalogName != null && schemaName == null) {
            return catalogName;
        }
        if (catalogName == null && schemaName != null) {
            return schemaName;
        }
        return catalogName+"."+schemaName;
    }
}
