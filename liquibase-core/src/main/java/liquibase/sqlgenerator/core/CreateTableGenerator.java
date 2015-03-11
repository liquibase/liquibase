package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DatabaseDataType;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.FulltextConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CreateTableGenerator extends AbstractSqlGenerator<CreateTableStatement> {

    @Override
    public ValidationErrors validate(CreateTableStatement createTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createTableStatement.getTableName());
        validationErrors.checkRequiredField("columns", createTableStatement.getColumns());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        
        if (database instanceof InformixDatabase) {
                AbstractSqlGenerator<CreateTableStatement> gen = new CreateTableGeneratorInformix();
                return gen.generateSql(statement, database, sqlGeneratorChain);
        }

        List<Sql> additionalSql = new ArrayList<Sql>();
        String sql = "";
        boolean create = true;
        
        //if ( database instanceof OracleDatabase ) {
        //    if( statement.getTableName().startsWith(sql) ){
         //       create = false;
         //   }
        //}
        
        if ( create ) { 
            
            StringBuffer buffer = new StringBuffer();
            buffer.append("CREATE TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" ");
            buffer.append("(");

            boolean isSinglePrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null
                && statement.getPrimaryKeyConstraint().getColumns().size() == 1;

            boolean isPrimaryKeyAutoIncrement = false;

            Iterator<String> columnIterator = statement.getColumns().iterator();
            List<String> primaryKeyColumns = new LinkedList<String>();

            BigInteger mysqlTableOptionStartWith = null;

            while (columnIterator.hasNext()) {
                String column = columnIterator.next();
                DatabaseDataType columnType = statement.getColumnTypes().get(column).toDatabaseDataType(database);
                buffer.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column));

                if ( database instanceof OracleDatabase) {
                    if( columnType.toString().trim().toLowerCase().startsWith("enum") ){                    
                        columnType = new DatabaseDataType("VARCHAR2", 255);                    
                    }
                    if( columnType.toString().trim().toLowerCase().startsWith("decimal") ){ 
                        String param = columnType.toString().toLowerCase().replace("(", "").replace("decimal", "");
                        String[] par = param.split("\\)");
                        param = par[0].trim();
                        Object[] parameters = param.split(",");

                        columnType = new DatabaseDataType("NUMBER", parameters);                    
                    }
                    if( columnType.toString().trim().toLowerCase().startsWith("int") ){                    
                        columnType = new DatabaseDataType("NUMBER");                    
                    }
                }

                buffer.append(" ").append(columnType);

                AutoIncrementConstraint autoIncrementConstraint = null;

                for (AutoIncrementConstraint currentAutoIncrementConstraint : statement.getAutoIncrementConstraints()) {
                    if (column.equals(currentAutoIncrementConstraint.getColumnName())) {
                        autoIncrementConstraint = currentAutoIncrementConstraint;
                        break;
                    }
                }

                boolean isAutoIncrementColumn = autoIncrementConstraint != null;
                boolean isPrimaryKeyColumn = statement.getPrimaryKeyConstraint() != null
                        && statement.getPrimaryKeyConstraint().getColumns().contains(column);
                isPrimaryKeyAutoIncrement = isPrimaryKeyAutoIncrement
                        || isPrimaryKeyColumn && isAutoIncrementColumn;

                if (isPrimaryKeyColumn) {
                    primaryKeyColumns.add(column);
                }

                if ((database instanceof SQLiteDatabase) &&
                        isSinglePrimaryKeyColumn &&
                        isPrimaryKeyColumn &&
                        isAutoIncrementColumn) {
                    String pkName = StringUtils.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
                    if (pkName == null) {
                        pkName = database.generatePrimaryKeyName(statement.getTableName());
                    }
                    if (pkName != null) {
                        buffer.append(" CONSTRAINT ");
                        buffer.append(database.escapeConstraintName(pkName));
                    }
                    buffer.append(" PRIMARY KEY");
                }

                // for the serial data type in postgres, there should be no default value
                if (!columnType.isAutoIncrement() && statement.getDefaultValue(column) != null) {
                    Object defaultValue = statement.getDefaultValue(column);
                    if (database instanceof MSSQLDatabase) {
                        buffer.append(" CONSTRAINT ").append(((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), column));
                    }

                    if ( database instanceof OracleDatabase) {
                        if( !defaultValue.equals("0000-00-00 00:00:00") &&  !defaultValue.equals("0000-00-00") &&  !defaultValue.equals("00:00:00") ){
                            buffer.append(" DEFAULT ");
                            buffer.append(statement.getColumnTypes().get(column).objectToSql(defaultValue, database));
                        }
                    }else{
                        buffer.append(" DEFAULT ");
                        buffer.append(statement.getColumnTypes().get(column).objectToSql(defaultValue, database));
                    }
                }

                if (isAutoIncrementColumn) {
                    // TODO: check if database supports auto increment on non primary key column
                    if (database.supportsAutoIncrement()) {
                        String autoIncrementClause = database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());

                        if (!"".equals(autoIncrementClause)) {
                            buffer.append(" ").append(autoIncrementClause);
                        }

                        if( autoIncrementConstraint.getStartWith() != null ){
                                if (database instanceof PostgresDatabase) {
                                    String sequenceName = statement.getTableName()+"_"+column+"_seq";
                                    additionalSql.add(new UnparsedSql("alter sequence "+database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), sequenceName)+" start with "+autoIncrementConstraint.getStartWith(), new Sequence().setName(sequenceName).setSchema(statement.getCatalogName(), statement.getSchemaName())));
                                }else if(database instanceof MySQLDatabase){
                                    mysqlTableOptionStartWith = autoIncrementConstraint.getStartWith();
                                }
                        }
                        
                         if ( database instanceof OracleDatabase) {
                             
                             String sequence = "CREATE SEQUENCE \""+statement.getTableName()+"_SEQ\"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE";
                             
                             String trigger = "create or replace trigger \""+statement.getTableName()+"_TRIGGER\"\n" +
                                                        "before insert on \""+statement.getTableName()+"\"\n" +
                                                        "for each row\n" +
                                                        "declare\n" +
                                                        "    max_id number;\n" +
                                                        "    cur_seq number;\n" +
                                                        "begin\n" +
                                                        "    if :new.\""+column+"\" is null then\n" +
                                                        "        -- No ID passed, get one from the sequence\n" +
                                                        "        select \""+statement.getTableName()+"_SEQ\".nextval into :new.\""+column+"\" from dual;\n" +
                                                        "    else\n" +
                                                        "        -- ID was set via insert, so update the sequence\n" +
                                                        "        select greatest(nvl(max(\""+column+"\"),0), :new.\"id\") into max_id from \""+statement.getTableName()+"\";\n" +
                                                        "        select \""+statement.getTableName()+"_SEQ\".nextval into cur_seq from dual;\n" +
                                                        "        while cur_seq < max_id\n" +
                                                        "        loop\n" +
                                                        "            select \""+statement.getTableName()+"_SEQ\".nextval into cur_seq from dual;\n" +
                                                        "        end loop;\n" +
                                                        "    end if;\n" +
                                                        "end";
                             
                             
                             
                             additionalSql.add(new UnparsedSql(sequence));
                             additionalSql.add(new UnparsedSql(trigger));
                             
                         }
                        
                        
                    } else {
                        LogFactory.getLogger().warning(database.getShortName()+" does not support autoincrement columns as requested for "+(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())));
                    }
                }

                if (statement.getNotNullColumns().contains(column)) {
                    buffer.append(" NOT NULL");
                } else {
                    if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase || database instanceof MySQLDatabase) {
                        buffer.append(" NULL");
                    }
                }

                if (database instanceof InformixDatabase && isSinglePrimaryKeyColumn && isPrimaryKeyColumn) {
                    //buffer.append(" PRIMARY KEY");
                }

                if(database instanceof MySQLDatabase && statement.getColumnRemarks(column) != null){
                    buffer.append(" COMMENT '" + database.escapeStringForDatabase(statement.getColumnRemarks(column)) + "'");

                }

                if (columnIterator.hasNext()) {
                    buffer.append(", ");
                }
            }

            buffer.append(",");

            if (!( (database instanceof SQLiteDatabase) &&
                    isSinglePrimaryKeyColumn &&
                    isPrimaryKeyAutoIncrement) &&

                    !((database instanceof InformixDatabase) &&
                    isSinglePrimaryKeyColumn
                    )) {
                // ...skip this code block for sqlite if a single column primary key
                // with an autoincrement constraint exists.
                // This constraint is added after the column type.

                if (statement.getPrimaryKeyConstraint() != null && statement.getPrimaryKeyConstraint().getColumns().size() > 0) {
                    if (database.supportsPrimaryKeyNames()) {
                        String pkName = StringUtils.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
                        if (pkName == null) {
                            // TODO ORA-00972: identifier is too long
                            // If tableName lenght is more then 28 symbols
                            // then generated pkName will be incorrect
                            pkName = database.generatePrimaryKeyName(statement.getTableName());
                        }
                        if (pkName != null) {
                            buffer.append(" CONSTRAINT ");
                            buffer.append(database.escapeConstraintName(pkName));
                        }
                    }
                    buffer.append(" PRIMARY KEY (");
                    buffer.append(database.escapeColumnNameList(StringUtils.join(statement.getPrimaryKeyConstraint().getColumns(), ", ")));
                    buffer.append(")");
                    // Setting up table space for PK's index if it exist
                    if (database instanceof OracleDatabase &&
                        statement.getPrimaryKeyConstraint().getTablespace() != null) {
                        buffer.append(" USING INDEX TABLESPACE ");
                        buffer.append(statement.getPrimaryKeyConstraint().getTablespace());
                    }
                    buffer.append(",");
                }
            }

            for (ForeignKeyConstraint fkConstraint : statement.getForeignKeyConstraints()) {
                if (!(database instanceof InformixDatabase)) {
                    buffer.append(" CONSTRAINT ");
                    buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));
                }
                String referencesString = fkConstraint.getReferences();

                buffer.append(" FOREIGN KEY (")
                        .append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), fkConstraint.getColumn()))
                        .append(") REFERENCES ");
                if (referencesString != null) {
                    if (!referencesString.contains(".") && database.getDefaultSchemaName() != null && database.getOutputDefaultSchema()) {
                        referencesString = database.getDefaultSchemaName() +"."+referencesString;
                    }
                    buffer.append(referencesString);
                } else {
                    buffer.append(database.escapeObjectName(fkConstraint.getReferencedTableName(), Table.class))
                        .append("(")
                        .append(database.escapeColumnNameList(fkConstraint.getReferencedColumnNames()))
                        .append(")");

                }


                if (fkConstraint.isDeleteCascade()) {
                    buffer.append(" ON DELETE CASCADE");
                }

                if ((database instanceof InformixDatabase)) {
                    buffer.append(" CONSTRAINT ");
                    buffer.append(database.escapeConstraintName(fkConstraint.getForeignKeyName()));
                }

                if (fkConstraint.isInitiallyDeferred()) {
                    buffer.append(" INITIALLY DEFERRED");
                }
                if (fkConstraint.isDeferrable()) {
                    buffer.append(" DEFERRABLE");
                }
                buffer.append(",");
            }

            for (UniqueConstraint uniqueConstraint : statement.getUniqueConstraints()) {
                if (uniqueConstraint.getConstraintName() != null && !constraintNameAfterUnique(database)) {
                    buffer.append(" CONSTRAINT ");
                    buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
                }
                buffer.append(" UNIQUE (");
                buffer.append(database.escapeColumnNameList(StringUtils.join(uniqueConstraint.getColumns(), ", ")));
                buffer.append(")");
                if (uniqueConstraint.getConstraintName() != null && constraintNameAfterUnique(database)) {
                    buffer.append(" CONSTRAINT ");
                    buffer.append(database.escapeConstraintName(uniqueConstraint.getConstraintName()));
                }
                buffer.append(",");
            }

            for (FulltextConstraint fulltextConstraint : statement.getFulltextConstraints()) {
                if (fulltextConstraint.getConstraintName() != null && !constraintNameAfterFulltext(database)) {
                    buffer.append(" CONSTRAINT ");
                    buffer.append(database.escapeConstraintName(fulltextConstraint.getConstraintName()));
                }
                buffer.append(" FULLTEXT (");
                buffer.append(database.escapeColumnNameList(StringUtils.join(fulltextConstraint.getColumns(), ", ")));
                buffer.append(")");

                buffer.append(",");
            }
    //        if (constraints != null && constraints.getCheckConstraint() != null) {
    //            buffer.append(constraints.getCheckConstraint()).append(" ");
    //        }
    //    }


            sql = buffer.toString().replaceFirst(",\\s*$", "")+")";

            if (database instanceof MySQLDatabase && mysqlTableOptionStartWith != null){
                    LogFactory.getLogger().info("[MySQL] Using last startWith statement ("+mysqlTableOptionStartWith.toString()+") as table option.");
                    sql += " "+((MySQLDatabase)database).getTableOptionAutoIncrementStartWithClause(mysqlTableOptionStartWith);
            }


    //        if (StringUtils.trimToNull(tablespace) != null && database.supportsTablespaces()) {
    //            if (database instanceof MSSQLDatabase) {
    //                buffer.append(" ON ").append(tablespace);
    //            } else if (database instanceof DB2Database) {
    //                buffer.append(" IN ").append(tablespace);
    //            } else {
    //                buffer.append(" TABLESPACE ").append(tablespace);
    //            }
    //        }

            if (statement.getTablespace() != null && database.supportsTablespaces()) {
                if (database instanceof MSSQLDatabase || database instanceof SybaseASADatabase) {
                    sql += " ON " + statement.getTablespace();
                } else if (database instanceof DB2Database || database instanceof InformixDatabase) {
                    sql += " IN " + statement.getTablespace();
                } else {
                    sql += " TABLESPACE " + statement.getTablespace();
                }
            }

            if( database instanceof MySQLDatabase && statement.getRemarks() != null) {
                sql += " COMMENT='"+database.escapeStringForDatabase(statement.getRemarks())+"' ";
            }

        }
        
        additionalSql.add(0, new UnparsedSql(sql, getAffectedTable(statement)));
        return additionalSql.toArray(new Sql[additionalSql.size()]);
        
        
        
    }

    protected Relation getAffectedTable(CreateTableStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
    }

    private boolean constraintNameAfterUnique(Database database) {
        return database instanceof InformixDatabase;
    }
    
    private boolean constraintNameAfterFulltext(Database database) {
        return database instanceof InformixDatabase;
    }

}
