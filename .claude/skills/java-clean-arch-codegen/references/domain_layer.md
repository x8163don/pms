# Java 架構分層實作 - Domain 層 (領域實體與核心業務)

本文件說明 Java 的 Domain 層實作，展示如何實作乾淨的、不依賴外部框架的領域模型與核心業務規則。

---

## 1. Java 領域設計原則
1. **無框架依賴 (Framework-Free)**：不使用任何 Spring, Hibernate/JPA, Jackson 或 Lombok 等外部註解。確保領域模型是純粹的 POJO (Plain Old Java Object)。
2. **封裝與行為並重 (Rich Domain Model)**：領域物件除了狀態（欄位）外，應包含操作這些狀態的商業邏輯與規則，避免貧血模型。
3. **強型別與物件導向特性**：使用 Java 的 `interface` 與多型實作策略模式（例如 `Plan`），消除重複的 `if-else` 分支。

---

## 2. Java 實作範例

### 2.1. 計劃類型列舉 (PlanType.java)

```java
package com.mybooker.domain.account;

public enum PlanType {
    FREE,
    STANDARD
}
```

### 2.2. 計劃介面與實作 (Plan, FreePlan, StandardPlan)

#### Plan.java (領域介面)
```java
package com.mybooker.domain.account;

import java.time.LocalDateTime;

public interface Plan {
    PlanType getType();
    boolean isExpired();
    LocalDateTime getExpiredAt();
    Plan downgrade();
    void addBookingCountOfThisMonth(int amount);
    boolean canUseBooking();
}
```

#### FreePlan.java (免費版計費計劃)
```java
package com.mybooker.domain.account;

import java.time.LocalDateTime;

public class FreePlan implements Plan {
    private int bookingCountOfThisMonth;

    public FreePlan(int bookingCountOfThisMonth) {
        this.bookingCountOfThisMonth = bookingCountOfThisMonth;
    }

    @Override
    public PlanType getType() {
        return PlanType.FREE;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public LocalDateTime getExpiredAt() {
        return null;
    }

    @Override
    public Plan downgrade() {
        return new FreePlan(0);
    }

    @Override
    public void addBookingCountOfThisMonth(int amount) {
        this.bookingCountOfThisMonth += amount;
    }

    @Override
    public boolean canUseBooking() {
        return this.bookingCountOfThisMonth < 10;
    }

    public int getBookingCountOfThisMonth() {
        return bookingCountOfThisMonth;
    }
}
```

#### StandardPlan.java (標準版計費計劃)
```java
package com.mybooker.domain.account;

import java.time.LocalDateTime;

public class StandardPlan implements Plan {
    private final LocalDateTime expiredAt;

    public StandardPlan(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    @Override
    public PlanType getType() {
        return PlanType.STANDARD;
    }

    @Override
    public boolean isExpired() {
        if (this.expiredAt == null) {
            return true;
        }
        return this.expiredAt.isBefore(LocalDateTime.now());
    }

    @Override
    public LocalDateTime getExpiredAt() {
        return this.expiredAt;
    }

    @Override
    public Plan downgrade() {
        return new FreePlan(0);
    }

    @Override
    public void addBookingCountOfThisMonth(int amount) {
        // 標準版無預約限制，不計數
    }

    @Override
    public boolean canUseBooking() {
        return true;
    }
}
```

### 2.3. 核心領域實體 (Account.java)

```java
package com.mybooker.domain.account;

import java.util.Objects;

public class Account {
    private final Long id;
    private final String email;
    private Plan plan;

    public Account(Long id, String email, Plan plan) {
        this.id = id;
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.plan = Objects.requireNonNull(plan, "Plan cannot be null");
    }

    // 業務行為：變更訂閱計劃
    public boolean changePlan(Plan newPlan) {
        Objects.requireNonNull(newPlan, "New plan cannot be null");
        if (this.plan.getType() == newPlan.getType()) {
            throw new IllegalArgumentException("Cannot change to the same plan type");
        }
        this.plan = newPlan;
        return true;
    }

    // 業務行為：增加當月預約次數
    public void addBookingCountOfThisMonth(int amount) {
        this.plan.addBookingCountOfThisMonth(amount);
    }

    // 業務行為：檢查訂閱計劃是否過期，若過期則自動降級
    public boolean checkPlanExpired() {
        if (this.plan.isExpired()) {
            this.plan = this.plan.downgrade();
            return true;
        }
        return false;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Plan getPlan() {
        return plan;
    }
}
```

### 2.4. 外部訂閱資訊值對象 (Subscription.java)

代表從第三方支付平台（如 Stripe）查詢回來的訂閱資訊，為唯讀的值對象 (Value Object)。

```java
package com.mybooker.domain.account;

import java.time.LocalDateTime;

public record Subscription(
    String subscriptionId,
    PlanType planType,
    LocalDateTime expiredAt,
    String status
) {}
```
