package com.alphawang.diff;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MockItems {
    private String memberSrl;
    private Long id;
    
    private List<MockItem> itemList;
    private Map<Long, MockItem> itemMap;
    private MockItem[] itemArray;
    private int[] intArray;
}
