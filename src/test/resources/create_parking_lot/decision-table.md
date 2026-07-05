# 決策表：create_parking_lot

`ParkingLot` 隸屬於 `Fab`（`Fab "1" -- "0..*" ParkingLot`），且 `ParkingLot` 本身是 Aggregate Root，`Zone` 是它的子實體（見 CLAUDE.md 的 deviation 說明）。拆成三張子決策表：

- 決策表 1：`fabId` 存在性檢查
- 決策表 2：`name` 重複 / 長度規則（比照 create_city / create_fab 直接沿用）
- 決策表 3：`regularTotal` / `flexibleTotal` 數值規則

> 現況：`ParkingLotUseCaseImpl.createParkingLot` 目前**只檢查 `fabId` 是否存在**；`ParkingLot` domain 的 `validateName` 也**只檢查 blank**，`validateNonNegative` 已檢查負數。決策表 2 全部、決策表 3 的邊界案例是尚未實作/未測試的部分。

## 決策表 1：fabId 存在性檢查

### Criteria（條件）

| Criteria | Rule 1 | Rule 2 |
|---|---|---|
| `fabId` 對應的 Fab | 存在 | 不存在（如已刪除或從未存在的 ID）|

### Action（預期結果）

| Action | Rule 1 | Rule 2 |
|---|---|---|
| 建立結果 | 成功（繼續走決策表 2、3 的驗證）| 失敗 |
| 錯誤型態 | 不適用 | `FabNotFoundException` |
| 預期 HTTP 狀態碼 | 200 | 404 |

### Rule 說明

| | Rule 1 | Rule 2 |
|---|---|---|
| 測試企圖 | 正常流程，父物件存在 | 驗證缺少父物件時明確失敗，而非丟出 NPE 或建立出「孤兒」ParkingLot |
| 重要性 | 高 | 高：目前 `ParkingLotUseCaseImpl` 已有此檢查，須避免回歸 |

已由現有程式碼 (`ParkingLotUseCaseImpl.createParkingLot` 第 22–23 行) 實作並覆蓋，僅需確認測試涵蓋 Rule 2。

檢查順序：先驗證 `fabId` 是否存在（決策表 1），通過後才進入 name（決策表 2）與數值（決策表 3）驗證。

## 決策表 2：name 重複 / 長度規則（直接沿用 create_city / create_fab，假設 `fabId` 皆有效）

### Criteria（條件）

| Criteria | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| 輸入的 name | `"B1"` | `"B1"` | `""` / `"   "` | `"b1"` | `" B1 "` | `"B1"` | 剛好 50 字元 | 51 字元 |
| 系統內既有 ParkingLot（全域，不分 Fab）| 無同名 | 已有 `"B1"`（完全相同）| 任意 | 已有 `"B1"`（僅大小寫不同）| 已有 `"B1"`（trim 後相同）| 無任何 ParkingLot（第一筆）| 無同名 | 無同名 |

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

## 決策表 3：regularTotal / flexibleTotal 數值規則（假設 fabId 有效、name 合法）

### Criteria（條件）

| Criteria | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|---|---|---|---|
| `regularTotal` | 10 | -1 | 0 | 0 |
| `flexibleTotal` | 5 | 5 | 10 | 0 |

### Action（預期結果）

| Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|---|---|---|---|
| 建立結果 | 成功 | 失敗 | 成功 | 成功 |
| 原因 | 正常兩種車位都有 | 負數不合法 | 只有 flexible、regular 為 0 屬合法邊界 | 兩者皆 0（容量資訊性欄位，允許之後才用 addZone 分配）|
| 錯誤型態 | 不適用 | `IllegalArgumentException` | 不適用 | 不適用 |
| 預期 HTTP 狀態碼 | 200 | 400 | 200 | 200 |
| `regularRemain` / `flexibleRemain` | = `regularTotal` / `flexibleTotal` | 不適用 | = `regularTotal` / `flexibleTotal` | = `regularTotal` / `flexibleTotal` |

### Rule 說明

| | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|---|---|---|---|
| 測試企圖 | 正常流程 | 驗證負數被擋下（`validateNonNegative` 既有邏輯）| 允許單一車位類型為 0（例如全彈性車位的樓層）| 允許建立當下兩者皆為 0（尚未分配容量），行為與 Zone 加總無關 |
| 重要性 | 高 | 高：既有邏輯，避免回歸 | 中 | 中：對齊你的確認——ParkingLot 的容量欄位僅是資訊性欄位，不需要跟 Zone 加總做交叉驗證 |

`flexibleTotal` 為負數的鏡像案例（Rule 2 的對稱情況）不重複列出，邏輯相同、`validateNonNegative` 共用同一段程式碼。

## 已確認的規則

- name 重複檢查為全域（across 所有 ParkingLot），不分 Fab 範圍——沿用 create_fab 的模式，`ParkingLotRepository` 新增 `existsByName(name)`。
- 大小寫不算重複、trim 後才比較、重複直接複用 400 + `IllegalArgumentException`、長度上限 50、trim 在長度驗證之前——皆沿用 create_city / create_fab 已定案規則。
- `fabId` 不存在時，在 name/數值驗證之前就先失敗（`FabNotFoundException`，404）——決策表 1。
- **Zone 與 ParkingLot 容量欄位彼此獨立，不做加總驗證**（已與你確認：ParkingLot 的 `regularTotal`/`flexibleTotal` 只是資訊性欄位）。因此 `addZone` 目前的行為（無容量檢查）維持不變，本次決策表不涉及 `addZone`/`removeZone` 規則。
- 新建立的 `ParkingLot` 的 `zones` 一定是空集合（Zone 透過 `addZone` 另外新增）；`getAllTotal()`/`getAllRemain()` 在建立當下等於 `regularTotal + flexibleTotal`（因為還沒有任何 Zone）。

## 對應測試

見 `src/test/java/com/pms/usecase/parkinglot/ParkingLotUseCaseImplTest.java`。

決策表 2 的 Rule 6（空集合時不誤判重複）與 Rule 1 在 Mockito 單元測試層級無法區分（兩者都只是 `existsByName(...)` 回傳 `false`），比照 create_city / create_fab 的處理方式，適合留給未來的 Repository 整合測試（H2）驗證，不在 UseCase 單元測試中重複實作。
