package liquibase.command.core;

import java.util.Set;

/**
 * Registry of Liquibase Pro commands and subcommands for improved error messaging.
 * This registry helps identify when Community users attempt to run Pro-only commands
 * so we can provide clear messaging about license requirements.
 */
public class ProCommandsRegistry {

    /**
     * Set of primary Pro commands that require a Liquibase license.
     * These are top-level commands like 'flow', 'checks', etc.
     */
    public static final Set<String> PRO_COMMANDS = Set.of(
        "flow",
        "checks",
        "rollbackonechangeset",
        "rollbackoneupdate",
        "updateonechangeset",
        "dbclhistory",
        "databasechangeloghistory",
        "connect",
        "driftdetect",
        "driftreport",
        "snapshotreference"
    );

    /**
     * Set of Pro subcommands that work with certain primary commands.
     * For example: 'checks run', 'checks show', 'flow validate', etc.
     */
    public static final Set<String> PRO_SUBCOMMANDS = Set.of(
        "run",
        "show",
        "enable",
        "disable",
        "validate"
    );

    /**
     * Checks if a given command is a Pro command.
     * @param command the command name to check (case insensitive)
     * @return true if the command requires a Pro license
     */
    public static boolean isProCommand(String command) {
        if (command == null) {
            return false;
        }
        return PRO_COMMANDS.contains(command.toLowerCase());
    }

    /**
     * Checks if a given command and subcommand combination requires Pro license.
     * @param command the main command (e.g., "checks")
     * @param subcommand the subcommand (e.g., "run")
     * @return true if the command+subcommand combination requires a Pro license
     */
    public static boolean isProSubcommand(String command, String subcommand) {
        if (command == null || subcommand == null) {
            return false;
        }
        return isProCommand(command) && PRO_SUBCOMMANDS.contains(subcommand.toLowerCase());
    }

    /**
     * Gets a formatted command string for error messages.
     * @param command the main command
     * @param subcommand the subcommand (optional, can be null)
     * @return formatted command string for display
     */
    public static String getFormattedCommand(String command, String subcommand) {
        if (subcommand != null && !subcommand.isEmpty()) {
            return command + " " + subcommand;
        }
        return command;
    }
}