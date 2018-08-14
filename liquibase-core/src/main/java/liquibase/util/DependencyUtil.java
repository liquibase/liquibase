package liquibase.util;

import liquibase.logging.LogService;
import liquibase.logging.LogType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DependencyUtil {


    public static class DependencyGraph<T> {

        private HashMap<T, GraphNode<T>> nodes = new HashMap<>();
        private NodeValueListener<T> listener;
        private List<GraphNode<T>> evaluatedNodes = new ArrayList<>();

        private int recursiveSizeCheck = -1;

        public DependencyGraph(NodeValueListener<T> listener) {
            this.listener = listener;
        }

        public void add(T evalFirstValue, T evalAfterValue) {
            GraphNode<T> firstNode = null;
            GraphNode<T> afterNode = null;
            if (nodes.containsKey(evalFirstValue)) {
                firstNode = nodes.get(evalFirstValue);
            } else {
                firstNode = createNode(evalFirstValue);
                nodes.put(evalFirstValue, firstNode);
            }
            if (nodes.containsKey(evalAfterValue)) {
                afterNode = nodes.get(evalAfterValue);
            } else {
                afterNode = createNode(evalAfterValue);
                nodes.put(evalAfterValue, afterNode);
            }
            firstNode.addGoingOutNode(afterNode);
            afterNode.addComingInNode(firstNode);
        }

        private GraphNode<T> createNode(T value) {
            GraphNode<T> node = new GraphNode<>();
            node.value = value;
            return node;
        }

        public void computeDependencies() {
            List<GraphNode<T>> orphanNodes = getOrphanNodes();
            List<GraphNode<T>> nextNodesToDisplay = new ArrayList<>();
            if (orphanNodes != null) {
                for (GraphNode<T> node : orphanNodes) {
                    listener.evaluating(node.value);
                    evaluatedNodes.add(node);
                    nextNodesToDisplay.addAll(node.getGoingOutNodes());
                }
                computeDependencies(nextNodesToDisplay);
            }
        }

        private void computeDependencies(List<GraphNode<T>> nodes) {
            List<GraphNode<T>> nextNodesToDisplay = null;
            for (GraphNode<T> node : nodes) {
                if (!isAlreadyEvaluated(node)) {
                    List<GraphNode<T>> comingInNodes = node.getComingInNodes();
                    if (areAlreadyEvaluated(comingInNodes)) {
                        listener.evaluating(node.value);
                        evaluatedNodes.add(node);
                        List<GraphNode<T>> goingOutNodes = node.getGoingOutNodes();
                        if (goingOutNodes != null) {
                            if (nextNodesToDisplay == null)
                                nextNodesToDisplay = new ArrayList<>();
                            // add these too, so they get a chance to be displayed
                            // as well
                            nextNodesToDisplay.addAll(goingOutNodes);
                        }
                    } else {
                        if (nextNodesToDisplay == null)
                            nextNodesToDisplay = new ArrayList<>();
                        // the checked node should be carried
                        nextNodesToDisplay.add(node);
                    }
                }
            }
            if ((nextNodesToDisplay != null) && !nextNodesToDisplay.isEmpty()) {
                if (nextNodesToDisplay.size() == recursiveSizeCheck) {
                    //Recursion is not making progress, heading to a stack overflow exception.
                    //Probably some cycles in there somewhere, so pull out a node and re-try
                    GraphNode nodeToRemove = null;
                    int nodeToRemoveLinks = Integer.MAX_VALUE;
                    for (GraphNode node : nextNodesToDisplay) {
                        List links = node.getComingInNodes();
                        if ((links != null) && (links.size() < nodeToRemoveLinks)) {
                            nodeToRemove = node;
                            nodeToRemoveLinks = links.size();
                        }
                    }
                    LogService.getLog(getClass()).debug(LogType.LOG, "Potential StackOverflowException. Pro-actively removing "+nodeToRemove.value+" with "+nodeToRemoveLinks+" incoming nodes");
                    nextNodesToDisplay.remove(nodeToRemove);
                }

                recursiveSizeCheck = nextNodesToDisplay.size();
                computeDependencies(nextNodesToDisplay);
            }
            // here the recursive call ends
        }

        private boolean isAlreadyEvaluated(GraphNode<T> node) {
            return evaluatedNodes.contains(node);
        }

        private boolean areAlreadyEvaluated(List<GraphNode<T>> nodes) {
            return evaluatedNodes.containsAll(nodes);
        }

        private List<GraphNode<T>> getOrphanNodes() {
            List<GraphNode<T>> orphanNodes = null;
            Set<T> keys = nodes.keySet();
            for (T key : keys) {
                GraphNode<T> node = nodes.get(key);
                if (node.getComingInNodes() == null) {
                    if (orphanNodes == null)
                        orphanNodes = new ArrayList<>();
                    orphanNodes.add(node);
                }
            }
            return orphanNodes;
        }
    }

    private static class GraphNode<T> {
        public T value;
        private List<GraphNode<T>> comingInNodes;
        private List<GraphNode<T>> goingOutNodes;

        public void addComingInNode(GraphNode<T> node) {
            if (comingInNodes == null)
                comingInNodes = new ArrayList<>();
            comingInNodes.add(node);
        }

        public void addGoingOutNode(GraphNode<T> node) {
            if (goingOutNodes == null)
                goingOutNodes = new ArrayList<>();
            goingOutNodes.add(node);
        }

        public List<GraphNode<T>> getComingInNodes() {
            return comingInNodes;
        }

        public List<GraphNode<T>> getGoingOutNodes() {
            return goingOutNodes;
        }
    }


    public interface NodeValueListener<T> {
        void evaluating(T nodeValue);
    }


}