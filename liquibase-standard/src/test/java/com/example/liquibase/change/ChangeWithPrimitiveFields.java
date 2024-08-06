package com.example.liquibase.change;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@DatabaseChange(
        name = "primitiveChange",
        description = "Used in unit tests",
        priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class ChangeWithPrimitiveFields extends AbstractChange {

    private boolean aBoolean;
    private byte aByte;
    private char aChar;
    private double aDouble;
    private float aFloat;
    private int anInt;
    private long aLong;
    private short aShort;

    @Override
    public String getConfirmationMessage() {
        return "Test confirmation message";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return SqlStatement.EMPTY_SQL_STATEMENT;
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
