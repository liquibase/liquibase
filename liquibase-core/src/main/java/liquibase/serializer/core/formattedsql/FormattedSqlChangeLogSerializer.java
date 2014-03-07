package liquibase.serializer.core.formattedsql;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedSqlChangeLogSerializer  implements ChangeLogSerializer {

    private static Pattern fileNamePatter = Pattern.compile(".*\\.(\\w+)\\.sql");

    @Override
    public String[] getValidFileExtensions() {
        return new String[] {
                "sql"
        };
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        if (object instanceof ChangeSet) {
            StringBuilder builder = new StringBuilder();

            ChangeSet changeSet = (ChangeSet) object;
            Database database = getTargetDatabase(changeSet);

            String author = (changeSet.getAuthor()).replaceAll("\\s+", "_");
            author = author.replace("_(generated)","");

            builder.append("--changeset ").append(author).append(":").append(changeSet.getId()).append("\n");
            for (Change change : changeSet.getChanges()) {
                Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(change.generateStatements(database), database);
                if (sqls != null) {
                    for (Sql sql : sqls) {
                        builder.append(sql.toSql()).append(sql.getEndDelimiter()).append("\n");
                    }
                }
            }

            return builder.toString();
        } else {
            throw new UnexpectedLiquibaseException("Cannot serialize object type: "+object.getClass().getName());
        }
    }

    protected Database getTargetDatabase(ChangeSet changeSet) {
        String filePath = changeSet.getFilePath();
        if (filePath == null) {
            throw new UnexpectedLiquibaseException("No changeset file path set. Cannot determine target database type");
        }
        Matcher matcher = fileNamePatter.matcher(filePath);
        if (!matcher.matches()) {
            throw new UnexpectedLiquibaseException("Serializing changelog as sql requires a file name in the format *.databaseType.sql. Example: changelog.h2.sql. Passed: "+filePath);
        }
        String shortName = matcher.replaceFirst("$1");

        Database database = DatabaseFactory.getInstance().getDatabase(shortName);

        if (database == null) {
            throw new UnexpectedLiquibaseException("Serializing changelog as sql requires a file name in the format *.databaseType.sql. Example: changelog.h2.sql. Unknown databaes type: "+shortName);
        }

        return database;
    }

    @Override
    public void write(List<ChangeSet> changeSets, OutputStream out) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("--liquibase formatted sql\n\n");

        for (ChangeSet changeSet : changeSets) {
            builder.append(serialize(changeSet, true));
            builder.append("\n");
        }

        out.write(builder.toString().getBytes("UTF-8"));

    }

    @Override
    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {

    }
}
