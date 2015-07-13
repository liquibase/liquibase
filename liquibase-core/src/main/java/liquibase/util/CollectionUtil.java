package liquibase.util;

import java.util.*;

public class CollectionUtil {

    public static <T> Set<Set<T>> powerSet(Collection<T> originalSet) {
        Set<Set<T>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
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

    public static <T> List<List<T>> permutations(List<List<T>> parameterValues) {
        List<List<T>> list = new ArrayList<>();
        if (parameterValues == null || parameterValues.size() == 0) {
            return list;
        }

        int i=0;
        LinkedHashMap<String, List<T>> map = new LinkedHashMap<>();
        for (Collection<T> value : parameterValues) {
            map.put(String.valueOf(i++), new ArrayList<T>(value));
        }

        for (Map<String, T> permutation : permutations(map)) {
            List<T> val = new ArrayList<>();
            for (String key : permutation.keySet()) {
                val.add(permutation.get(key));
            }
            list.add(val);
        }



        return list;
    }

    public static <T> List<Map<String, T>> permutations(Map<String, List<T>> parameterValues) {
        List<Map<String, T>> list = new ArrayList<>();
        if (parameterValues == null || parameterValues.size() == 0) {
            return list;
        }

        permute(new LinkedHashMap<String, T>(), new ArrayList<>(parameterValues.keySet()), parameterValues, list);

        return list;
    }

    private static <T> void permute(Map<String, T> basePermutation, List<String> remainingKeys, Map<String, List<T>> parameterValues, List<Map<String, T>> returnList) {

        String thisKey = remainingKeys.get(0);
        remainingKeys = remainingKeys.subList(1, remainingKeys.size());
        for (T value : parameterValues.get(thisKey)) {
            Map<String, T> permutation = new LinkedHashMap<>(basePermutation);

            permutation.put(thisKey, value);

            if (remainingKeys.size() == 0) {
                returnList.add(permutation);
            } else {
                permute(permutation, remainingKeys, parameterValues, returnList);
            }
        }
    }

    public static boolean hasValue(List value) {
        return value != null && value.size() > 0;
    }

    /**
     * Convenience method returns passed currentValue if it is not null and creates a new ArrayList if it is null.
     * <br><br>
     * Example: values = createIfNull(values)
     */
    public static <T> List<T> createIfNull(List<T> currentValue) {
        if (currentValue == null) {
            return new ArrayList<T>();
        } else {
            return currentValue;
        }
    }

    /**
     * Convenience method like {@link #createIfNull(List)} but also adds the given valueToAdd after the collection is ensured to exist.
     */
    public static <T> List<T> createIfNull(List<T> currentValue, T valueToAdd) {
        List<T> list = createIfNull(currentValue);
        list.add(valueToAdd);

        return list;
    }

    public static <T> T[] createIfNull(T[] arguments) {
        if (arguments == null) {
            return (T[]) new Object[0];
        } else {
            return arguments;
        }
    }

    public static <T> Set<T> createIfNull(Set<T> currentValue) {
        if (currentValue == null) {
            return new HashSet<>();
        } else {
            return currentValue;
        }
    }


    /**
     * Return a new list, filtered by the collectionFilter
     */
    public static <T> List<T> select(List<T> collection, CollectionFilter<T> collectionFilter) {
        List<T> newCollection = new ArrayList<>();

        for (T obj : collection) {
            if (collectionFilter.include(obj)) {
                newCollection.add(obj);
            }
        }

        return newCollection;
    }

    /**
     * Return a new set, filtered by the collectionFilter
     */
    public static <T> Set<T> select(Set<T> collection, CollectionFilter<T> collectionFilter) {
        Set<T> newCollection = new HashSet<>();

        for (T obj : collection) {
            if (collectionFilter.include(obj)) {
                newCollection.add(obj);
            }
        }

        return newCollection;
    }

    public interface CollectionFilter<T> {

        boolean include(T obj);
    }
}
