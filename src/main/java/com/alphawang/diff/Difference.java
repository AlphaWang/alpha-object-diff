package com.alphawang.diff;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(staticName = "of")
public class Difference {
    private final String path;
    private final DifferenceType type;
    private final String summary;
    
    private final Object leftValue;
    private final Object rightValue;
    
    public static Difference of(String path, DifferenceType type, Object leftValue, Object rightValue) {
        return of(path, type, null, leftValue, rightValue);
    }

    enum DifferenceType {
        VALUE_NOT_EQUALS,
        SIZE_NOT_SAME,
        TYPE_NOT_SAME,
        NULL_VS_NONNULL,
        ;
        
    }
}
