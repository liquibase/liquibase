package liquibase.parser;

import liquibase.plugin.Plugin;
import liquibase.util.StringClauses;

public interface LiquibaseSqlParser extends Plugin {

    StringClauses parse(String sqlBlock);

    StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments);

    int getPriority();
}
