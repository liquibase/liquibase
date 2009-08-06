package liquibase.database.structure;

public enum ForeignKeyConstraintType {
    importedKeyCascade,
    importedKeySetNull,
    importedKeySetDefault,
    importedKeyRestrict,
    importedKeyNoAction
}
