package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.exception.CommandLineParsingException;
import liquibase.exception.CommandValidationException;
import liquibase.integration.AbstractIntegrationAdapter;
import liquibase.util.StringUtil;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CommandLineIntegrationAdapter extends AbstractIntegrationAdapter {

    private final CommandLine commandLine;

    public CommandLineIntegrationAdapter(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public String[] getCommand() {
        return commandLine.getCommand();
    }

    @Override
    public String getUserErrorMessage(String message, Throwable exception) {
        if (exception instanceof CommandLine.ParameterException) {
            try (final StringWriter suggestionWriter = new StringWriter();
                 PrintWriter suggestionsPrintWriter = new PrintWriter(suggestionWriter)) {

                boolean printSuggestions = CommandLine.UnmatchedArgumentException.printSuggestions((CommandLine.ParameterException) exception, suggestionsPrintWriter);
                suggestionsPrintWriter.flush();

                String suggestions = "";
                if (printSuggestions) {
                    suggestions = suggestionWriter.toString();
                    if (suggestions.length() > 0) {
                        suggestions = "\n" + suggestions;
                    }
                }

                if (exception instanceof CommandLine.UnmatchedArgumentException) {
                    return "Unexpected argument(s): " + StringUtil.join(((CommandLine.UnmatchedArgumentException) exception).getUnmatched(), ", ") +
                            "\n" +
                            "For detailed help, try 'liquibase --help' or 'liquibase <command-name> --help'"
                            + suggestions;

                } else {
                    return "Error parsing command line: " + exception.getMessage() +
                            "\n" +
                            "For detailed help, try 'liquibase --help' or 'liquibase <command-name> --help'" +
                            suggestions;
                }
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Error closing suggestion stream: " + e.getMessage(), e);
            }
        } else if (exception instanceof IllegalArgumentException
                || exception instanceof CommandValidationException
                || exception instanceof CommandLineParsingException) {
            return "Error parsing command line: " + exception.getMessage() +
                    "\n" +
                    "For detailed help, try 'liquibase --help' or 'liquibase <command-name> --help'";
        }

        return null;
    }
}
