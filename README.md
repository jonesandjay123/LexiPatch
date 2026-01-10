# LexiPatch (MVP)

這是一個 Android 應用程式，專為學習英文單字設計。它包含桌面小工具 (Widget)、單字管理 (Library)、以及透過 AI 自動生成單字 (Paste) 的功能。

## 功能特點 (Features)

1.  **持久化儲存 (Local Persistence)**
    *   使用 JSON 檔案儲存在 App 內部空間 (`filesDir/vocab.json`)。
    *   資料模型包含：英文 (English)、中文 (Chinese)、縮寫 (Abbreviation-Optional)。
    *   初次執行時會自動載入 10 個範例單字。
    *   支援新增、刪除、與瀏覽進度記憶 (SharedPreferences)。

2.  **桌面小工具 (Widgets)**
    *   與 App 共用同一份單字列表。
    *   **上一頁/下一頁 (Prev/Next)**：即時切換單字。
    *   **點擊文字**：可直接開啟 App。
    *   若無單字，顯示提示訊息。

3.  **App 介面 (UI)**
    *   **Library (單字庫)**：列表顯示所有單字，可長按刪除，或右下角按鈕新增。
    *   **Paste (AI 生成)**：貼上英文文章，輸入 Gemini API Key，透過 LLM 自動提取重點單字。
    *   **Review (審核)**：勾選 AI 生成的單字，一鍵加入單字庫。

## 如何設定 Gemini API Key

本專案使用 Google Gemini API 進行 AI 單字生成。
在 **Paste** 頁面中，有一個欄位輸入您的 API Key。
*   取得 API Key：請至 [Google AI Studio](https://aistudio.google.com/) 申請。
*   為了簡化 MVP，Key 是直接在輸入框輸入。

## 操作指南 (Run Instructions)

### 1. 執行專案
*   在 Android Studio 中開啟專案。
*   點擊 **Run** (綠色三角形)。
*   支援 Android 11 (API 30) 以上版本。

### 2. 使用 Library (單字庫)
*   **首頁 (Library)** 會顯示目前的單字。
*   點擊右下角 **+** 按鈕：手動輸入英文與中文新增單字。Widget 會同步更新。
*   **長按**列表項目：可刪除該單字。

### 3. 使用 Widget (小工具)
*   回到手機桌面，長按空白處 -> 選擇 **Widgets** -> **LexiPatch**。
*   新增到桌面。
*   試按 Next/Prev 確保單字切換順暢。
*   若在 App 中刪除單字，Widget 按下一次按鈕時會自動修正顯示。

### 4. 使用 AI 生成 (Paste & Generate)
*   切換到 **Paste** 分頁。
*   在上方輸入框貼上一段英文文章 (例如新聞或故事)。
*   在下方輸入框貼上您的 **Gemini API Key**。
*   點擊 **Generate Words**。
*   等待數秒，成功後會跳轉至 **Review** 頁面。
*   勾選您想要的單字，點擊 **Add Selected**。
*   回到 Library，您會看到新單字已加入，且 Widget 也已包含這些新字。

## 技術實作 (Implementation Detail)

*   **JSON Persistence**: 使用 `Gson` 庫將 `List<VocabItem>` 序列化為 JSON 字串並寫入檔案。
*   **Widget Synchronization**: 使用 `Context.filesDir` 共用檔案讀取，並透過 `Intent.ACTION_APPWIDGET_UPDATE` 與 Broadcast 廣播通知 Widget 更新。
*   **Gemini API**: 使用 `HttpURLConnection` 發送 REST POST 請求到 Google Generative Language API，解析回傳的 JSON 結構。
*   **UI Architecture**: `MainActivity` 使用 `BottomNavigationView` 管理 `LibraryFragment` (RecyclerView) 與 `PasteFragment`。
*   **Dependencies**: 極簡化設計，僅增加 `Gson` 用於 JSON 處理，網路層使用原生 Java API 以符合最小依賴原則。

---
**注意**: 此為 MVP 展示版本，API Key 未進行加密儲存，請勿在生產環境中使用真實的付費 Key。
