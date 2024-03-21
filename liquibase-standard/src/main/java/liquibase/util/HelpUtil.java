package liquibase.util;

import liquibase.command.CommandDefinition;

public class HelpUtil {

    public static final String HISTORY_URL = "https://docs.liquibase.com/DATABASECHANGELOGHISTORY";
    /**
     * Hides the command name when running liquibase --help
     * @param commandDefinition the command definition to adjust
     */
    public static void hideCommandNameInHelpView(CommandDefinition commandDefinition) {
        if (commandDefinition.getPipeline().size() == 1) {
            commandDefinition.setInternal(true);
        }
    }
}
