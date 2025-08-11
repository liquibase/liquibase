package liquibase.command.core.init;

public class FilenameGetter extends AbstractCommandLineValueGetter<String> {
    public FilenameGetter() {
        super(String.class);
    }

    @Override
    public boolean validate(String input) {
        if (input.contains("\"")) {
            throw new IllegalArgumentException("The supplied filename contains double quotes, which is not permitted.");
        }
        if (input.contains("\\") || input.contains("/")) {
            throw new IllegalArgumentException("Filename cannot contain path elements.");
        }

        return true;
    }

    @Override
    public String convert(String input) {
        return input;
    }

    @Override
    public String describe() {
        return "valid filename";
    }
}