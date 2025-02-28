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
     * @deprecated use {@link org.apache.commons.collections4.CollectionUtils} instead
     */
    @Deprecated
    public static <T> List<T> createIfNull(List<T> currentValue) {
        return ObjectUtil.defaultIfNull(currentValue, new ArrayList<>());
    }

    /**
     * Returns a new empty array if the passed array is null.
     * @deprecated use {@link org.apache.commons.collections4.CollectionUtils} instead
     */
    @Deprecated
    public static <T> T[] createIfNull(T[] arguments) {
        return ObjectUtil.defaultIfNull(arguments, (T[]) new Object[0]);
    }

    /**
     * Returns a new empty set if the passed set is null.
     * @deprecated use {@link org.apache.commons.collections4.CollectionUtils} instead
     */
    @Deprecated
    public static <T> Set<T> createIfNull(Set<T> currentValue) {
        return ObjectUtil.defaultIfNull(currentValue, new HashSet<>());
    }

    /**
     * Returns a new empty map if the passed map is null.
     * @deprecated use {@link org.apache.commons.collections4.CollectionUtils} instead
     */
    @Deprecated
    public static <T, E> Map<T, E> createIfNull(Map<T, E> currentValue) {
        return ObjectUtil.defaultIfNull(currentValue, new HashMap<>());
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

    /**
     * Find the actual key in a map, by searching the keys in the map and checking them ignoring case.
     * @param key the key to search for, in any case
     * @param map the map in which to search
     * @return the properly cased key, if found, or null if not found
     */
    public static String findKeyInMapIgnoreCase(String key, Map<String, Object> map) {
        for (Map.Entry<String, Object> mapEntry : map.entrySet()) {
            String actualKey = mapEntry.getKey();
            if (actualKey.equalsIgnoreCase(key)) {
                return actualKey;
            }
        }
        return null;
    }
}
