package liquibase.sdk.convert;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.servicelocator.LiquibaseService;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@LiquibaseService(skip = true)
public class ConvertCommandStep extends AbstractCommandStep {

    public static final CommandArgumentDefinition<String> SRC_ARG;
    public static final CommandArgumentDefinition<String> OUT_ARG;
    public static final CommandArgumentDefinition<String> CLASSPATH_ARG;

    static {
        final CommandBuilder builder = new CommandBuilder(new String[] {"convert"});
        SRC_ARG = builder.argument("src", String.class).required().build();
        OUT_ARG = builder.argument("out", String.class).required().build();
        CLASSPATH_ARG = builder.argument("classpath", String.class).required().build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{new String[] {"convert"}};
    }

    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        return -1;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String src = commandScope.getArgumentValue(SRC_ARG);
        String out = commandScope.getArgumentValue(OUT_ARG);
        String classpath = commandScope.getArgumentValue(CLASSPATH_ARG);

        List<ResourceAccessor> openers = new ArrayList<>();
        openers.add(new DirectoryResourceAccessor(new File(".")));
        openers.add(new ClassLoaderResourceAccessor());
        if (classpath != null) {
            openers.add(new DirectoryResourceAccessor(new File(classpath)));
        }
        ResourceAccessor resourceAccessor = new CompositeResourceAccessor(openers);

        ChangeLogParser sourceParser = ChangeLogParserFactory.getInstance().getParser(src, resourceAccessor);
        ChangeLogSerializer outSerializer = ChangeLogSerializerFactory.getInstance().getSerializer(out);

        DatabaseChangeLog changeLog = sourceParser.parse(src, new ChangeLogParameters(), resourceAccessor);

        File outFile = new File(out);
        if (!outFile.exists()) {
            outFile.getParentFile().mkdirs();
        }
        
        try (OutputStream outputStream = Files.newOutputStream(outFile.toPath())) {
            outSerializer.write(changeLog.getChangeSets(), outputStream);
        }

        Scope.getCurrentScope().getUI().sendMessage("Converted successfully");
    }
}
