# 決策表：create_city（依 name 重複 / 長度規則判斷成功/失敗）

## Criteria（條件）

| Criteria | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| 輸入的 name | `"Taipei"` | `"Taipei"` | `""` / `"   "` | `"taipei"` | `" Taipei "` | `"Taipei"` | 剛好 50 字元 | 51 字元 |
| 系統內既有城市 | 無同名 | 已有 `"Taipei"`（完全相同）| 任意 | 已有 `"Taipei"`（僅大小寫不同）| 已有 `"Taipei"`（trim 後相同）| 無任何城市（第一筆）| 無同名 | 無同名 |

## Action（預期結果）

| Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| 建立結果 | 成功 | 失敗 | 失敗 | 成功 | 失敗 | 成功 | 成功 | 失敗 |
| 原因 | 不適用 | 名稱重複（trim 後完全相同）| 既有 blank 驗證擋下 | 大小寫不同不算重複 | trim 後與既有名稱相同 → 視為重複 | 空集合，理應無重複 | 剛好等於上限，合法 | 超過上限，不合法 |
| 錯誤型態 | 不適用 | `IllegalArgumentException` | `IllegalArgumentException` | 不適用 | `IllegalArgumentException` | 不適用 | 不適用 | `IllegalArgumentException` |
| 預期 HTTP 狀態碼 | 200 | 400 | 400 | 200 | 400 | 200 | 200 | 400 |

## Rule 說明

| | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| 測試企圖 | 正常流程 | 核心重複規則（完全相同）| 既有驗證不受影響 | 驗證大小寫視為不同名稱 | 驗證 trim 後才比較，且儲存值也應是 trim 後的結果 | 驗證空集合不誤判為重複 | 長度上限邊界（等於）| 長度上限邊界（超過）|
| 重要性 | 高 | 高：本次需求核心 | 中 | 高：容易誤用 `equalsIgnoreCase` 而判斷錯誤 | 高：容易忘記 trim，導致明明重複卻放行 | 中 | 中：容易寫成 `> 50` 而非 `>= 50` 判斷錯誤 | 高：邊界值最容易寫錯 `>` vs `>=` |

## 已確認的規則

- 大小寫不算重複（Rule 4 = 成功）
- 前後空白算重複，需要 trim 後比較（Rule 5 = 失敗）
- 重複名稱直接複用 400 + `IllegalArgumentException`，不新增自訂例外/409
- name 長度上限為 50 個字元
- 假設：trim 在長度驗證之前進行（trim 後才檢查是否 ≤ 50）

## 對應測試

見 `src/test/java/com/pms/usecase/city/CityUseCaseImplTest.java`。

Rule 6（空集合時不誤判重複）在 `CityUseCaseImpl` 的 Mockito 單元測試層級，跟 Rule 1 在 mock 行為上無法區分（兩者都只是 `existsByName(...)` 回傳 `false`），實質上是 `CityRepositoryImpl`／DB 查詢層級的行為，適合寫在未來的 Repository 整合測試（例如用真實 H2 驗證 `existsByName` 在資料表為空時回傳 `false`），因此本檔案未在 UseCase 單元測試中重複實作。
