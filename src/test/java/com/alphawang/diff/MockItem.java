package com.alphawang.diff;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
public class MockItem {

    private Long itemId;
    private String name;
    private LocalDate createdAt;
    private Date modifiedAt;
}
