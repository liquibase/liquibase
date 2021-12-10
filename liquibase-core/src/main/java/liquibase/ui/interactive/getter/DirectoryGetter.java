package liquibase.ui.interactive.getter;

import liquibase.ui.interactive.AbstractCommandLineValueGetter;

import java.io.File;

public class DirectoryGetter extends AbstractCommandLineValueGetter<String> {
    public DirectoryGetter() {
        super(String.class);
    }

    @Override
    public boolean validate(String input) {
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
}

