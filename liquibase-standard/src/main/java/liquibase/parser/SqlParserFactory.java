package liquibase.parser;


import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlParserFactory extends AbstractPluginFactory<LiquibaseSqlParser> {
    @Override
    protected Class<LiquibaseSqlParser> getPluginClass() {
        return LiquibaseSqlParser.class;
    }

    @Override
    protected int getPriority(LiquibaseSqlParser obj, Object... args) {
        return obj.getPriority();
    }

    public LiquibaseSqlParser getSqlParser() {
        return getPlugin();
    }

    public void unregister(LiquibaseSqlParser liquibaseSqlParser) {
        removeInstance(liquibaseSqlParser);
    }
}
