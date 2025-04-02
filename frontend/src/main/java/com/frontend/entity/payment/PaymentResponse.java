package com.frontend.entity.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "payment_response")  // 表名，可以根据实际情况调整
public class PaymentResponse {

    @Id
    @Column(name = "order_id", nullable = false, unique = true) // 主键
    private String orderId;    // 订单 ID

    @Column(name = "send_type")
    private String sendType;   // 传送型态

    @Column(name = "result")
    private String result;     // 结果

    @Column(name = "ret_msg")
    private String retMsg;     // 返回消息

    @Column(name = "amount")
    private String amount;     // 金额

    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    @Column(name = "order_no")
    private String orderNo;    // 订单号

    @Column(name = "number")
    private String number;     // 编号

    @Column(name = "outlay")
    private String outlay;     // 支出

    @Column(name = "check_string")
    private String checkString; // 检查字符串

    @Column(name = "bank_name")
    private String bankName;   // 银行名称

    @Column(name = "av_code")
    private String avCode;     // AV 代码

    @Column(name = "invoice_no")
    private String invoiceNo;  // 发票号

    @Column(name = "e_pay_account")
    private String ePayAccount; // 支付账户

    @Column(name = "user_id")
    private Long userId;
}
