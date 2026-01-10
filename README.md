# LexiPatch

這是一個 Android 應用程式，包含一個桌面小工具 (Widget)，用於學習英文單字。

## 功能特點 (Features)

*   **單字卡顯示**：在桌面上顯示英文單字 (English) 及其對應的中文含義 (Chinese)。
*   **互動按鈕**：小工具介面上包含「上一頁 (Prev)」和「下一頁 (Next)」按鈕。
*   **即時更新**：點擊按鈕後，小工具會立即更新顯示內容，無需等待。
*   **狀態保存**：系統會自動記住您目前看到的單字進度 (使用 `SharedPreferences`)，即使重開機也不會遺失進度。
*   **硬編碼字彙表**：內建 20 個精選單字，無需網路連線即可使用。

## 如何新增與測試 Widget (How to Use)

1.  **編譯與執行 (Build & Run)**:
    *   將此專案安裝到 Android 模擬器或實體裝置上。

2.  **新增 Widget 到桌面**:
    *   回到手機的主畫面 (Home Screen)。
    *   在桌面的空白處**長按 (Long Press)**。
    *   選擇選單中的 **Widgets (小工具)**。
    *   捲動或搜尋找到 **LexiPatch**。
    *   按住該 Widget 並將其**拖曳 (Drag)** 到桌面上的任意位置。

3.  **操作說明**:
    *   您會看到顯示的英文單字 (例如 "Apple") 與中文 ("蘋果")。
    *   點擊 **Next** 按鈕：切換到下一個單字。
    *   點擊 **Prev** 按鈕：回到上一個單字。
    *   循環功能：當到達最後一個單字時，再按 Next 會回到第一個；反之亦然。

## 專案結構說明 (Implementation Details)

主要修改的檔案如下：

*   **Kotlin 程式碼**: `app/src/main/java/com/joneslab/lexipatch/VocabularyWidget.kt`
    *   繼承自 `AppWidgetProvider`。
    *   處理 `ACTION_NEXT` 和 `ACTION_PREV` 廣播事件。
    *   管理單字列表與索引更新。

*   **介面佈局 (Layout)**: `app/src/main/res/layout/widget_vocabulary.xml`
    *   定義 Widget 的外觀，包含兩個 `TextView` (顯示文字) 和兩個 `Button`。

*   **Widget 設定**: `app/src/main/res/xml/vocabulary_widget_info.xml`
    *   定義 Widget 的最小尺寸與初始佈局。

*   **清單文件 (Manifest)**: `app/src/main/AndroidManifest.xml`
    *   註冊 `VocabularyWidget` 接收器 (Receiver)，以便系統能識別並載入此 Widget。
