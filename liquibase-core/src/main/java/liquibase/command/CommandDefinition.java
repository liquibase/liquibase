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

    private String longDescription = "";
    private String shortDescription = "";

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
     * The long description of the command. Used in help docs.
     */
    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    void add(CommandStep step) {
        this.pipeline.add(step);
    }

    public void add(CommandArgumentDefinition commandArg) {
        this.arguments.put(commandArg.getName(), commandArg);
    }
}
