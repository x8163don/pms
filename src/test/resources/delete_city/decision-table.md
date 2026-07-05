# 決策表：delete_city

`City "1" -- "0..*" Fab`：City 是 Fab 的父物件。刪除 City 前必須確認底下沒有任何 Fab，否則會產生「孤兒」Fab（`fab.city_id` 指向不存在的 City）。

## Criteria（條件）

| Criteria | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| `id` 對應的 City | 不存在 | 存在 | 存在 |
| 該 City 底下是否有 Fab | 不適用 | 無（空集合）| 有（≥1 筆）|

## Action（預期結果）

| Action | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| 刪除結果 | 失敗 | 成功 | 失敗 |
| 原因 | City 不存在 | 沒有子物件，可安全刪除 | 仍有 Fab 依賴此 City，刪除會產生孤兒資料 |
| 錯誤型態 | `CityNotFoundException` | 不適用 | `CityInUseException` |
| 預期 HTTP 狀態碼 | 404 | 204 | 409 |

## Rule 說明

| | Rule 1 | Rule 2 | Rule 3 |
|---|---|---|---|
| 測試企圖 | 驗證刪除不存在的資源時明確失敗，而非 NPE 或靜默成功 | 正常流程：無子物件時可刪除 | 核心保護規則：有子物件時擋下刪除，避免孤兒資料 |
| 重要性 | 高 | 高 | 高：本次需求核心，容易漏寫子物件檢查而直接刪除 |

## 已確認的規則

- 檢查順序：先確認 City 是否存在（Rule 1），再檢查是否有 Fab 依賴（Rule 2、3）——不存在時不需要、也不應該再查一次 Fab。
- 「有 Fab 依賴」用存在性查詢（`existsByCityId`），不需要知道確切數量，1 筆與多筆行為相同，不另外列規則。
- 衝突（有子物件）直接複用 409 + `CityInUseException`，不做自動級聯刪除（cascade delete）——若要刪除有子物件的 City，需先手動刪除底下所有 Fab。

## 對應測試

見 `src/test/java/com/pms/usecase/city/CityUseCaseImplTest.java`。

此決策表對應的業務邏輯（`CityUseCaseImpl.deleteCity`）已經實作，本次僅補齊尚未覆蓋的測試，屬於回歸測試（regression coverage），預期一開始就是綠燈（green），不是 TDD 的 red-green 循環。
