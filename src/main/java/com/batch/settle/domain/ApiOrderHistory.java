package com.batch.settle.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiOrderHistory {
    public String id;
    public Long userId;
    private String url;
    private Integer fee;
    private State state;
    private String createdAt;

    public enum State {
        SUCCESS,
        FAIL;
    }
}
