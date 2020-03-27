package liquibase.sdk;

import liquibase.command.LiquibaseCommand;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sdk.convert.ConvertCommand;
import liquibase.util.StringUtil;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class Main {

    private boolean debug;

    private CommandLine globalArguments;
    private String command;
    private List<String> commandArgs = new ArrayList<>();

    private Options globalOptions;

    public Main() {
        globalOptions = new Options();
    }

    public static void main(String[] args) {
        Main main = new Main();

        main.header("Liquibase SDK");

        try {
            main.init(args);

            if (main.command == null) {
                throw new UserError("No command passed");
            }

            if ("help".equals(main.command)) {
                main.printHelp();
                return;
            }

            LiquibaseCommand command;
            CommandLineParser commandParser = new GnuParser();
            if ("convert".equals(main.command)) {
                command = new ConvertCommand();

                Options options = new Options();
                options.addOption(OptionBuilder.hasArg().withDescription("Original changelog").isRequired().create("src"));
                options.addOption(OptionBuilder.hasArg().withDescription("Output changelog").isRequired().create("out"));
                options.addOption(OptionBuilder.hasArg().withDescription("Classpath").create("classpath"));

                CommandLine commandArguments = commandParser.parse(options, main.commandArgs.toArray(new String[main.commandArgs.size()]));
                ((ConvertCommand) command).setSrc(commandArguments.getOptionValue("src"));
                ((ConvertCommand) command).setOut(commandArguments.getOptionValue("out"));
                ((ConvertCommand) command).setClasspath(commandArguments.getOptionValue("classpath"));
            } else {
                throw new UserError("Unknown command: "+main.command);
            }

            command.execute();

            main.divider();
            main.out("Command executed successfully");


        } catch (UserError userError) {
            main.out("");
            main.header("ERROR EXECUTING COMMAND");
            main.out(userError.getMessage());
            main.out("");
            main.out("");
            return;
        } catch (Throwable exception) {
            System.out.println("Unexpected error: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    public void init(String[] args) throws UserError {
        CommandLineParser globalParser = new GnuParser();

        List<String> globalArgs = new ArrayList<>();

        boolean inGlobal = true;
        for (String arg : args) {
            if (inGlobal) {
                if (arg.startsWith("--")) {
                    globalArgs.add(arg);
                } else {
                    this.command = arg;
                    inGlobal = false;
                }
            } else {
                commandArgs.add(arg);
            }
        }

        try {
            this.globalArguments = globalParser.parse(globalOptions, globalArgs.toArray(new String[globalArgs.size()]));
        } catch (ParseException e) {
            throw new UserError("Error parsing global command line argument: " + e.getMessage());
        }
    }

//        Context context = Context.getInstance();
//        if (context.getSeenExtensionClasses().size() == 0) {
//            System.out.println("No extension classes found in "+StringUtil.join(context.getPackages(), ","));
//            return;
//        }
//
//        System.out.println("Extension classes found:");
//        for (Map.Entry<Class, Set<Class>> entry : context.getSeenExtensionClasses().entrySet()) {
//            System.out.println(StringUtil.indent(entry.getKey().getName()+" extensions:", 4));
//
//            System.out.println(StringUtil.indent(StringUtil.join(entry.getValue(), "\n", new StringUtil.StringUtilFormatter() {
//                @Override
//                public String toString(Object obj) {
//                    return ((Class) obj).getName();
//                }
//            }), 8));
//        }

//        header("Running Tests");
//
//        JUnitCore junit = new JUnitCore();
//        junit.addListener(new TextListener(System.out));
//        Result result = junit.run(new Computer(), StandardChangeTests.class);


    public File getSdkRoot() {
        File dir = new File(".").getAbsoluteFile();
        while (dir != null) {
            if (dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return "liquibase-sdk.bat".equals(name);
                }
            }).length > 0) {
                return dir;
            }

            dir = dir.getParentFile();
        }

        throw new UnexpectedLiquibaseException("Could not find Liquibase SDK home. Please run liquibase-sdk from the " +
            "liquibase/sdk directory or one of it's sub directories");
    }

    public String getCommand() {
        return command;
    }

    public void header(String... header) {
        divider();
        for (String line : header) {
            System.out.println(line);
        }
        divider();
    }

    public void divider() {
        System.out.println("---------------------------------------------------");
    }


    public void out(String message) {
        System.out.println(message);
    }

    public void debug(String message) {
        if (debug) {
            System.out.println("DEBUG: "+message);
        }
    }

    public void fatal(String error) throws UserError {
        throw new UserError(error);
    }

    public void fatal(Throwable exception) {
        fatal(exception.getMessage(), exception);
    }

    public void fatal(String error, Throwable exception) throws UserError {
        throw new UserError(error, exception);
    }

    public String getPath() {
        Map<String, String> environment = new ProcessBuilder().environment();

        String path = environment.get("Path");
        if (path == null) {
            path = environment.get("PATH");
        }
        if (path == null) {
            path = environment.get("path");
        }
        if (path == null) {
            throw new UnexpectedLiquibaseException("Cannot find path variable in environment. Possible variables are " + StringUtil.join(environment.keySet(), ","));
        }

        return path;
    }

    public String getPath(String... possibleFileNames) {
        Set<String> fileNames = new HashSet<>();

        for (String dir : getPath().split("[:;]")) {
            for (String fileName : possibleFileNames) {
                File file = new File(dir, fileName);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;

    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("liquibase-sdk [global options] [command] [command options]", globalOptions);
    }

    private static class UserError extends RuntimeException {
    
        private static final long serialVersionUID = 6926190469964122370L;
    
        public UserError(String message) {
            super(message);
        }

        UserError(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
