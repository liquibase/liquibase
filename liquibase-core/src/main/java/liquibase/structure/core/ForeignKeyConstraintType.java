package liquibase.structure.core;

public enum ForeignKeyConstraintType {
    importedKeyCascade,
    importedKeySetNull,
    importedKeySetDefault,
    importedKeyRestrict,
    importedKeyNoAction
}
