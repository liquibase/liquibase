package liquibase.util;

import liquibase.database.Database;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;

import java.util.*;

/**
 * Class to compute an ordered list based on dependencies between objects.
 * Used, for example, to return the order to create objects in a generateChangeLog operation.
 */
public class DependencyGraph {

    private final Map<Class<? extends DatabaseObject>, Node> allNodes = new HashMap<Class<? extends DatabaseObject>, Node>();

    private final List<Class<? extends DatabaseObject>> orderedTypes;

    /**
     * Creates an ordered list for the given {@link liquibase.diff.output.changelog.ChangeGenerator} and {@link liquibase.database.Database}
     */
    public DependencyGraph(Class<? extends ChangeGenerator> generatorType, Database database) {

        for (Class<? extends DatabaseObject> type : DatabaseObjectFactory.getInstance().getAllTypes()) {
            allNodes.put(type, new Node(type));
        }

        this.orderedTypes = this.sort(generatorType, database);

    }

    /**
     * Return the objects in this graph, optionally filtered to only the passed types.
     * If a null value is passed, all types are returned. If an empty collection is passed, an empty collection is returned.
     */
    public List<Class<? extends DatabaseObject>> getOrderedOutputTypes(Collection<Class<? extends DatabaseObject>> wantedTypes) {

        List<Class<? extends DatabaseObject>> types = new ArrayList<Class<? extends DatabaseObject>>(orderedTypes);

        ListIterator<Class<? extends DatabaseObject>> finalTypeIterator = types.listIterator();
        if (wantedTypes != null) {
            while (finalTypeIterator.hasNext()) {
                if (!wantedTypes.contains(finalTypeIterator.next())) {
                    finalTypeIterator.remove();
                }
            }
        }
        return types;
    }


    private List<Class<? extends DatabaseObject>> sort(Class<? extends ChangeGenerator> generatorType, Database database) {
        ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();
        for (Class<? extends DatabaseObject> type : allNodes.keySet()) {
            for (Class<? extends DatabaseObject> afterType : changeGeneratorFactory.runBeforeTypes(type, database, generatorType)) {
                getNode(type).addEdge(getNode(afterType));
            }

            for (Class<? extends DatabaseObject> beforeType : changeGeneratorFactory.runAfterTypes(type, database, generatorType)) {
                getNode(beforeType).addEdge(getNode(type));
            }
        }


        ArrayList<Node> returnNodes = new ArrayList<Node>();

        SortedSet<Node> nodesWithNoIncomingEdges = new TreeSet<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.type.getName().compareTo(o2.type.getName());
            }
        });
        for (Node n : allNodes.values()) {
            if (n.inEdges.size() == 0) {
                nodesWithNoIncomingEdges.add(n);
            }
        }

        while (!nodesWithNoIncomingEdges.isEmpty()) {
            Node node = nodesWithNoIncomingEdges.iterator().next();
            nodesWithNoIncomingEdges.remove(node);

            returnNodes.add(node);

            for (Iterator<Edge> it = node.outEdges.iterator(); it.hasNext(); ) {
                //remove edge e from the graph
                Edge edge = it.next();
                Node nodePointedTo = edge.to;
                it.remove();//Remove edge from node
                nodePointedTo.inEdges.remove(edge);//Remove edge from nodePointedTo

                //if nodePointedTo has no other incoming edges then insert nodePointedTo into nodesWithNoIncomingEdges
                if (nodePointedTo.inEdges.isEmpty()) {
                    nodesWithNoIncomingEdges.add(nodePointedTo);
                }
            }
        }
        //Check to see if all edges are removed
        for (Node n : allNodes.values()) {
            if (!n.inEdges.isEmpty()) {
                String message = "Could not resolve " + generatorType.getSimpleName() + " dependencies due to dependency cycle. Dependencies: \n";
                for (Node node : allNodes.values()) {
                    SortedSet<String> fromTypes = new TreeSet<String>();
                    SortedSet<String> toTypes = new TreeSet<String>();
                    for (Edge edge : node.inEdges) {
                        fromTypes.add(edge.from.type.getSimpleName());
                    }
                    for (Edge edge : node.outEdges) {
                        toTypes.add(edge.to.type.getSimpleName());
                    }
                    String from = StringUtils.join(fromTypes, ",");
                    String to = StringUtils.join(toTypes, ",");
                    message += "    ["+ from +"] -> "+ node.type.getSimpleName()+" -> [" + to +"]\n";
                }

                throw new UnexpectedLiquibaseException(message);
            }
        }
        List<Class<? extends DatabaseObject>> returnList = new ArrayList<Class<? extends DatabaseObject>>();
        for (Node node : returnNodes) {
            returnList.add(node.type);
        }
        return returnList;
    }


    private Node getNode(Class<? extends DatabaseObject> type) {
        Node node = allNodes.get(type);
        if (node == null) {
            node = new Node(type);
        }
        return node;
    }


    static class Node {
        public final Class<? extends DatabaseObject> type;
        public final HashSet<Edge> inEdges;
        public final HashSet<Edge> outEdges;

        public Node(Class<? extends DatabaseObject> type) {
            this.type = type;
            inEdges = new HashSet<Edge>();
            outEdges = new HashSet<Edge>();
        }

        public Node addEdge(Node node) {
            Edge e = new Edge(this, node);
            outEdges.add(e);
            node.inEdges.add(e);
            return this;
        }

        @Override
        public String toString() {
            return type.getName();
        }
    }

    static class Edge {
        public final Node from;
        public final Node to;

        public Edge(Node from, Node to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Edge)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            Edge e = (Edge) obj;
            return e.from == from && e.to == to;
        }

        @Override
        public int hashCode() {
            return (this.from.toString()+"."+this.to.toString()).hashCode();
        }
    }
}
