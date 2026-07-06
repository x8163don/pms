# Java 架構分層實作 - Usecase 層 (業務案例與介面合約)

本文件說明 Java 的 Usecase 層實作，展示如何實作 Usecase 業務案例及定義傳出埠（Ports）介面，並包含 **領域事件 (Domain Events)** 的發送機制。

---

## 1. Java Usecase 設計原則
1. **Ports & Adapters (六角架構)**：Usecase 層定義對外的用例介面（傳入埠，Inbound Ports）以及對外部技術依賴的抽象介面（傳出埠，Outbound Ports，如 Repository, PaymentService, DomainEventPublisher），不涉及具體技術實現。
2. **無狀態服務 (Stateless Service)**：Usecase 類別為無狀態的單例，所有操作所需的上下文均透過參數或安全上下文取得。
3. **依賴注入 (DI)**：在實作類別中使用構造函數注入 (Constructor Injection) 傳出埠依賴，便於單元測試時注入 Mock 物件。
4. **領域事件發送**：在 Usecase 執行完業務狀態變更並持久化後，透過 `DomainEventPublisher` 傳出埠發送領域事件，通知外部模組（如發送通知、觸發其他非同步工作流程）。

---

## 2. Java 實作範例

### 2.1. 傳出埠定義 (Outbound Ports)

#### AccountRepository.java (資料庫持久化抽象)
```java
package com.mybooker.usecase.account;

import com.mybooker.domain.account.Account;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> getById(Long id);
    Optional<Account> getByEmail(String email);
    Account save(Account account);
}
```

#### PaymentService.java (第三方支付管道抽象)
```java
package com.mybooker.usecase.account;

import com.mybooker.domain.account.Account;
import com.mybooker.domain.account.Subscription;

public interface PaymentService {
    String createCheckoutSession(Account account);
    Subscription getSubscriptionDetail(Long customerId, String subscriptionId);
    Subscription cancelSubscription(Account account);
}
```

#### DomainEventPublisher.java (領域事件發送器抽象)
定義發送領域事件的合約，屬於 Outbound Port。
```java
package com.mybooker.usecase.account;

public interface DomainEventPublisher {
    void publish(Object event);
}
```

---

### 2.2. 領域事件定義 (Domain Events)
事件物件本身屬於領域的一部分或用例的一部分，在 Java 中非常適合使用 Java 16+ 引入的 `record` 來實作唯讀的事件訊息（DTO/Event）。

#### PlanChangedEvent.java
```java
package com.mybooker.usecase.account.event;

import com.mybooker.domain.account.PlanType;

public record PlanChangedEvent(
    Long accountId,
    PlanType newPlanType
) {}
```

---

### 2.3. 傳入埠與業務案例 (Inbound Port & UseCase)

#### AccountUseCase.java
```java
package com.mybooker.usecase.account;

import com.mybooker.domain.account.Account;

public interface AccountUseCase {
    Account checkPlanExpired(Long accountId);
    Account addBookingCountOfThisMonth(Long accountId, int amount);
}
```

---

### 2.4. 業務用例實作 (UseCase Implementation)

#### AccountUseCaseImpl.java
```java
package com.mybooker.usecase.account;

import com.mybooker.domain.account.Account;
import com.mybooker.usecase.account.event.PlanChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class AccountUseCaseImpl implements AccountUseCase {
    private static final Logger log = LoggerFactory.getLogger(AccountUseCaseImpl.class);

    private final AccountRepository accountRepository;
    private final PaymentService paymentService;
    private final DomainEventPublisher eventPublisher;

    // 使用構造函數注入，將 Repository、Stripe 服務與事件發送器一起注入
    public AccountUseCaseImpl(
            AccountRepository accountRepository, 
            PaymentService paymentService,
            DomainEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Account checkPlanExpired(Long accountId) {
        Account account = accountRepository.getById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for ID: " + accountId));

        if (account.checkPlanExpired()) {
            log.info("Account {} standard plan has expired, downgrading to free plan", accountId);
            Account saved = accountRepository.save(account);

            // 狀態變更並保存成功後，發送訂閱計劃變更之領域事件
            eventPublisher.publish(new PlanChangedEvent(saved.getId(), saved.getPlan().getType()));

            return saved;
        }

        return account;
    }

    @Override
    @Transactional
    public Account addBookingCountOfThisMonth(Long accountId, int amount) {
        Account account = accountRepository.getById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for ID: " + accountId));

        account.addBookingCountOfThisMonth(amount);
        return accountRepository.save(account);
    }
}
```

#### AccountNotFoundException.java (用例層異常)
```java
package com.mybooker.usecase.account;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
```
