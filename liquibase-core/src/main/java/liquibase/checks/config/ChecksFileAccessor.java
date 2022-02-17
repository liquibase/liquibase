package liquibase.checks.config;

import liquibase.Scope;
import liquibase.exception.CommandExecutionException;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChecksFileAccessor implements liquibase.checks.config.FileAccessor {
    @Override
    public FileAccessorDTO loadFileContents(String filename) throws IOException {
        File f = new File(filename);
        if (!f.exists()) {
            Scope.getCurrentScope().getLog(getClass()).info("No configuration file named '" + filename + "' found.");
            throw new FileNotFoundException("Check settings configuration file not found.");
        } else {
            Scope.getCurrentScope().getLog(getClass()).info("Check settings configuration file located at '" + f.getAbsolutePath() + "'.");
            String contents = FileUtil.getContents(f);
            FileAccessorDTO dto = new FileAccessorDTO();
            dto.versioned = FileEncoder.isVersioned(contents);
            FileEncoder.FileEncoderDTO encoderDTO = FileEncoder.decode(contents);
            dto.contents = encoderDTO.contents;
            dto.encoded = encoderDTO.encoded;
            if (dto.encoded) {
                String extraMessage = "\nThis backup file will work with Liquibase 4.5.0 users who specify it using the --checks-settings-file argument.";
                File backupFile = new File(f.getAbsolutePath() + ".v4.5");
                makeBackup(backupFile, contents, extraMessage);
            }
            return dto;
        }
    }

    /**
     *
     * Make a backup copy of the file
     *
     * @param backupFile             The file to back up
     * @param contents               The contents of the file
     * @param extraMessage           Extra message text to include
     *
     */
    public static void makeBackup(File backupFile, String contents, String extraMessage) {
        //
        // Make a backup copy of the file
        //
        String confFileBackupPath = backupFile.getAbsolutePath();
        try {
            FileUtil.write(contents, new File(confFileBackupPath));
            String message =
                "The file '" + backupFile.getAbsolutePath() + "' has been updated so it can be used by your current version of Liquibase, and to simplify resolving merge conflicts in Source Control. No action is required from you. Your original file was backed up as '" + confFileBackupPath + "'." +
                    (extraMessage != null ? extraMessage : "");
            Scope.getCurrentScope().getLog(ChecksFileAccessor.class).warning(message);
            Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
        } catch (IOException ioe) {
            String message = "Error creating backup file '" + confFileBackupPath + "' " + ioe.getMessage();
            Scope.getCurrentScope().getLog(ChecksFileAccessor.class).warning(message);
            Scope.getCurrentScope().getUI().sendMessage(message);
        }
    }

    @Override
    public void writeFileContents(String filename, String contents) throws CommandExecutionException, IOException {
        File f = new File(filename);
        if (!f.exists()) {
            Scope.getCurrentScope().getLog(getClass()).info("No configuration file named '" + filename + "' found.");

            f = createNewFile(filename);
            Scope.getCurrentScope().getLog(getClass()).info("Creating new file: " + f.getAbsolutePath());
        }
        Scope.getCurrentScope().getLog(getClass()).info("Check settings configuration file located at '" + f.getAbsolutePath() + "'.");
        FileUtil.write(FileEncoder.FILE_HEADER_LINUX + contents, f, false);
    }

    private File createNewFile(String filename) throws CommandExecutionException {
        File f = new File(filename);
        if (!StringUtil.isEmpty(f.getParent())) {
            f.getParentFile().mkdirs();
        }
        try {
            boolean created = f.createNewFile();
            if (created) {
                Scope.getCurrentScope().getLog(getClass()).fine("Existing check settings configuration file not found, it is being created at '" + f.getAbsolutePath() + "'.");
            }
        } catch (IOException e) {
            throw new CommandExecutionException("Unable to create the file '" + filename + "'. Make sure the location is accessible and that files can be created there.", e);
        }
        return f;
    }


}
