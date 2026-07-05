# 決策表：delete_fab

`Fab "1" -- "0..*" ParkingLot`：Fab 是 ParkingLot 的父物件。刪除 Fab 前必須確認底下沒有任何 ParkingLot，否則會產生「孤兒」ParkingLot（`parking_lot.fab_id` 指向不存在的 Fab）。

## Criteria（條件）

| Criteria | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| `id` 對應的 Fab | 不存在 | 存在 | 存在 |
| 該 Fab 底下是否有 ParkingLot | 不適用 | 無（空集合）| 有（≥1 筆）|

## Action（預期結果）

| Action | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| 刪除結果 | 失敗 | 成功 | 失敗 |
| 原因 | Fab 不存在 | 沒有子物件，可安全刪除 | 仍有 ParkingLot 依賴此 Fab，刪除會產生孤兒資料 |
| 錯誤型態 | `FabNotFoundException` | 不適用 | `FabInUseException` |
| 預期 HTTP 狀態碼 | 404 | 204 | 409 |

## Rule 說明

| | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| 測試企圖 | 驗證刪除不存在的資源時明確失敗，而非 NPE 或靜默成功 | 正常流程：無子物件時可刪除 | 核心保護規則：有子物件時擋下刪除，避免孤兒資料 |
| 重要性 | 高 | 高 | 高：本次需求核心，容易漏寫子物件檢查而直接刪除 |

## 已確認的規則

- 檢查順序：先確認 Fab 是否存在（Rule 1），再檢查是否有 ParkingLot 依賴（Rule 2、3）。
- 「有 ParkingLot 依賴」用存在性查詢（`existsByFabId`），1 筆與多筆行為相同，不另外列規則。
- 衝突（有子物件）直接複用 409 + `FabInUseException`，不做自動級聯刪除——與 [delete_city](../delete_city/decision-table.md) 的規則一致。

## 對應測試

見 `src/test/java/com/pms/usecase/fab/FabUseCaseImplTest.java`。

此決策表對應的業務邏輯（`FabUseCaseImpl.deleteFab`）已經實作，本次僅補齊尚未覆蓋的測試，屬於回歸測試，預期一開始就是綠燈。
