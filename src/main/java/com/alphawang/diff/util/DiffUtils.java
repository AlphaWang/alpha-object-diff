package com.alphawang.diff.util;

import java.util.Collection;

public class DiffUtils {
    
    public static boolean isEmpty(Collection collection) {
         return collection == null || collection.size() == 0;
    }
}
