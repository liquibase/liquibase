package liquibase.ui.interactive.getter;

import liquibase.ui.interactive.AbstractCommandLineValueGetter;
import liquibase.util.FilenameUtil;

import java.nio.file.InvalidPathException;

public class FilenameGetter extends AbstractCommandLineValueGetter<String> {
    public FilenameGetter() {
        super(String.class);
    }

    @Override
    public boolean validate(String input) {
        // If the inputted value contains parts of the path
        try {
            if(
//                    !input.equalsIgnoreCase("s") &&
                            !FilenameUtil.getDirectory(input).isEmpty()){
                throw new IllegalArgumentException("ERROR: Filename cannot contain path elements.");
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("ERROR: Invalid path.");
        }

        return true;
    }

    @Override
    public String convert(String input) {
        return input;
    }
}

