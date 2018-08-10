package com.alphawang.diff;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.alphawang.diff.Difference.DifferenceType.NULL_VS_NONNULL;
import static com.alphawang.diff.Difference.DifferenceType.SIZE_NOT_SAME;
import static com.alphawang.diff.Difference.DifferenceType.TYPE_NOT_SAME;
import static com.alphawang.diff.Difference.DifferenceType.VALUE_NOT_EQUALS;
import static com.alphawang.diff.util.ReflectionUtils.getCollectionItemClass;
import static com.alphawang.diff.util.ReflectionUtils.getFields;
import static com.alphawang.diff.util.ReflectionUtils.isSimpleClass;

@Slf4j
public class ObjectDiff {

    private static final String PATH_SEPARATOR = "/";

    private Map identities;
    private List<String> ignoreFields;
    private Map<Class, Function> collectionItemKeyFunction = new HashMap<>();
    private Object left;
    private Object right;
    
    private ObjectDiff() { }
    
    public static ObjectDiff newInstance() {
        return new ObjectDiff();
    }

    /**
     * Identities of current diff
     * @param identities
     * @return
     */
    public ObjectDiff withIdentities(Map identities) {
        this.identities = identities;
        return this;
    }

    /**
     * Regex for the paths need to be ignored.
     * @param ignoreFields
     * @return
     */
    public ObjectDiff withIgnorePaths(List<String> ignoreFields) {
        this.ignoreFields = ignoreFields;
        return this;
    }

    /**
     * This is for collection diff. 
     * By default, it will compare collection items index by index, so if the collection items are same but the order is not the same, 
     * it will be considered as different.
     * 
     * With collectionItemKeyFunction, you can specific a function to generate a key for collection items, then it will compare items 
     * by key instead of by index.
     * 
     * @param collectionItemKeyFunction
     * @return
     */
    public ObjectDiff addCollectionItemKeyFunction(Class clazz, Function collectionItemKeyFunction) {
        this.collectionItemKeyFunction.put(clazz, collectionItemKeyFunction);
        return this;
    }

    /**
     * Left Object
     * @param left
     * @return
     */
    public ObjectDiff withLeft(Object left) {
        this.left = left;
        return this;
    }

    /**
     * Right Object
     * @param right
     * @return
     */
    public ObjectDiff withRight(Object right) {
        this.right = right;
        return this;
    }

    /**
     * Generate diff result
     * @return
     */
    public DiffResult diff() {
        DiffResult diffResult = new DiffResult(identities, left, right);

        try {
            diff(diffResult, "", left, right);
        } catch (Exception e) {
            log.error("Diff Object ERROR.", e);
        }
        return diffResult;
    }
    
    
    private void diff(DiffResult diffResult, String path, Object left, Object right) {
        if (ignore(path, ignoreFields)) {
            return;
        }
        
        if (Objects.equals(left, right)) {
            return;
        }
        
        if (left == null && right != null) {
            diffResult.add(Difference.of(path, NULL_VS_NONNULL, null, right));
            return;
        }
        if (left != null && right == null) {
            diffResult.add(Difference.of(path, NULL_VS_NONNULL, left, null));
            return;
        }

        if (left instanceof Map) {
            diffMap(diffResult, path, left, right);
        } else if (left instanceof Collection) {
            diffCollection(diffResult, path, left, right);
        } else if (left.getClass().isArray()) {
            diffArray(diffResult, path, left, right);
        } else if (isSimpleClass(left)) {
            diffSimpleClass(diffResult, path, left, right);
        } else if (left instanceof Comparable) {
            diffComparable(diffResult, path, left, right);
        } else {
            diffObject(diffResult, path, left, right);
        }
        
    }
    
    private boolean ignore(String path, List<String> ignorePaths) {
        if (Strings.isNullOrEmpty(path) || ignorePaths == null || ignorePaths.isEmpty()) {
            return false;
        }
        
        for (String regex : ignorePaths) {
            if (path.matches(regex)) {
                 return true;
            }
        }
        return false;
    }

    private void diffSimpleClass(DiffResult diffResult, String path, Object left, Object right) {
        if (!left.equals(right)) {
            diffResult.add(Difference.of(path, VALUE_NOT_EQUALS, left, right));
        }
    }

    private void diffComparable(DiffResult diffResult, String path, Object left, Object right) {
        Comparable leftComparable = (Comparable) left;
        if (leftComparable.compareTo(right) != 0) {
            diffResult.add(Difference.of(path, VALUE_NOT_EQUALS, left, right));
        }
    }

    private void diffObject(DiffResult diffResult, String path, Object left, Object right) {
        Map<String, Field> leftFields = getFields(left.getClass());
        Map<String, Field> rightFields = getFields(right.getClass());
        
        for (Map.Entry<String, Field> entry : leftFields.entrySet()) {
            String fieldName = entry.getKey();
            Field leftField = entry.getValue();
            Field rightField = rightFields.get(fieldName);
            if (rightField == null) {
                log.debug("No field {} in {}", fieldName, right);
                continue;
            }
            
            leftField.setAccessible(true);
            rightField.setAccessible(true);
            try {
                Object leftValue = leftField.get(left);
                Object rightValue = rightField.get(right);
                
                String fieldPath = path + PATH_SEPARATOR + fieldName;
                diff(diffResult, fieldPath, leftValue, rightValue);
            } catch (IllegalAccessException e) {
                log.debug("cannot access {}#{}", left.getClass().getSimpleName(), fieldName);
                continue;
            }
        }
        
    }

    private void diffMap(DiffResult diffResult, String path, Object leftObj, Object rightObj) {
        if (!(leftObj instanceof Map && rightObj instanceof Map)) {
            log.debug("Class not match for {}, left = {}, right = {}", path, leftObj, rightObj);
            diffResult.add(Difference.of(path, TYPE_NOT_SAME, leftObj, rightObj));
        }

        Map left = (Map) leftObj;
        Map right = (Map) rightObj; 
        
        if (left != null && !left.isEmpty() && right != null && !right.isEmpty()) {
            if (left.size() == right.size()) {
                for (Object key : left.keySet()) {
                    String mapEntryPath = path + PATH_SEPARATOR + key;
                    Object leftValue = left.get(key);
                    Object rightValue = right.get(key);
                    
                    diff(diffResult, mapEntryPath, leftValue, rightValue);
                }
            } else {
                diffResult.add(Difference.of(path, SIZE_NOT_SAME, left.size() + " : " + right.size(), left, right));
            }
        }
    }

    private void diffCollection(DiffResult diffResult, String path, Object leftObj, Object rightObj) {
        if (!(leftObj instanceof Collection && rightObj instanceof Collection)) {
            log.debug("Class not match for {}, left = {}, right = {}", path, leftObj, rightObj);
            diffResult.add(Difference.of(path, TYPE_NOT_SAME, leftObj, rightObj));
        }

        Collection left = (Collection) leftObj;
        Collection right = (Collection) rightObj;
        
        if (!left.isEmpty() && !right.isEmpty()) {
            if (left.size() != right.size()) {
                diffResult.add(Difference.of(path, SIZE_NOT_SAME, left.size() + " : " + right.size(), left, right));
                return;
            } 
            
            Class itemClass = getCollectionItemClass(left);
            Function keyFunction = getCollectionItemKeyFunction(itemClass);
            if (keyFunction != null) {
                try {
                    Map leftMap = Maps.uniqueIndex(left, keyFunction);
                    Map rightMap = Maps.uniqueIndex(right, keyFunction);
                    diffMap(diffResult, path, leftMap, rightMap);
                } catch (Exception e) {
                    log.warn("Failed to transform collection. left={}, right={}, function={}", left, right, keyFunction, e);
                    diffCollectionByIndex(diffResult, path, left, right);
                }
            } else {
                diffCollectionByIndex(diffResult, path, left, right);
            }
            
        } else {
            diffResult.add(Difference.of(path, NULL_VS_NONNULL, left, right));
        }
    }

    private void diffCollectionByIndex(DiffResult diffResult, String path, Collection left, Collection right) {
        Iterator leftIter = left.iterator();
        Iterator rightIter = right.iterator();
        for (int i = 0; i < left.size(); i++) {
            String collectionItemPath = path + PATH_SEPARATOR + i;
            diff(diffResult, collectionItemPath, leftIter.next(), rightIter.next());
        }
    }

    private void diffArray(DiffResult diffResult, String path, Object leftObj, Object rightObj) {
        int leftLength = Array.getLength(leftObj);
        int rightLength = Array.getLength(rightObj);
        
        if (leftLength != rightLength) {
            diffResult.add(Difference.of(path, SIZE_NOT_SAME, leftLength + " : " + rightLength, leftObj, rightObj));
            return;
        }
        
        for (int i = 0; i < leftLength; i++) {
            String arrayPath = path + PATH_SEPARATOR + i;
            diff(diffResult, arrayPath, Array.get(leftObj, i), Array.get(rightObj, i));
        }
    }
    
    private Function getCollectionItemKeyFunction(Class clazz) {
        if (collectionItemKeyFunction == null || collectionItemKeyFunction.size() <= 0) {
            return null;
        }

        while (clazz != null && clazz != Object.class) {
            Function function = collectionItemKeyFunction.get(clazz);
            if (function != null) {
                return function;
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

}
