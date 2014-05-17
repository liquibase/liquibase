package liquibase.sdk;

import liquibase.command.LiquibaseCommand;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import liquibase.sdk.convert.ConvertCommand;
import liquibase.sdk.vagrant.VagrantCommand;
import liquibase.sdk.watch.WatchCommand;
import liquibase.util.StringUtils;
import org.apache.commons.cli.*;
import org.eclipse.jetty.util.log.StdErrLog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class Main {

    private boolean debug = false;

    private CommandLine globalArguments;
    private String command;
    private List<String> commandArgs = new ArrayList<String>();

    private Options globalOptions;

    public static void main(String[] args) {
        Main main = new Main();

        main.header("Liquibase SDK");

        try {
            main.init(args);

            if (main.command == null) {
                throw new UserError("No command passed");
            }

            if (main.command.equals("help")) {
                main.printHelp();
                return;
            }

            LiquibaseCommand command;
            CommandLineParser commandParser = new GnuParser();
            if (main.command.equals("vagrant")) {
                command = new VagrantCommand(main);
                try {
                    CommandLine commandArguments = commandParser.parse(((VagrantCommand) command).getOptions(), main.commandArgs.toArray(new String[main.commandArgs.size()]));
                    ((VagrantCommand) command).setup(commandArguments);
                } catch (ParseException e) {
                    throw new UserError("Error parsing command arguments: "+e.getMessage());
                }
            } else if (main.command.equals("watch")) {
                ((StdErrLog) org.eclipse.jetty.util.log.Log.getRootLogger()).setLevel(StdErrLog.LEVEL_WARN);
                LogFactory.getInstance().setDefaultLoggingLevel(LogLevel.WARNING);
                command = new WatchCommand(main);

                Options options = new Options();
                options.addOption(OptionBuilder.hasArg().withDescription("Webserver port. Default 8080").create("port"));
                options.addOption(OptionBuilder.hasArg().withDescription("Database URL").isRequired().create("url"));
                options.addOption(OptionBuilder.hasArg().withDescription("Database username").isRequired().create("username"));
                options.addOption(OptionBuilder.hasArg().withDescription("Database password").isRequired().create("password"));

                CommandLine commandArguments = commandParser.parse(options, main.commandArgs.toArray(new String[main.commandArgs.size()]));
                ((WatchCommand) command).setUrl(commandArguments.getOptionValue("url"));
                ((WatchCommand) command).setUsername(commandArguments.getOptionValue("username"));
                ((WatchCommand) command).setPassword(commandArguments.getOptionValue("password"));
                if (commandArguments.hasOption("port")) {
                    ((WatchCommand) command).setPort(Integer.valueOf(commandArguments.getOptionValue("port")));
                }
            } else if (main.command.equals("convert")) {
                command = new ConvertCommand(main);

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

    public Main() {
        globalOptions = new Options();
    }

    public void init(String[] args) throws UserError {
        Context.reset();
        CommandLineParser globalParser = new GnuParser();

        List<String> globalArgs = new ArrayList<String>();

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
//            System.out.println("No extension classes found in "+StringUtils.join(context.getPackages(), ","));
//            return;
//        }
//
//        System.out.println("Extension classes found:");
//        for (Map.Entry<Class, Set<Class>> entry : context.getSeenExtensionClasses().entrySet()) {
//            System.out.println(StringUtils.indent(entry.getKey().getName()+" extensions:", 4));
//
//            System.out.println(StringUtils.indent(StringUtils.join(entry.getValue(), "\n", new StringUtils.StringUtilsFormatter() {
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
                    return name.equals("liquibase-sdk.bat");
                }
            }).length > 0) {
                return dir;
            }

            dir = dir.getParentFile();
        }

        throw new UnexpectedLiquibaseException("Could not find Liquibase SDK home. Please run liquibase-sdk from the liquibase/sdk directory or one of it's sub directories");
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
            throw new UnexpectedLiquibaseException("Cannot find path variable in environment. Possible variables are " + StringUtils.join(environment.keySet(), ","));
        }

        return path;
    }

    public String getPath(String... possibleFileNames) {
        Set<String> fileNames = new HashSet<String>();

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

        public UserError(String message) {
            super(message);
        }

        UserError(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
