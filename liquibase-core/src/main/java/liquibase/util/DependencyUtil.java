package liquibase.util;

import java.util.*;

public class DependencyUtil {


    public static class DependencyGraph<T> {

        private HashMap<T, GraphNode<T>> nodes = new HashMap<T, GraphNode<T>>();
        private NodeValueListener<T> listener;
        private List<GraphNode<T>> evaluatedNodes = new ArrayList<GraphNode<T>>();


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
            GraphNode<T> node = new GraphNode<T>();
            node.value = value;
            return node;
        }

        public void computeDependencies() {
            List<GraphNode<T>> orphanNodes = getOrphanNodes();
            List<GraphNode<T>> nextNodesToDisplay = new ArrayList<GraphNode<T>>();
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
                                nextNodesToDisplay = new ArrayList<GraphNode<T>>();
                            // add these too, so they get a chance to be displayed
                            // as well
                            nextNodesToDisplay.addAll(goingOutNodes);
                        }
                    } else {
                        if (nextNodesToDisplay == null)
                            nextNodesToDisplay = new ArrayList<GraphNode<T>>();
                        // the checked node should be carried
                        nextNodesToDisplay.add(node);
                    }
                }
            }
            if (nextNodesToDisplay != null) {
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
                        orphanNodes = new ArrayList<GraphNode<T>>();
                    orphanNodes.add(node);
                }
            }
            return orphanNodes;
        }
    }

    static private class GraphNode<T> {
        public T value;
        private List<GraphNode<T>> comingInNodes;
        private List<GraphNode<T>> goingOutNodes;

        public void addComingInNode(GraphNode<T> node) {
            if (comingInNodes == null)
                comingInNodes = new ArrayList<GraphNode<T>>();
            comingInNodes.add(node);
        }

        public void addGoingOutNode(GraphNode<T> node) {
            if (goingOutNodes == null)
                goingOutNodes = new ArrayList<GraphNode<T>>();
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