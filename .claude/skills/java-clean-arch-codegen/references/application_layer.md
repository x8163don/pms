# Java 架構分層實作 - Application 層 (通訊與應用進入點)

本文件說明 Java 的 Application 層實作，展示如何在 Java (Spring Boot) 中實作 REST 控制器、配置屬性（Properties）與資料傳輸對象 (DTO)。

---

## 1. Java Application 設計原則
1. **傳輸框架隔離**：將 Spring Boot REST / HTTP 機制完全限縮在這一層。處理器只做路由分發、參數解析、格式轉換與異常統一轉換。
2. **聲明式輸入驗證**：利用 `jakarta.validation`（例如 `@NotBlank`, `@NotNull`）進行強型別與宣告式欄位校驗，在進入業務邏輯前拒絕不合規的請求。
3. **配置映射 (Configuration Properties)**：利用 Spring `@ConfigurationProperties` 來集中映射環境變數或專案配置。

---

## 2. Java 實作範例

### 2.1. 配置對象 (ServiceProperties.java)

```java
package com.mybooker.application.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
public class ServiceProperties {
    private String authifyUrl;
    private int serviceId;
    private int ownerId;

    // Getters and Setters
    public String getAuthifyUrl() { return authifyUrl; }
    public void setAuthifyUrl(String authifyUrl) { this.authifyUrl = authifyUrl; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
}
```

### 2.2. 請求與回應 DTO (CheckPlanRequest.java)

```java
package com.mybooker.application.account.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckPlanRequest {
    
    @NotBlank(message = "url_name must not be blank")
    private String urlName;

    // Getter and Setter
    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }
}
```

---

### 2.3. 回應 DTO (CheckoutSessionResponse.java)

```java
package com.mybooker.application.account.dto;

public class CheckoutSessionResponse {

    private final String sessionId;

    public CheckoutSessionResponse(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
```

---

### 2.4. 控制器 (AccountController.java)

```java
package com.mybooker.application.account;

import com.mybooker.application.account.dto.CheckPlanRequest;
import com.mybooker.application.account.dto.CheckoutSessionResponse;
import com.mybooker.domain.account.Account;
import com.mybooker.usecase.account.AccountUseCase;
import com.mybooker.usecase.account.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountUseCase accountUseCase;
    private final PaymentService paymentService;

    public AccountController(AccountUseCase accountUseCase, PaymentService paymentService) {
        this.accountUseCase = accountUseCase;
        this.paymentService = paymentService;
    }

    // 取得 Stripe 支付結帳 Session ID
    @GetMapping("/{id}/checkout-session")
    public ResponseEntity<CheckoutSessionResponse> getCheckoutSessionId(@PathVariable("id") Long accountId) {
        Account account = accountUseCase.checkPlanExpired(accountId);
        String sessionId = paymentService.createCheckoutSession(account);
        return ResponseEntity.ok(new CheckoutSessionResponse(sessionId));
    }

    // 檢查計劃並觸發降級
    @PostMapping("/{id}/check-plan")
    public ResponseEntity<Void> checkPlan(@PathVariable("id") Long accountId) {
        accountUseCase.checkPlanExpired(accountId);
        return ResponseEntity.ok().build();
    }
}
```

---

### 2.5. 全域例外處理器 (GlobalExceptionHandler.java)

統一捕捉 UseCase 層拋出的例外，轉換為標準的 HTTP 錯誤回應。

```java
package com.mybooker.application.exception;

import com.mybooker.usecase.account.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕捉 UseCase 層拋出的資源不存在例外
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 捕捉 @Valid 驗證失敗
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    // 捕捉業務規則違反（Domain 層拋出）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    // 捕捉所有未預期例外
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }
}
```
