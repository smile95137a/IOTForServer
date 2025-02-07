package com.frontend.res.store;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class StoreRes {
    private Long storeId;
    private String uid;
    private String address;
    private String name;
    private Long availablesCount;
    private Long inusesCount;
    private Long lat;
    private Long lon;
}
