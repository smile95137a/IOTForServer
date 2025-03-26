package com.frontend.res.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StoreRes {

    private Long id;                      // 商店 ID
    private String uid;                   // 商店唯一标识
    private String address;               // 商店地址
    private String name;                  // 商店名称
    private Long availablesCount;         // 可用池台数量
    private Long inusesCount;             // 已使用池台数量
    private String lat;                   // 纬度
    private String lon;                   // 经度
    private Integer deposit;              // 押金
    private String imgUrl;                // 图片 URL
    private List<StorePricingScheduleRes> pricingSchedules; // 定价时段信息
    private String hint;                  // 提示
    private String contactPhone;          // 联系电话

    // 这里可以扩展更多的业务逻辑，例如池台状态的计算等
}
