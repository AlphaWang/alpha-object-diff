package com.alphawang.diff;

import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@ToString
public class DiffResult {
    
    private final Map identities;
    private final Object left;
    private final Object right;
    private final Map<String, Difference> differences = new LinkedHashMap<>();
    
    public DiffResult(Map identities, Object left, Object right) {
        this.identities = identities;
        this.left = left;
        this.right = right;
    }

    public boolean hasDifference() {
        return differences.size() > 0;
    }
    
    public void add(Difference difference) {
        if (difference == null) {
            return;
        }
        differences.put(difference.getPath(), difference);
    }
}
