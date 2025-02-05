package com.frontend.res.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class VendorRes {
    private Long storeId;
    private String uid;
    private String address;
    private String name;
    private Long availablesCount;
}
