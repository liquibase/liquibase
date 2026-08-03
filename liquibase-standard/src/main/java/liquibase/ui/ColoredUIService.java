package liquibase.ui;

import liquibase.AbstractExtensibleObject;

public class ColoredUIService extends AbstractExtensibleObject implements UIService {
    private final UIService delegate;
    private static final String RESET = "\u001B[0m";

    // Basic colors
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String ORANGE = "\u001B[38;5;214m";
    private static final String PURPLE = "\u001B[35m";

    // Light variations
    private static final String LIGHT_RED = "\u001B[91m";
    private static final String LIGHT_GREEN = "\u001B[92m";
    private static final String LIGHT_BLUE = "\u001B[94m";
    private static final String LIGHT_ORANGE = "\u001B[38;5;215m";
    private static final String LIGHT_PURPLE = "\u001B[95m";

    // Dark variations
    private static final String DARK_RED = "\u001B[38;5;88m";
    private static final String DARK_GREEN = "\u001B[38;5;22m";
    private static final String DARK_BLUE = "\u001B[38;5;17m";
    private static final String DARK_ORANGE = "\u001B[38;5;172m";
    private static final String DARK_PURPLE = "\u001B[38;5;54m";

    public ColoredUIService(UIService delegate) {
        this.delegate = delegate;
    }

    public void sendColoredMessage(String... parts) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < parts.length; i += 2) {
            String text = parts[i];
            String color = i + 1 < parts.length ? parts[i + 1].toLowerCase() : "default";
            message.append(getColorCode(color)).append(text).append(RESET);
        }
        delegate.sendMessage(message.toString());
    }

    private String getColorCode(String color) {
        switch (color) {
            case "red":
                return RED;
            case "green":
                return GREEN;
            case "blue":
                return BLUE;
            case "orange":
                return ORANGE;
            case "purple":
                return PURPLE;
            case "light_red":
                return LIGHT_RED;
            case "light_green":
                return LIGHT_GREEN;
            case "light_blue":
                return LIGHT_BLUE;
            case "light_orange":
                return LIGHT_ORANGE;
            case "light_purple":
                return LIGHT_PURPLE;
            case "dark_red":
                return DARK_RED;
            case "dark_green":
                return DARK_GREEN;
            case "dark_blue":
                return DARK_BLUE;
            case "dark_orange":
                return DARK_ORANGE;
            case "dark_purple":
                return DARK_PURPLE;
            default:
                return "";
        }
    }

    // Required UIService interface methods
    @Override
    public void sendMessage(String message) {
        delegate.sendMessage(message);
    }

    @Override
    public void sendErrorMessage(String message) {
        delegate.sendErrorMessage(message);
    }

    @Override
    public void sendErrorMessage(String message, Throwable exception) {
        delegate.sendErrorMessage(message, exception);
    }

    @Override
    public <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type) {
        return delegate.prompt(prompt, valueIfNoEntry, inputHandler, type);
    }

    @Override
    public void setAllowPrompt(boolean allowPrompt) throws IllegalArgumentException {
        delegate.setAllowPrompt(allowPrompt);
    }

    @Override
    public boolean getAllowPrompt() {
        return delegate.getAllowPrompt();
    }

    @Override
    public int getPriority() {
        return delegate.getPriority();
    }

    public void send(String word) {
        System.out.print(word);
    }
}