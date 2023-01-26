package liquibase.util;

import java.util.*;

public class CollectionUtil {

    public static <T> Set<Set<T>> powerSet(Collection<T> originalSet) {
        Set<Set<T>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<>());
            return sets;
        }
        List<T> list = new ArrayList<>(originalSet);
        T head = list.get(0);
        Collection<T> rest = list.subList(1, list.size());
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public static <T> List<Map<String, T>> permutations(Map<String, List<T>> parameterValues) {
        List<Map<String, T>> list = new ArrayList<>();
        if ((parameterValues == null) || parameterValues.isEmpty()) {
            return list;
        }

        permute(new HashMap<>(), new ArrayList<>(parameterValues.keySet()), parameterValues, list);

        return list;


    }

    private static <T> void permute(Map<String, T> basePermutation, List<String> remainingKeys, Map<String, List<T>> parameterValues, List<Map<String, T>> returnList) {

        String thisKey = remainingKeys.get(0);
        remainingKeys = remainingKeys.subList(1, remainingKeys.size());
        for (T value : parameterValues.get(thisKey)) {
            Map<String, T> permutation = new HashMap<>(basePermutation);

            permutation.put(thisKey, value);

            if (remainingKeys.isEmpty()) {
                returnList.add(permutation);
            } else {
                permute(permutation, remainingKeys, parameterValues, returnList);
            }
        }
    }

    /**
     * Returns passed currentValue if it is not null and creates a new ArrayList if it is null.
     * <br><br>
     * Example: values = createIfNull(values)
     */
    public static <T> List<T> createIfNull(List<T> currentValue) {
        if (currentValue == null) {
            return new ArrayList<>();
        } else {
            return currentValue;
        }
    }

    /**
     * Returns a new empty array if the passed array is null.
     */
    public static <T> T[] createIfNull(T[] arguments) {
        if (arguments == null) {
            return (T[]) new Object[0];
        } else {
            return arguments;
        }
    }

    /**
     * Returns a new empty set if the passed set is null.
     */
    public static <T> Set<T> createIfNull(Set<T> currentValue) {
        if (currentValue == null) {
            return new HashSet<>();
        } else {
            return currentValue;
        }
    }

    /**
     * Returns a new empty map if the passed map is null.
     */
    public static <T, E> Map<T, E> createIfNull(Map<T, E> currentValue) {
        if (currentValue == null) {
            return new HashMap<>();
        } else {
            return currentValue;
        }
    }

    /**
     * Converts a set of nested maps (like from yaml/json) into a flat map with dot-separated properties
     */
    public static Map<String, Object> flatten(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return flatten(null, map);

    }

    private static Map<String, Object> flatten(String prefix, Map<String, Object> map) {
        Map<String, Object> outMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String propertyName = entry.getKey();
            if (prefix != null) {
                propertyName = prefix + "." + propertyName;
            }

            if (entry.getValue() instanceof Map) {
                outMap.putAll(flatten(propertyName, (Map<String, Object>) entry.getValue()));
            } else {
                outMap.put(propertyName, entry.getValue());
            }
        }

        return outMap;
    }

}
