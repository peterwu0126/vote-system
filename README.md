# 線上投票系統 (Online Voting System)

前後端分離的線上投票系統。後台可管理投票項目（新增/更新/刪除），使用者可對項目進行多選投票並即時查看累積票數。

## 技術棧

| 分類 | 技術 |
|---|---|
| 前端 | Vue.js 3 + Vite + Axios + Vue Router |
| 後端 | Spring Boot 3.3.4 (Java 21) |
| 資料庫 | MySQL 8 / MariaDB |
| 資料庫存取 | Spring JDBC `SimpleJdbcCall` 呼叫 Stored Procedure |
| 建置工具 | Maven |

## 系統架構

```
瀏覽器 (Vue.js)  ──HTTP/JSON(REST)──▶  Spring Boot (Application Server)  ──JDBC(Stored Procedure)──▶  MySQL
```

後端依職責分為四層：

| 層級 | 路徑 | 說明 |
|---|---|---|
| 展示層 (Controller) | `backend/src/main/java/com/example/vote/controller` | 接收 HTTP 請求、回應結果 |
| 業務層 (Service) | `backend/src/main/java/com/example/vote/service` | 業務邏輯、Transaction 控制 |
| 資料層 (Repository) | `backend/src/main/java/com/example/vote/repository` | 呼叫 Stored Procedure 存取資料庫 |
| 共用層 (Common) | `backend/src/main/java/com/example/vote/common` | 統一回應格式、例外處理、CORS、XSS 防護工具 |

## 專案結構

```
vote-system/
├── DB/
│   ├── schema.sql        # DDL：建表 + 6 個 Stored Procedure
│   └── data.sql          # DML：範例資料
├── backend/               # Spring Boot 專案
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/vote/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── dto/
│       │   └── common/
│       └── resources/application.yml
└── frontend/               # Vue.js 專案
    └── src/
        ├── views/         # VoteView.vue (投票頁) / ManageView.vue (後台管理頁)
        ├── components/
        ├── api/
        ├── router/
        └── utils/
```

## 功能對照

| 規格需求 | 實作位置 |
|---|---|
| 後台顯示所有投票項目 | `ManageView.vue` → `GET /api/items` |
| 後台新增投票項目 | `ManageView.vue` → `POST /api/items` |
| 後台更新/刪除投票項目 | `ManageView.vue` → `PATCH` / `DELETE /api/items/{id}` |
| 使用者查看項目與票數 | `VoteView.vue` → `GET /api/items` |
| 使用者多選投票 | `VoteView.vue` → `POST /api/votes`（`itemIds` 陣列） |
| 多表異動 Transaction | `VotingServiceImpl.vote()`：對多個 `itemId` 逐一呼叫 SP，包在 `@Transactional` 內，任一失敗則整批回滾 |

## REST API 一覽

| 方法 | 路徑 | 說明 |
|---|---|---|
| `GET` | `/api/items` | 查詢所有投票項目 |
| `GET` | `/api/items/{id}` | 查詢單一項目 |
| `POST` | `/api/items` | 新增投票項目 |
| `PATCH` | `/api/items/{id}` | 更新項目名稱 |
| `DELETE` | `/api/items/{id}` | 刪除項目（軟刪除） |
| `POST` | `/api/votes` | 執行投票（支援多選 `itemIds`） |
| `GET` | `/api/votes/records` | 查詢投票紀錄（可用 `?itemId=` 篩選） |

---

## 前置需求

| 工具 | 版本 |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL / MariaDB | 8.0+ / 10.6+ |

### 安裝提醒（Windows）

如果終端機回報 `'java'`、`'mvn'` 或 `'mysql'` 不是內部或外部命令：

1. **Java**：建議用 [Eclipse Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21) 的 `.msi` 安裝檔，安裝時勾選 **Add to PATH**，可省去手動設定環境變數。
2. **Maven**：[官方下載頁](https://maven.apache.org/download.cgi) 下載 zip 解壓縮後，需**手動**把 `bin` 路徑加進系統 `Path` 環境變數（注意：是編輯已存在的 `Path` 變數，不是新建一個叫 `maven` 的變數）。
3. **MySQL**：安裝完成後同樣需要把 `bin` 路徑加進 `Path`，才能在任何路徑下直接執行 `mysql` 指令。

加完環境變數後，**務必關閉所有終端機視窗、重新開一個新的**才會生效。

---

## 啟動步驟

### 1. 建立資料庫

用終端機（不是 GUI 工具的 SQL 編輯器，避免 `DELIMITER` 解析問題）執行：

```bash
cd DB
mysql -u root -p < schema.sql
mysql -u root -p < data.sql
```

`schema.sql` 會自動建立 `vote_system` 資料庫、兩張表（`voting_item`、`voting_record`）與 7 個 Stored Procedure。`data.sql` 會灌入範例資料（電腦 2 票、滑鼠 3 票、鍵盤 0 票）。

> ⚠️ `data.sql` 內含固定 `item_id` 的 `INSERT`，**只能執行一次**。重複執行會出現 `Duplicate entry` 錯誤；如需重跑，先清空表：
> ```sql
> USE vote_system;
> SET FOREIGN_KEY_CHECKS = 0;
> TRUNCATE TABLE voting_record;
> TRUNCATE TABLE voting_item;
> SET FOREIGN_KEY_CHECKS = 1;
> ```

#### 驗證資料庫

```sql
USE vote_system;
SHOW TABLES;
CALL sp_item_list();
```

應該看到 `voting_item`、`voting_record` 兩張表，以及電腦/滑鼠/鍵盤三筆資料。

### 2. 設定後端資料庫連線

編輯 `backend/src/main/resources/application.yml`：

```yaml
datasource:
  username: ${DB_USERNAME:你的MySQL帳號}
  password: ${DB_PASSWORD:你的MySQL密碼}
```

或者不改檔案，啟動前用環境變數指定：

```cmd
set DB_USERNAME=root
set DB_PASSWORD=你的密碼
```

（PowerShell 改用 `$env:DB_USERNAME="root"`；macOS/Linux 用 `export DB_USERNAME=root`）

### 3. 啟動後端

```bash
cd backend
mvn clean spring-boot:run
```

> 建議使用 `mvn clean spring-boot:run` 而非單純 `spring-boot:run`，避免修改原始碼後因編譯快取問題導致執行的是舊版 `.class`。

看到以下訊息代表啟動成功，**此視窗需保持開啟，不要按 Ctrl+C**：

```
Tomcat started on port 8080 (http) with context path '/'
Started VoteSystemApplication in X.XXX seconds
```

#### 驗證後端

瀏覽器開啟 `http://localhost:8080/api/items`，應回傳：

```json
{"success":true,"message":"OK","data":[{"itemId":1,"itemName":"電腦","voteCount":2,...}, ...]}
```

### 4. 啟動前端

**開一個新的終端機視窗**（後端視窗保持執行）：

```bash
cd frontend
npm install
npm run dev
```

啟動成功後瀏覽器開啟：

```
http://localhost:5173
```

前端已透過 `vite.config.js` 設定 proxy，`/api` 請求會自動轉發到 `localhost:8080`，無需額外設定 CORS。

---

## 常見問題排解

| 現象 | 原因 | 解法 |
|---|---|---|
| `Public Key Retrieval is not allowed` | MySQL 8 預設認證方式 + 未啟用 SSL | 連線字串加上 `allowPublicKeyRetrieval=true&useSSL=false`（`application.yml` 已內建此設定） |
| `Access denied for user 'xxx'@'localhost'` | 帳號不存在或密碼錯誤 | 確認 MySQL 帳密，或用 `CREATE USER` 建立對應帳號並 `GRANT` 權限 |
| DBeaver 顯示 `Invalid column reference` / `does not exist` | IDE 的即時語法檢查誤判，或執行到一半的腳本片段 | 全選整份 `.sql` 檔案、用「Execute SQL Script」執行，而非單句執行；改用終端機 `mysql -u root -p < file.sql` 最穩定 |
| `Duplicate entry '1' for key 'voting_item.PRIMARY'` | `data.sql` 被重複執行 | 確認資料是否已存在（`SELECT * FROM voting_item`），若已存在可忽略；若要重跑請先 `TRUNCATE` |
| API 回 `{"success":false,"message":"系統發生錯誤，請稍後再試"}` | 後端內部例外，詳細原因藏在後端終端機的 stack trace 中 | 查看後端視窗的 `ERROR` 訊息，常見原因：資料庫帳密錯誤、SP 不存在、連線設定問題 |
| `Connection is read-only. Queries leading to data modification are not allowed` | Service 層方法標註 `@Transactional(readOnly = true)`，但 MySQL Connector/J 將「呼叫 Stored Procedure」一律視為可能寫入，在 read-only 連線上禁止執行 | 移除 `readOnly = true`，改用一般 `@Transactional`（本專案已修正） |
| API 回傳 `{"success":true,"data":[]}` 但資料庫裡明明有資料 | `SimpleJdbcCall` 自動探測 SP metadata 在部分 MySQL 環境下失敗，導致取不到結果集，但不拋出例外 | Repository 改用 `.withoutProcedureColumnMetaDataAccess()` + 明確 `.declareParameters()` / `.returningResultSet()`，不依賴自動探測（本專案已修正） |
| `'mysql'` / `'mvn'` / `'java'` 不是內部或外部命令 | 對應工具未安裝，或未加入系統 PATH | 安裝對應工具，並編輯系統環境變數 `Path`（不要新建一個獨立變數），新增後重開終端機視窗 |
| Maven 報 `No plugin found for prefix 'spring-boot'` | 目前資料夾沒有 `pom.xml`（路徑切錯，常見於資料夾巢狀同名，如 `vote-system/vote-system/backend`） | 用 `dir /s /b pom.xml` 從上層往下搜尋，確認正確路徑後再 `cd` |

---

## 安全性設計摘要

- **SQL Injection**：所有資料庫存取均透過 Stored Procedure，且呼叫端使用 JDBC 參數化綁定（`MapSqlParameterSource`），未使用任何字串拼接 SQL；SP 內部亦為靜態 SQL，無動態 SQL（`PREPARE`/`EXECUTE`）。
- **XSS**：前端 Vue 模板插值預設自動 HTML escape，且未使用 `v-html`；後端額外使用 `XssSanitizer`（基於 OWASP Encoder）對輸入進行二次清洗，並以 Bean Validation（`@Pattern`/`@Size`）限制輸入格式。
- **Transaction**：多選投票（`VotingServiceImpl.vote()`）對每個選中項目逐一呼叫 `sp_vote_cast`（單項目原子操作：寫入投票紀錄 + 累加票數），整個迴圈包覆在 `@Transactional` 範圍內，任一項目失敗即整批回滾，避免部分成功的資料不一致。

## License

本專案為教學/練習用範例專案。
