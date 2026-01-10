# LexiPatch (MVP)

這是一個 Android 應用程式，專為學習英文單字設計。它包含桌面小工具 (Widget)、單字管理 (Library)、以及透過 AI 自動生成單字 (Paste) 的功能，並支援即時發音 (TTS)。

## 功能特點 (Features)

1.  **持久化儲存 (Local Persistence)**
    *   使用 JSON 檔案儲存在 App 內部空間 (`filesDir/vocab.json`)。
    *   資料模型包含：英文 (English)、中文 (Chinese)、縮寫 (Abbreviation-Optional)。
    *   初次執行時會自動載入 10 個範例單字。
    *   支援新增、刪除、與瀏覽進度記憶 (SharedPreferences)。

2.  **桌面小工具 (Widgets)**
    *   尺寸為 **2x1** (單層高度)。
    *   **上一頁/下一頁 (Prev/Next)**：即時切換單字。
    *   **點擊文字**：可直接開啟 App。
    *   若無單字，顯示提示訊息。

3.  **App 介面 (UI)**
    *   **Library (單字庫)**：列表顯示所有單字，右側設有小喇叭圖示 (TTS 朗讀)。
        *   **新增/刪除**：右下角 + 號新增，長按列表項目刪除。
    *   **Paste (AI 生成)**：
        *   貼上英文文章，透過 Google Gen AI SDK (Gemini) 自動提取重點單字。
        *   **輸入框優化**：深色背景、白色文字，提升閱讀舒適度。
    *   **Review (審核)**：勾選 AI 生成的單字，一鍵加入單字庫。
    *   **安全區域 (Safe Area)**：已適配系統狀態列 (Status Bar)。

## 如何設定 Gemini API Key (重要！)

本專案使用官方 Google Gen AI Java SDK。為了安全性，API Key **不再** 從 App 介面輸入，而是配置在專案的 `local.properties` 檔案中 (此檔案預設被 git 忽略，不會上傳)。

1.  取得 API Key：請至 [Google AI Studio](https://aistudio.google.com/) 申請。
2.  在 Android Studio 專案根目錄找到 `local.properties` 檔案。
3.  新增以下一行：
    ```properties
    GEMINI_API_KEY=你的_API_KEY_貼在這裡
    ```
4.  重新 Sync Gradle 並執行 App。

## 操作指南 (Run Instructions)

### 1. 執行專案
*   設定好 `local.properties` 後，點擊 **Run**。
*   支援 Android 11 (API 30) 以上版本。

### 2. 使用 Library & Widget
*   **朗讀**：點擊列表中的喇叭圖示。
*   **Widget**：新增到桌面 (2x1)，試按 Next/Prev。

### 3. 使用 AI 生成 (Paste & Generate)
*   切換到 **Paste** 分頁。
*   貼上英文文章。
*   點擊 **Generate Words**。
*   App 會自動讀取 `local.properties` 中的 Key 進行呼叫。
*   等待數秒，成功後跳轉至 **Review** 頁面選取單字。

## 技術實作 (Implementation Detail)

*   **SDK Update**: 升級使用 `com.google.genai:google-genai` Java SDK，取代舊的 REST API 呼叫。
*   **Security**: 使用 `BuildConfig` 結合 `local.properties` 管理 API Key，避免 Key 洩漏。
*   **UI Improvements**:
    *   `PasteFragment` 輸入區域改為深色主題 (`#333333` 背景, `#FFFFFF` 文字)。
    *   全域性 Safe Area Insets 適配。
*   **TTS**: Android 原生 TextToSpeech 整合。
