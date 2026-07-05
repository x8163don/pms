# 決策表：create_fab

`Fab` 隸屬於 `City`（`City "1" -- "0..*" Fab`），因此比 `create_city` 多一個「父物件是否存在」的前置條件。拆成兩張子決策表：

- 決策表 1：`cityId` 存在性檢查
- 決策表 2：`name` 重複 / 長度規則（比照 `create_city` 直接沿用，前提：`cityId` 皆有效）

> 現況：`FabUseCaseImpl.createFab` 目前**只檢查 `cityId` 是否存在**，`Fab` domain 的 `validateName` 也**只檢查 blank**，尚未有 trim、長度上限、重複名稱檢查。決策表 2 的規則是尚未實作的新需求，直接沿用 `create_city` 已定案的規則（見下方「已確認的規則」），準備進入 TDD。

## 決策表 1：cityId 存在性檢查

### Criteria（條件）

| Criteria | Rule 1 | Rule 2 |
|---|---|---|
| `cityId` 對應的 City | 存在 | 不存在（如已刪除或從未存在的 ID）|

### Action（預期結果）

| Action | Rule 1 | Rule 2 |
|---|---|---|
| 建立結果 | 成功（繼續走決策表 2 的 name 驗證）| 失敗 |
| 錯誤型態 | 不適用 | `CityNotFoundException` |
| 預期 HTTP 狀態碼 | 200 | 404 |

### Rule 說明

| | Rule 1 | Rule 2 |
|---|---|---|
| 測試企圖 | 正常流程，父物件存在 | 驗證缺少父物件時明確失敗，而非丟出 NPE 或建立出「孤兒」Fab |
| 重要性 | 高 | 高：目前 `FabUseCaseImpl` 已有此檢查，須避免回歸 |

已由現有程式碼 (`FabUseCaseImpl.createFab` 第 25–26 行) 實作並覆蓋，僅需確認測試涵蓋 Rule 2。

檢查順序：先驗證 `cityId` 是否存在（決策表 1），通過後才進入 name 驗證（決策表 2）。

## 決策表 2：name 重複 / 長度規則（直接沿用 create_city，假設 `cityId` 皆有效）

### Criteria（條件）

| Criteria | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| 輸入的 name | `"1F-A"` | `"1F-A"` | `""` / `"   "` | `"1f-a"` | `" 1F-A "` | `"1F-A"` | 剛好 50 字元 | 51 字元 |
| 系統內既有 Fab（全域，不分 City）| 無同名 | 已有 `"1F-A"`（完全相同）| 任意 | 已有 `"1F-A"`（僅大小寫不同）| 已有 `"1F-A"`（trim 後相同）| 無任何 Fab（第一筆）| 無同名 | 無同名 |

### Action（預期結果）

| Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| 建立結果 | 成功 | 失敗 | 失敗 | 成功 | 失敗 | 成功 | 成功 | 失敗 |
| 原因 | 不適用 | 名稱重複（trim 後完全相同）| 既有 blank 驗證擋下 | 大小寫不同不算重複 | trim 後與既有名稱相同 → 視為重複 | 空集合，理應無重複 | 剛好等於上限，合法 | 超過上限，不合法 |
| 錯誤型態 | 不適用 | `IllegalArgumentException` | `IllegalArgumentException` | 不適用 | `IllegalArgumentException` | 不適用 | 不適用 | `IllegalArgumentException` |
| 預期 HTTP 狀態碼 | 200 | 400 | 400 | 200 | 400 | 200 | 200 | 400 |

### Rule 說明

| | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| 測試企圖 | 正常流程 | 核心重複規則（完全相同）| 既有驗證不受影響 | 驗證大小寫視為不同名稱 | 驗證 trim 後才比較，且儲存值也應是 trim 後的結果 | 驗證空集合不誤判為重複 | 長度上限邊界（等於）| 長度上限邊界（超過）|
| 重要性 | 高 | 高：本次需求核心 | 中 | 高：容易誤用 `equalsIgnoreCase` 而判斷錯誤 | 高：容易忘記 trim，導致明明重複卻放行 | 中 | 中：容易寫成 `> 50` 而非 `>= 50` 判斷錯誤 | 高：邊界值最容易寫錯 `>` vs `>=` |

## 已確認的規則

- **name 重複檢查為全域（across 所有 City），不分 City 範圍** — 直接沿用 `City.existsByName` 的模式，`FabRepository` 新增對應的 `existsByName(name)`（而非 `existsByCityIdAndName`）。
- 大小寫不算重複（Rule 4 = 成功）——沿用 create_city。
- 前後空白算重複，需要 trim 後比較（Rule 5 = 失敗）——沿用 create_city。
- 重複名稱直接複用 400 + `IllegalArgumentException`，不新增自訂例外/409——沿用 create_city。
- name 長度上限為 50 個字元——沿用 create_city。
- trim 在長度驗證之前進行（trim 後才檢查是否 ≤ 50）——沿用 create_city。
- `cityId` 不存在時，在 name 驗證之前就先失敗（`CityNotFoundException`，404）——決策表 1。

## 對應測試

見 `src/test/java/com/pms/usecase/fab/FabUseCaseImplTest.java`。

決策表 2 的 Rule 6（空集合時不誤判重複）與 Rule 1 在 Mockito 單元測試層級無法區分（兩者都只是 `existsByName(...)` 回傳 `false`），比照 `create_city` 的處理方式，適合留給未來的 Repository 整合測試（H2）驗證，不在 UseCase 單元測試中重複實作。
