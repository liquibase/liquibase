package liquibase.integration;

import liquibase.Scope;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandFailedException;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.util.SortedSet;

public class Integration<IntegrationAdapter extends AbstractIntegrationAdapter> {

    private final IntegrationAdapter adapter;

    public Integration(IntegrationAdapter adapter) {
        this.adapter = adapter;
    }

    public IntegrationAdapter getAdapter() {
        return adapter;
    }

    public int handleException(Throwable exception) {
        Throwable cause = exception;

        String uiMessage = "";
        while (cause != null) {
            String newMessage = StringUtil.trimToNull(cleanExceptionMessage(cause.getMessage()));
            if (newMessage != null) {
                if (!uiMessage.contains(newMessage)) {
                    if (!uiMessage.equals("")) {
                        uiMessage += System.lineSeparator() + "  - Caused by: ";
                    }
                    uiMessage += newMessage;
                }
            }

            cause = cause.getCause();
        }

        if (StringUtil.isEmpty(uiMessage)) {
            uiMessage = exception.getClass().getName();
        }

        String userErrorMessage = adapter.getUserErrorMessage(uiMessage, exception);

        Logger log = Scope.getCurrentScope().getLog(getClass());
        if (cause instanceof CommandFailedException && ((CommandFailedException) cause).isExpected()) {
            log.severe(uiMessage);
        } else {
            log.severe(uiMessage, exception);
        }

        if (userErrorMessage != null) {
            log.severe(userErrorMessage);
        } else if (exception.getCause() != null && exception.getCause() instanceof CommandFailedException) {
            log.severe(uiMessage);
        } else {
            log.severe("Unexpected error running Liquibase: " + uiMessage, exception);
        }

        if (exception.getCause() != null && exception.getCause() instanceof CommandFailedException) {
            CommandFailedException cfe = (CommandFailedException) exception.getCause();
            return cfe.getExitCode();
        }
        return 1;
    }

    protected String cleanExceptionMessage(String message) {
        if (message == null) {
            return null;
        }

        String originalMessage;
        do {
            originalMessage = message;
            message = message.replaceFirst("^[\\w.]*Exception: ", "");
            message = message.replaceFirst("^[\\w.]*Error: ", "");
        } while (!originalMessage.equals(message));

        message = message.replace("Unexpected error running Liquibase: ", "");
        return message;
    }

    public SortedSet<CommandDefinition> getAvailableCommands() {
        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);
        return commandFactory.getCommands(false);
    }

}
