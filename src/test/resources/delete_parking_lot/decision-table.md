# 決策表：delete_parking_lot

`ParkingLot` 是 Aggregate Root，`Zone` 是它的子實體（同一個 aggregate，見 [[project_parkinglot_zone_aggregate]]）。與 delete_city / delete_fab 不同的是：Zone 沒有獨立的 Repository/existsBy 查詢，子物件是否存在直接看 `ParkingLot.getZones()`（已經一起被讀出來的 aggregate 內容），而不是額外一次資料庫查詢。

## Criteria（條件）

| Criteria | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| `id` 對應的 ParkingLot | 不存在 | 存在 | 存在 |
| 該 ParkingLot 的 `zones` | 不適用 | 空集合 | 有 ≥1 個 Zone |

## Action（預期結果）

| Action | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| 刪除結果 | 失敗 | 成功 | 失敗 |
| 原因 | ParkingLot 不存在 | 沒有 Zone，可安全刪除 | 仍有 Zone 依附此 ParkingLot（同一 aggregate 內），刪除會遺失子資料 |
| 錯誤型態 | `ParkingLotNotFoundException` | 不適用 | `ParkingLotInUseException` |
| 預期 HTTP 狀態碼 | 404 | 204 | 409 |

## Rule 說明

| | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| 測試企圖 | 驗證刪除不存在的資源時明確失敗 | 正常流程：無 Zone 時可刪除 | 核心保護規則：有 Zone 時擋下刪除，避免資料連帶遺失 |
| 重要性 | 高 | 高 | 高：容易誤用 aggregate 內已讀出的 `zones` 做判斷，反而漏查或多查一次資料庫 |

## 已確認的規則

- 判斷「是否有子物件」時直接用 `parkingLot.getZones().isEmpty()`（aggregate 內已載入的集合），**不**額外呼叫 Repository 的 existsBy 查詢——這點與 delete_city／delete_fab 使用 `existsByCityId`/`existsByFabId` 不同，因為 Zone 沒有獨立 Repository（見 aggregate 設計）。
- 有 Zone 時直接複用 409 + `ParkingLotInUseException`，不做自動級聯刪除——若要刪除有 Zone 的 ParkingLot，需先用 `removeZone` 逐一刪除底下所有 Zone。
- 1 個 Zone 與多個 Zone 行為相同，不另外列規則。

## 對應測試

見 `src/test/java/com/pms/usecase/parkinglot/ParkingLotUseCaseImplTest.java`。

此決策表對應的業務邏輯（`ParkingLotUseCaseImpl.deleteParkingLot`）已經實作，本次僅補齊尚未覆蓋的測試，屬於回歸測試，預期一開始就是綠燈。
