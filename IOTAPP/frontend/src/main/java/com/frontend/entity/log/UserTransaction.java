package src.main.java.com.frontend.entity.log;

import src.main.java.com.frontend.enums.PayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_transaction")
public class UserTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String uid;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING) // 在数据库中保存英文枚举值
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType; // 消费或储值

    @Column(name = "amount", nullable = false)
    private BigDecimal amount; // 交易金额

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate; // 交易时间

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate; // 记录创建时间
    @Column(name = "updated_date")
    private LocalDateTime updatedDate; // 记录更新时间

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "pay_type")
    private PayType payType; //付款方式

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.transactionDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // 枚举类型，用于表示交易类型
    public enum TransactionType {
        CONSUME("消費"),   // 英文保存到数据库，中文用于展示
        DEPOSIT("儲值");

        private final String friendlyName;

        TransactionType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        // 根据友好字符串返回对应的枚举
        public static TransactionType fromFriendlyName(String friendlyName) {
            for (TransactionType type : TransactionType.values()) {
                if (type.getFriendlyName().equals(friendlyName)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("非法参数: No enum constant for friendly name " + friendlyName);
        }
    }

    // 返回交易类型的友好字符串，用于前端展示
    public String getFriendlyTransactionType() {
        return transactionType.getFriendlyName();
    }

    // 设置友好的交易类型
    public void setFriendlyTransactionType(String friendlyTransactionType) {
        this.transactionType = TransactionType.fromFriendlyName(friendlyTransactionType);
    }
}
