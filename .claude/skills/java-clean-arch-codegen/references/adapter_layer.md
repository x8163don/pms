# Java 架構分層實作 - Adapter 層 (外部基礎設施整合)

本文件說明 Java 的 Adapter 層實作，展示如何在 Java (Spring Boot / JPA) 中實作與外部系統、資料庫的底層整合，並實作 **領域事件 (Domain Events)** 的具體發送技術。

---

## 1. Java Adapter 設計原則

1. **Mapper Pattern (對映器模式)**：實作資料庫 Entity 與 Domain Model 的雙向對映。避免將 JPA 註解（如 `@Entity`, `@Table`, `@Column`）寫在 Domain 類別中，保護 Domain 不受資料庫結構變動的干擾。
2. **具體技術整合**：在此層引用具體的外部 SDK（例如 `stripe-java`），對 Usecase 隱藏底層 API 的調用細節。
3. **快取治理**：可在 Repository 實作中結合 Spring Cache 或自定義 Redis 快取邏輯。
4. **事件發送器實作**：將 Usecase 層定義的 `DomainEventPublisher` 介面（Port），對接到具體的技術（如 Spring 的 `ApplicationEventPublisher` 以進行 JVM 內本機發送，或對接到 Kafka/RabbitMQ 用於跨服務分散式發送）。

---

## 2. Java 實作範例

### 2.1. 資料庫實體與 JPA 倉儲 (AccountEntity.java / AccountJpaRepository.java)

#### AccountEntity.java (JPA 實體對象)

```java
package com.mybooker.adapter.repository.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "plan_type", nullable = false)
    private int planType;

    @Column(name = "plan_expire_time")
    private LocalDateTime planExpireTime;

    @Column(name = "appointment_count_of_this_month")
    private int appointmentCountOfThisMonth;

    // Getters, Setters, Boilerplate constructors
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getPlanType() { return planType; }
    public void setPlanType(int planType) { this.planType = planType; }

    public LocalDateTime getPlanExpireTime() { return planExpireTime; }
    public void setPlanExpireTime(LocalDateTime planExpireTime) { this.planExpireTime = planExpireTime; }

    public int getAppointmentCountOfThisMonth() { return appointmentCountOfThisMonth; }
    public void setAppointmentCountOfThisMonth(int appointmentCountOfThisMonth) { this.appointmentCountOfThisMonth = appointmentCountOfThisMonth; }
}
```

#### AccountJpaRepository.java (Spring Data JPA)

```java
package com.mybooker.adapter.repository;

import com.mybooker.adapter.repository.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByEmail(String email);
}
```

---

### 2.2. 資料對映器 (AccountMapper.java)

負責在 DB Model 與 Domain Model 之間進行安全轉換。

```java
package com.mybooker.adapter.repository.mapper;

import com.mybooker.adapter.repository.entity.AccountEntity;
import com.mybooker.domain.account.*;

public class AccountMapper {

    public static Account toDomain(AccountEntity entity) {
        if (entity == null) return null;

        Plan plan;
        if (entity.getPlanType() == PlanType.STANDARD.ordinal()) {
            plan = new StandardPlan(entity.getPlanExpireTime());
        } else {
            plan = new FreePlan(entity.getAppointmentCountOfThisMonth());
        }

        return new Account(entity.getId(), entity.getEmail(), plan);
    }

    public static AccountEntity toEntity(Account domain) {
        if (domain == null) return null;

        AccountEntity entity = new AccountEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPlanType(domain.getPlan().getType().ordinal());

        if (domain.getPlan() instanceof StandardPlan) {
            entity.setPlanExpireTime(domain.getPlan().getExpiredAt());
        } else if (domain.getPlan() instanceof FreePlan) {
            entity.setAppointmentCountOfThisMonth(((FreePlan) domain.getPlan()).getBookingCountOfThisMonth());
        }

        return entity;
    }
}
```

---

### 2.3. 倉儲實作 (AccountRepositoryImpl.java)

實作 Usecase 的 Port。

```java
package com.mybooker.adapter.repository;

import com.mybooker.adapter.repository.entity.AccountEntity;
import com.mybooker.adapter.repository.mapper.AccountMapper;
import com.mybooker.domain.account.Account;
import com.mybooker.usecase.account.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpaRepository;

    public AccountRepositoryImpl(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Account> getById(Long id) {
        return jpaRepository.findById(id).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> getByEmail(String email) {
        return jpaRepository.findByEmail(email).map(AccountMapper::toDomain);
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountMapper.toEntity(account);
        AccountEntity saved = jpaRepository.save(entity);
        return AccountMapper.toDomain(saved);
    }
}
```

---

### 2.4. 第三方 SDK 服務實作 (StripePaymentServiceImpl.java)

```java
package com.mybooker.adapter.service;

import com.mybooker.domain.account.Account;
import com.mybooker.domain.account.Subscription;
import com.mybooker.usecase.account.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StripePaymentServiceImpl implements PaymentService {

    private final String standardPriceId;
    private final String webHost;

    public StripePaymentServiceImpl(
            @Value("${stripe.api.key}") String apiKey,
            @Value("${stripe.price.standard}") String standardPriceId,
            @Value("${stripe.web-host}") String webHost) {
        Stripe.apiKey = apiKey;
        this.standardPriceId = standardPriceId;
        this.webHost = webHost;
    }

    @Override
    public String createCheckoutSession(Account account) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(webHost + "/checkout/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(webHost + "/account")
                .setCustomerEmail(account.getEmail())
                .setClientReferenceId(String.valueOf(account.getId()))
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(standardPriceId)
                        .setQuantity(1L)
                        .build()
                )
                .putSubscriptionDataMetadata("customer_id", String.valueOf(account.getId()))
                .build();

            Session session = Session.create(params);
            return session.getId();
        } catch (Exception e) {
            throw new PaymentGatewayException("Failed to create Stripe checkout session", e);
        }
    }

    @Override
    public Subscription getSubscriptionDetail(Long customerId, String subscriptionId) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public Subscription cancelSubscription(Account account) {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
```

---

### 2.5. 自訂例外 (PaymentGatewayException.java)

在 Adapter 層捕捉外部 SDK 的例外後，轉換為自訂例外向上拋出，避免 Stripe 的例外類型外洩至 Usecase 層。

```java
package com.mybooker.adapter.service;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

### 2.6. 領域事件發送器實作 (SpringDomainEventPublisher.java)

實作 Usecase 層定義的 `DomainEventPublisher` Outbound Port。這裡使用 Spring Boot 的本機事件發送機制（同一個 JVM 中同步或非同步分發）。

```java
package com.mybooker.adapter.event;

import com.mybooker.usecase.account.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Object event) {
        if (event != null) {
            // 發送 Spring ApplicationContext 本機事件
            applicationEventPublisher.publishEvent(event);
        }
    }
}
```
