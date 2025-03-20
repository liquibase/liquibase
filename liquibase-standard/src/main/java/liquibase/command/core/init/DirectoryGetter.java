package liquibase.command.core.init;

import java.io.File;

public class DirectoryGetter extends AbstractCommandLineValueGetter<String> {
    public DirectoryGetter() {
        super(String.class);
    }

    @Override
    public boolean validate(String input) {
        if (input.contains("\"")) {
            throw new IllegalArgumentException("The supplied path contains double quotes, which is not permitted.");
        }
        File f = new File(input);
        if (f.exists()){
            if (!f.isDirectory()) {
                throw new IllegalArgumentException("The supplied path is actually a file and cannot be used.");
            }
            return f.canWrite();
        }
        return true;
    }

    @Override
    public String convert(String input) {
        return input;
    }

    @Override
    public String describe() {
        return "path to a directory";
    }
}
