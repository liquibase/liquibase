package liquibase.command;

import liquibase.util.StringUtil;

import java.util.*;

/**
 * Metadata about a particular command. Look up instances with {@link CommandFactory#getCommandDefinition(String...)}
 */
public class CommandDefinition implements Comparable<CommandDefinition> {

    private final String[] name;
    private final String concatName;

    /**
     * Stored as a SortedSet even though exposed as a list for easier internal management
     */
    private final SortedSet<CommandStep> pipeline;

    private final SortedMap<String, CommandArgumentDefinition<?>> arguments = new TreeMap<>();

    private String longDescription = null;
    private String shortDescription = null;
    private boolean internal;
    private boolean hidden;
    private String footer;

    private Map<String, String> groupLongDescription = new HashMap<>();
    private Map<String, String> groupShortDescription = new HashMap<>();

    protected CommandDefinition(String[] name) {
        this.name = name;
        this.concatName = StringUtil.join(Arrays.asList(name), " ");

        pipeline = new TreeSet<>((o1, o2) -> {
            final int order = Integer.compare(o1.getOrder(this), o2.getOrder(this));
            if (order == 0) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }

            return order;
        });

    }

    /**
     * The fully qualified name of this command.
     */
    public String[] getName() {
        return name;
    }

    @Override
    public int compareTo(CommandDefinition o) {
        return this.concatName.compareTo(o.concatName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return this.concatName.equals(((CommandDefinition) o).concatName);
    }

    @Override
    public int hashCode() {
        return concatName.hashCode();
    }

    /**
     * Returns the steps in the command's pipeline
     */
    public List<CommandStep> getPipeline() {
        return Collections.unmodifiableList(new ArrayList<>(pipeline));
    }


    /**
     * Returns the given argument definition for this command. Handles argument name smoothing.
     * Returns null if no argument matches.
     */
    public CommandArgumentDefinition<?> getArgument(String argName) {
        argName = StringUtil.toCamelCase(argName);

        for (CommandArgumentDefinition<?> argumentDefinition : getArguments().values()) {
            if (argumentDefinition.getName().equalsIgnoreCase(argName)) {
                return argumentDefinition;
            }
        }

        return null;
    }

    /**
     * Returns the arguments for this command.
     */
    public SortedMap<String, CommandArgumentDefinition<?>> getArguments() {
        return Collections.unmodifiableSortedMap(this.arguments);
    }

    /**
     * Convenience method to return the  arguments for this command which are of a given type.
     */
    public <T> SortedSet<CommandArgumentDefinition<T>> getArguments(Class<T> argumentType) {
        SortedSet<CommandArgumentDefinition<T>> returnSet = new TreeSet<>();

        for (CommandArgumentDefinition<?> definition : arguments.values()) {
            if (definition.getDataType().isAssignableFrom(argumentType)) {
                returnSet.add((CommandArgumentDefinition<T>) definition);
            }
        }

        return returnSet;
    }


    /**
     * The short description of the command. Used in help docs.
     */
    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * The short description of the given command group. Used in help docs.
     */
    public String getGroupShortDescription(String[] group) {
        return groupShortDescription.get(StringUtil.join(group, " "));
    }

    /**
     * Sets the short description of the given command group for help docs.
     * If multiple command commands set different descriptions for the group, which version will be shown in any rolled up help is undefined.
     */
    public void setGroupShortDescription(String[] group, String shortDescription) {
        this.groupShortDescription.put(StringUtil.join(group, " "), shortDescription);
    }


    /**
     * The long description of the given command group. Used in help docs.
     * If multiple command commands set different descriptions for the group, which version will be shown in any rolled up help is undefined.
     */
    public String getGroupLongDescription(String[] group) {
        return groupLongDescription.get(StringUtil.join(group, " "));
    }

    public void setGroupLongDescription(String[] group, String longDescription) {
        this.groupLongDescription.put(StringUtil.join(group, " "), longDescription);
    }


    /**
     * The long description of the command. Used in help docs.
     */
    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getHelpFooter() { return footer; }

    public void setHelpFooter(String footer) { this.footer = footer; }

    void add(CommandStep step) {
        this.pipeline.add(step);
    }

    public void add(CommandArgumentDefinition commandArg) {
        this.arguments.put(commandArg.getName(), commandArg);
    }

    /**
     * Internal commands are ones that can be called programmatically, but should not be exposed directly and automatically through integrations.
     */
    public boolean getInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    /**
     * Hidden commands are ones that can be called via integrations, but should not be normally shown in help to users.
     * "Alias" or legacy commands are often marked as hidden commands.
     */
    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Return true if this command represents the given command
     */
    public boolean is(String... commandName) {
        return Arrays.equals(getName(), commandName);
    }

}
