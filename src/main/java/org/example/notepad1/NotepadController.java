package org.example.notepad1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.image.Image;
import javafx.print.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.util.Pair;
import javafx.collections.FXCollections;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class NotepadController implements Initializable {

    @FXML
    private TabPane tabPane;
    
    @FXML
    private MenuBar menuBar;
    
    @FXML
    private HBox statusBar;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label positionLabel;
    
    @FXML
    private Label encodingLabel;
    
    @FXML
    private Label zoomLabel;
    
    @FXML
    private CheckMenuItem statusBarMenuItem;
    
    // 新增的视图管理元素
    @FXML
    private VBox mainView;
    
    @FXML
    private VBox settingsView;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button backButton;
    
    // 设置界面的控件
    @FXML
    private ComboBox<String> themeComboBox;
    
    @FXML
    private ComboBox<String> fontFamilyComboBox;
    
    @FXML
    private Spinner<Integer> fontSizeSpinner;
    
    @FXML
    private CheckBox boldCheckBox;
    
    @FXML
    private CheckBox italicCheckBox;
    
    @FXML
    private CheckBox applyToAllTabsCheckBox;
    
    @FXML
    private ComboBox<String> encodingComboBox;
    
    @FXML
    private CheckBox autoSaveCheckBox;
    
    @FXML
    private ComboBox<String> timeFormatComboBox;
    
    @FXML
    private Button resetButton;
    
    @FXML
    private Button applyButton;
    
    private int untitledCounter = 1;
    
    // 最近使用文件列表
    private List<String> recentFiles = new ArrayList<>();
    private static final int MAX_RECENT_FILES = 10;
    private static final String RECENT_FILES_CONFIG = "recent_files.properties";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 创建第一个标签页
        createNewTab();
        
        // 设置标签页关闭策略
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        
        // 加载最近使用文件列表
        loadRecentFiles();
        
        // 设置标签页选择监听器，用于更新状态栏
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            updateStatusBar();
        });
        
        // 初始化状态栏
        updateStatusBar();
        
        // 初始化设置界面的控件
        initializeSettingsControls();
    }
    
    private void initializeSettingsControls() {
        // 检查控件是否已初始化
        if (themeComboBox != null) {
            // 初始化主题选项
            themeComboBox.setItems(FXCollections.observableArrayList(
                "浅色", "深色", "跟随系统"
            ));
            themeComboBox.setValue("浅色");
        }
        
        if (fontFamilyComboBox != null) {
            // 初始化字体列表
            fontFamilyComboBox.setItems(FXCollections.observableArrayList(
                "微软雅黑", "宋体", "黑体", "楷体", "仿宋", "Arial", "Times New Roman", "Courier New"
            ));
            fontFamilyComboBox.setValue("微软雅黑");
        }
        
        if (encodingComboBox != null) {
            // 初始化编码选项
            encodingComboBox.setItems(FXCollections.observableArrayList(
                "UTF-8", "GBK", "ASCII"
            ));
            encodingComboBox.setValue("UTF-8");
        }
        
        if (timeFormatComboBox != null) {
            // 初始化时间格式选项
            timeFormatComboBox.setItems(FXCollections.observableArrayList(
                "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy HH:mm", "dd/MM/yyyy HH:mm"
            ));
            timeFormatComboBox.setValue("yyyy-MM-dd HH:mm:ss");
        }
        
        if (fontSizeSpinner != null) {
            // 初始化字体大小Spinner
            fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 72, 12));
        }
    }
    
    // 文件菜单处理器
    @FXML
    private void handleNew() {
        createNewTab();
    }
    
    @FXML
    private void handleNewWindow() {
        try {
            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("notepad.fxml"));
            Parent root = loader.load();
            
            // 创建新的舞台（窗口）
            Stage newStage = new Stage();
            newStage.setTitle("记事本");
            newStage.setScene(new Scene(root, 800, 600));
            
            // 显示新窗口
            newStage.show();
            
        } catch (IOException e) {
            showErrorDialog("新建窗口失败", "无法创建新窗口：" + e.getMessage());
        }
    }
    
    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开文件");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("文本文件", "*.txt"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        Stage stage = (Stage) menuBar.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            openFileInNewTab(selectedFile);
            addToRecentFiles(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleRecentFile1() {
        showRecentFilesDialog();
    }
    
    @FXML
    private void handleSave() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.save();
        }
    }
    
    @FXML
    private void handleSaveAs() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.saveAs();
        }
    }
    
    @FXML
    private void handleSaveAll() {
        // 保存所有标签页
        boolean allSaved = true;
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof DocumentTab) {
                DocumentTab docTab = (DocumentTab) tab;
                if (docTab.hasUnsavedChanges()) {
                    if (!docTab.save()) {
                        allSaved = false;
                    }
                }
            }
        }
        
        if (allSaved) {
            showInfoDialog("保存完成", "所有文档已保存。");
        } else {
            showErrorDialog("保存失败", "部分文档保存失败。");
        }
    }
    
    @FXML
    private void handlePageSetup() {
        showPageSetupDialog();
    }
    
    @FXML
    private void handlePrint() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            printDocument(currentTab);
        }
    }
    
    @FXML
    private void handleCloseWindow() {
        // 关闭整个窗口（等同于退出）
        handleExit();
    }
    
    @FXML
    private void handleExit() {
        // 检查所有标签页是否有未保存的更改
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof DocumentTab) {
                DocumentTab docTab = (DocumentTab) tab;
                if (docTab.hasUnsavedChanges()) {
                    tabPane.getSelectionModel().select(tab);
                    if (!docTab.promptSaveChanges()) {
                        return; // 用户取消了保存操作
                    }
                }
            }
        }
        
        // 保存最近使用文件列表
        saveRecentFiles();
        
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.close();
    }
    
    // 编辑菜单处理器
    @FXML
    private void handleUndo() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.undo();
        }
    }
    
    @FXML
    private void handleRedo() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.redo();
        }
    }
    
    @FXML
    private void handleCut() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.cut();
        }
    }
    
    @FXML
    private void handleCopy() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.copy();
        }
    }
    
    @FXML
    private void handlePaste() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.paste();
        }
    }
    
    @FXML
    private void handleDelete() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.delete();
        }
    }
    
    @FXML
    private void handleSelectAll() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.selectAll();
        }
    }
        @FXML
    private void handleFind() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            showFindDialog(currentTab);
        }
    }
    
    @FXML
    private void handleFindNext() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.findNext();
        }
    }
    
    @FXML
    private void handleFindPrevious() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.findPrevious();
        }
    }
    
    @FXML
    private void handleReplace() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            showReplaceDialog(currentTab);
        }
    }
    
    @FXML
    private void handleGoTo() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            showGoToDialog(currentTab);
        }
    }
    
    @FXML
    private void handleTimeDate() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.insertTimeDate();
        }
    }
    
    @FXML
    private void handleBingDefine() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            String selectedText = currentTab.getTextArea().getSelectedText();
            if (selectedText != null && !selectedText.trim().isEmpty()) {
                try {
                    String url = "https://www.bing.com/search?q=define+" + 
                               java.net.URLEncoder.encode(selectedText.trim(), "UTF-8");
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                } catch (Exception e) {
                    showErrorDialog("打开浏览器失败", "无法打开浏览器进行搜索：" + e.getMessage());
                }
            } else {
                showInfoDialog("使用必应进行定义", "请先选择要定义的文本。");
            }
        }
    }
    // 查看菜单处理器
    @FXML
    private void handleWordWrap() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.toggleWordWrap();
        }
    }
    
    @FXML
    private void handleFont() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            showFontDialog(currentTab);
        }
    }
    
    @FXML
    private void handleZoomIn() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.zoomIn();
        }
    }
    
    @FXML
    private void handleZoomOut() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.zoomOut();
        }
    }
    
    @FXML
    private void handleRestoreDefaultZoom() {
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null) {
            currentTab.restoreDefaultZoom();
        }
    }
    
    @FXML
    private void handleStatusBar() {
        if (statusBar != null && statusBarMenuItem != null) {
            boolean isVisible = statusBar.isVisible();
            statusBar.setVisible(!isVisible);
            statusBarMenuItem.setSelected(!isVisible);
        }
    }
    
    // 字体选择对话框
    private void showFontDialog(DocumentTab docTab) {
        Dialog<Pair<String, Double>> dialog = new Dialog<>();
        dialog.setTitle("字体");
        dialog.setHeaderText("选择字体和大小");
        
        // 设置按钮类型
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
        
        // 创建字体选择界面
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // 字体家族选择
        ComboBox<String> fontFamily = new ComboBox<>();
        fontFamily.getItems().addAll(
            "Arial", "Times New Roman", "Courier New", "Verdana", 
            "Tahoma", "Georgia", "Comic Sans MS", "Impact",
            "Microsoft YaHei", "SimSun", "SimHei", "KaiTi"
        );
        
        // 获取当前字体
        TextArea textArea = docTab.getTextArea();
        Font currentFont = textArea.getFont();
        String currentFontFamily = currentFont.getFamily();
        double currentFontSize = currentFont.getSize();
        
        fontFamily.setValue(currentFontFamily);
        
        // 字体大小选择
        ComboBox<Double> fontSize = new ComboBox<>();
        fontSize.getItems().addAll(
            8.0, 9.0, 10.0, 11.0, 12.0, 14.0, 16.0, 18.0, 
            20.0, 22.0, 24.0, 26.0, 28.0, 36.0, 48.0, 72.0
        );
        fontSize.setValue(currentFontSize);
        fontSize.setEditable(true);
        
        // 字体样式选择
        CheckBox boldCheckBox = new CheckBox("粗体");
        CheckBox italicCheckBox = new CheckBox("斜体");
        
        // 预览标签
        Label previewLabel = new Label("AaBbCc 中文预览");
        previewLabel.setStyle("-fx-border-color: gray; -fx-padding: 10; -fx-min-width: 200; -fx-min-height: 50;");
        
        // 更新预览的方法
        Runnable updatePreview = () -> {
            String family = fontFamily.getValue();
            Double size = fontSize.getValue();
            if (family != null && size != null) {
                String style = "";
                if (boldCheckBox.isSelected()) style += "-fx-font-weight: bold; ";
                if (italicCheckBox.isSelected()) style += "-fx-font-style: italic; ";
                
                previewLabel.setStyle(
                    "-fx-font-family: '" + family + "'; " +
                    "-fx-font-size: " + size + "px; " +
                    style +
                    "-fx-border-color: gray; -fx-padding: 10; -fx-min-width: 200; -fx-min-height: 50;"
                );
            }
        };
        
        // 添加监听器
        fontFamily.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        fontSize.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        boldCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        italicCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        
        // 初始化预览
        updatePreview.run();
        
        grid.add(new Label("字体:"), 0, 0);
        grid.add(fontFamily, 1, 0);
        grid.add(new Label("大小:"), 0, 1);
        grid.add(fontSize, 1, 1);
        grid.add(new Label("样式:"), 0, 2);
        
        VBox styleBox = new VBox(5);
        styleBox.getChildren().addAll(boldCheckBox, italicCheckBox);
        grid.add(styleBox, 1, 2);
        
        grid.add(new Label("预览:"), 0, 3);
        grid.add(previewLabel, 1, 3, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // 处理结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                String family = fontFamily.getValue();
                Double size = fontSize.getValue();
                if (family != null && size != null) {
                    return new Pair<>(family, size);
                }
            }
            return null;
        });
        
        Optional<Pair<String, Double>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String family = pair.getKey();
            Double size = pair.getValue();
            
            // 应用字体到当前标签页
            String style = "-fx-font-family: '" + family + "'; -fx-font-size: " + size + "px;";
            if (boldCheckBox.isSelected()) style += " -fx-font-weight: bold;";
            if (italicCheckBox.isSelected()) style += " -fx-font-style: italic;";
            
            textArea.setStyle(style);
            docTab.setFontSize(size);
            
            updateStatusBar();
        });
    }
    
    // 更新状态栏信息
    private void updateStatusBar() {
        // 添加空值检查
        if (statusBar == null) {
            return;
        }
        
        DocumentTab currentTab = getCurrentDocumentTab();
        if (currentTab != null && statusBar.isVisible()) {
            TextArea textArea = currentTab.getTextArea();
            
            // 更新位置信息
            int caretPosition = textArea.getCaretPosition();
            String text = textArea.getText();
            
            int line = 1;
            int column = 1;
            
            for (int i = 0; i < caretPosition && i < text.length(); i++) {
                if (text.charAt(i) == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
            }
            
            positionLabel.setText("行 " + line + ", 列 " + column);
            
            // 更新缩放信息
            double fontSize = currentTab.getFontSize();
            int zoomPercent = (int) Math.round((fontSize / 14.0) * 100);
            zoomLabel.setText(zoomPercent + "%");
            
            // 更新状态
            if (currentTab.hasUnsavedChanges()) {
                statusLabel.setText("已修改");
            } else {
                statusLabel.setText("就绪");
            }
        }
    }
    
    // 帮助菜单处理器
    @FXML
    private void handleViewHelp() {
        showInfoDialog("帮助", "记事本帮助\n\n快捷键：\nCtrl+N: 新建\nCtrl+O: 打开\nCtrl+S: 保存\nCtrl+Shift+S: 另存为\nCtrl+W: 关闭标签页\nCtrl+Q: 退出");
    }
    
    @FXML
    private void handleSendFeedback() {
        showInfoDialog("发送反馈", "感谢您的反馈！\n\n请将您的建议发送至：feedback@notepad.com");
    }
    
    @FXML
    private void handleAboutNotepad() {
        showInfoDialog("关于记事本", "JavaFX 记事本\n版本 1.0\n\n一个简单而强大的文本编辑器。");
    }
    
    // 标签页相关处理器
    @FXML
    private void handleCloseTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof DocumentTab) {
            DocumentTab docTab = (DocumentTab) selectedTab;
            if (docTab.hasUnsavedChanges()) {
                if (!docTab.promptSaveChanges()) {
                    return; // 用户取消了保存操作
                }
            }
            tabPane.getTabs().remove(selectedTab);
            
            // 如果没有标签页了，创建一个新的
            if (tabPane.getTabs().isEmpty()) {
                createNewTab();
            }
        }
    }
    
    @FXML
    private void handleSettings() {
        if (settingsView != null && mainView != null) {
            showSettings();
            }
        // else {
        //     showSettingsDialog();
        // }
    }
    
    // 显示设置界面
    @FXML
    private void showSettings() {
        if (mainView != null && settingsView != null) {
            mainView.setVisible(false);
            settingsView.setVisible(true);
        }
    }
    
    // 显示主界面
    @FXML
    private void showMainView() {
        if (settingsView != null && mainView != null) {
            settingsView.setVisible(false);
            mainView.setVisible(true);
        }
    }
    
    // 应用设置
    @FXML
    private void handleApply() {
        // 应用主题设置
        applyTheme();
        
        // 应用字体设置
        applyFontSettings();
        
        // 返回主界面
        showMainView();
    }
    
    // 重置设置
    @FXML
    private void handleReset() {
        if (themeComboBox != null) themeComboBox.setValue("浅色");
        if (fontFamilyComboBox != null) fontFamilyComboBox.setValue("微软雅黑");
        if (fontSizeSpinner != null) fontSizeSpinner.getValueFactory().setValue(12);
        if (boldCheckBox != null) boldCheckBox.setSelected(false);
        if (italicCheckBox != null) italicCheckBox.setSelected(false);
        if (applyToAllTabsCheckBox != null) applyToAllTabsCheckBox.setSelected(false);
        if (encodingComboBox != null) encodingComboBox.setValue("UTF-8");
        if (autoSaveCheckBox != null) autoSaveCheckBox.setSelected(false);
        if (timeFormatComboBox != null) timeFormatComboBox.setValue("yyyy-MM-dd HH:mm:ss");
    }
    
    private void applyTheme() {
        if (themeComboBox == null) return;
        
        String selectedTheme = themeComboBox.getValue();
        Scene scene = tabPane.getScene();
        
        if (scene != null) {
            // 清除现有样式表
            scene.getStylesheets().clear();
            
            // 根据选择的主题应用对应的CSS
            try {
                switch (selectedTheme) {
                    case "深色":
                        URL darkTheme = getClass().getResource("/org/example/notepad1/dark-theme.css");
                        if (darkTheme != null) {
                            scene.getStylesheets().add(darkTheme.toExternalForm());
                        }
                        break;
                    case "浅色":
                        URL lightTheme = getClass().getResource("/org/example/notepad1/light-theme.css");
                        if (lightTheme != null) {
                            scene.getStylesheets().add(lightTheme.toExternalForm());
                        }
                        break;
                    case "跟随系统":
                        // 暂时默认使用浅色主题
                        URL systemTheme = getClass().getResource("/org/example/notepad1/light-theme.css");
                        if (systemTheme != null) {
                            scene.getStylesheets().add(systemTheme.toExternalForm());
                        }
                        break;
                }
            } catch (Exception e) {
                System.err.println("应用主题失败: " + e.getMessage());
            }
        }
    }
    
    private void applyFontSettings() {
        if (fontFamilyComboBox == null || fontSizeSpinner == null) return;
        
        String fontFamily = fontFamilyComboBox.getValue();
        int fontSize = fontSizeSpinner.getValue();
        boolean bold = boldCheckBox != null && boldCheckBox.isSelected();
        boolean italic = italicCheckBox != null && italicCheckBox.isSelected();
        boolean applyToAll = applyToAllTabsCheckBox != null && applyToAllTabsCheckBox.isSelected();
        
        if (applyToAll) {
            // 应用到所有标签页
            applyFontToAllTabs(fontFamily, fontSize, bold, italic);
        } else {
            // 只应用到当前活动的标签页
            applyFontToCurrentTab(fontFamily, fontSize, bold, italic);
        }
    }
    
    // 应用字体设置到当前标签页
    public void applyFontToCurrentTab(String fontFamily, int fontSize, boolean bold, boolean italic) {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof DocumentTab) {
            DocumentTab docTab = (DocumentTab) selectedTab;
            docTab.setFont(fontFamily, fontSize, bold, italic);
        }
    }
    
    // 应用字体设置到所有标签页
    public void applyFontToAllTabs(String fontFamily, int fontSize, boolean bold, boolean italic) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof DocumentTab) {
                DocumentTab docTab = (DocumentTab) tab;
                docTab.setFont(fontFamily, fontSize, bold, italic);
            }
        }
    }
    
    // // 新增完整的设置窗口方法
    // private void showSettingsDialog() {
    //     try {
    //         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/notepad1/settings.fxml"));
    //         Parent root = loader.load();
            
    //         SettingsController settingsController = loader.getController();
    //         settingsController.setMainScene(tabPane.getScene());
    //         settingsController.setMainController(this); // 传递控制器引用
            
    //         Stage stage = new Stage();
    //         stage.setTitle("设置q");
    //         stage.setScene(new Scene(root));
    //         stage.initModality(Modality.APPLICATION_MODAL);
    //         stage.setResizable(false);
    //         stage.showAndWait();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
    
    // 页面设置对话框
    private void showPageSetupDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("页面设置");
        dialog.setHeaderText("设置页面打印参数");
        
        // 设置按钮类型
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
        
        // 创建页面设置界面
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // 纸张大小
        ComboBox<String> paperSize = new ComboBox<>();
        paperSize.getItems().addAll("A4", "A3", "Letter", "Legal");
        paperSize.setValue("A4");
        
        // 方向
        RadioButton portrait = new RadioButton("纵向");
        RadioButton landscape = new RadioButton("横向");
        ToggleGroup orientation = new ToggleGroup();
        portrait.setToggleGroup(orientation);
        landscape.setToggleGroup(orientation);
        portrait.setSelected(true);
        
        // 边距设置
        TextField leftMargin = new TextField("20");
        TextField rightMargin = new TextField("20");
        TextField topMargin = new TextField("25");
        TextField bottomMargin = new TextField("25");
        
        // 页眉页脚
        TextField header = new TextField();
        TextField footer = new TextField();
        
        grid.add(new Label("纸张:"), 0, 0);
        grid.add(new Label("大小:"), 0, 1);
        grid.add(paperSize, 1, 1);
        
        grid.add(new Label("方向:"), 0, 2);
        VBox orientationBox = new VBox(5);
        orientationBox.getChildren().addAll(portrait, landscape);
        grid.add(orientationBox, 1, 2);
        
        grid.add(new Label("边距:"), 0, 3);
        grid.add(new Label("左:"), 0, 4);
        grid.add(leftMargin, 1, 4);
        grid.add(new Label("右:"), 2, 4);
        grid.add(rightMargin, 3, 4);
        grid.add(new Label("上:"), 0, 5);
        grid.add(topMargin, 1, 5);
        grid.add(new Label("下:"), 2, 5);
        grid.add(bottomMargin, 3, 5);
        
        grid.add(new Label("页眉:"), 0, 6);
        grid.add(header, 1, 6, 3, 1);
        grid.add(new Label("页脚:"), 0, 7);
        grid.add(footer, 1, 7, 3, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // 处理结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new Pair<>("页面设置已保存", "设置已应用");
            }
            return null;
        });
        
        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> showInfoDialog(pair.getKey(), pair.getValue()));
    }
    
    // 打印功能
    private void printDocument(DocumentTab docTab) {
        try {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob != null && printerJob.showPrintDialog(menuBar.getScene().getWindow())) {
                boolean success = printerJob.printPage(docTab.getTextArea());
                if (success) {
                    printerJob.endJob();
                    showInfoDialog("打印", "文档已发送到打印机。");
                } else {
                    showErrorDialog("打印失败", "无法打印文档。");
                }
            }
        } catch (Exception e) {
            showErrorDialog("打印错误", "打印时发生错误：" + e.getMessage());
        }
    }
    
    // 最近使用文件对话框
    private void showRecentFilesDialog() {
        if (recentFiles.isEmpty()) {
            showInfoDialog("最近使用", "没有最近使用的文件。");
            return;
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("最近使用的文件");
        dialog.setHeaderText("选择要打开的文件");
        
        ButtonType openButtonType = new ButtonType("打开", ButtonBar.ButtonData.OK_DONE);
        ButtonType clearButtonType = new ButtonType("清除列表", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(openButtonType, clearButtonType, cancelButtonType);
        
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(recentFiles);
        listView.setPrefHeight(200);
        
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("最近使用的文件:"), listView);
        vbox.setPadding(new Insets(10));
        
        dialog.getDialogPane().setContent(vbox);
        
        // 处理按钮点击
        Button clearButton = (Button) dialog.getDialogPane().lookupButton(clearButtonType);
        clearButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认清除");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("确定要清除最近使用文件列表吗？");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                recentFiles.clear();
                listView.getItems().clear();
                saveRecentFiles();
                showInfoDialog("清除完成", "最近使用文件列表已清除。");
                dialog.close();
            }
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == openButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(filePath -> {
            File file = new File(filePath);
            if (file.exists()) {
                openFileInNewTab(file);
            } else {
                showErrorDialog("文件不存在", "文件 " + filePath + " 不存在。");
                recentFiles.remove(filePath);
                saveRecentFiles();
            }
        });
    }
    
    // 最近使用文件管理（使用Properties文件）
    private void addToRecentFiles(String filePath) {
        recentFiles.remove(filePath); // 移除重复项
        recentFiles.add(0, filePath); // 添加到开头
        
        // 限制列表大小
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles = recentFiles.subList(0, MAX_RECENT_FILES);
        }
        
        saveRecentFiles();
    }
    
    // 加载最近使用文件列表
    private void loadRecentFiles() {
        try {
            File configFile = new File(RECENT_FILES_CONFIG);
            if (configFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                
                for (int i = 0; i < MAX_RECENT_FILES; i++) {
                    String filePath = props.getProperty("recentFile" + i);
                    if (filePath != null && !filePath.trim().isEmpty()) {
                        recentFiles.add(filePath); // 不检查文件是否存在，直接加载
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("加载最近使用文件列表失败: " + e.getMessage());
        }
    }
    
    // 保存最近使用文件列表
    private void saveRecentFiles() {
        try {
            Properties props = new Properties();
            
            // 保存当前列表
            for (int i = 0; i < recentFiles.size(); i++) {
                props.setProperty("recentFile" + i, recentFiles.get(i));
            }
            
            try (FileOutputStream fos = new FileOutputStream(RECENT_FILES_CONFIG)) {
                props.store(fos, "Recent Files Configuration");
            }
        } catch (IOException e) {
            System.err.println("保存最近使用文件列表失败: " + e.getMessage());
        }
    }
    
    // 辅助方法
    private void createNewTab() {
        DocumentTab newTab = new DocumentTab("无标题" + untitledCounter++);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }
    
    private void openFileInNewTab(File file) {
        try {
            String content = Files.readString(file.toPath());
            DocumentTab newTab = new DocumentTab(file.getName(), file, content);
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
        } catch (IOException e) {
            showErrorDialog("打开文件失败", "无法打开文件: " + file.getName() + "\n\n" + e.getMessage());
        }
    }
    
    private DocumentTab getCurrentDocumentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof DocumentTab) {
            return (DocumentTab) selectedTab;
        }
        return null;
    }
    
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


// 查找对话框
private void showFindDialog(DocumentTab docTab) {
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("查找");
    dialog.setHeaderText("查找内容");
    
    ButtonType findButtonType = new ButtonType("查找下一个", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(findButtonType, cancelButtonType);
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    
    TextField findField = new TextField();
    findField.setPromptText("输入要查找的文本");
    
    CheckBox matchCase = new CheckBox("区分大小写");
    CheckBox wholeWord = new CheckBox("全字匹配");
    
    grid.add(new Label("查找内容:"), 0, 0);
    grid.add(findField, 1, 0);
    grid.add(matchCase, 0, 1, 2, 1);
    grid.add(wholeWord, 0, 2, 2, 1);
    
    dialog.getDialogPane().setContent(grid);
    
    Platform.runLater(() -> findField.requestFocus());
    
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == findButtonType) {
            return findField.getText();
        }
        return null;
    });
    
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(searchText -> {
        if (!searchText.trim().isEmpty()) {
            docTab.setSearchText(searchText, matchCase.isSelected(), wholeWord.isSelected());
            docTab.findNext();
        }
    });
}

// 替换对话框
private void showReplaceDialog(DocumentTab docTab) {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("替换");
    dialog.setHeaderText("查找和替换");
    
    ButtonType replaceButtonType = new ButtonType("替换", ButtonBar.ButtonData.OK_DONE);
    ButtonType replaceAllButtonType = new ButtonType("全部替换", ButtonBar.ButtonData.OTHER);
    ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(replaceButtonType, replaceAllButtonType, cancelButtonType);
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    
    TextField findField = new TextField();
    findField.setPromptText("输入要查找的文本");
    
    TextField replaceField = new TextField();
    replaceField.setPromptText("输入替换文本");
    
    CheckBox matchCase = new CheckBox("区分大小写");
    CheckBox wholeWord = new CheckBox("全字匹配");
    
    grid.add(new Label("查找内容:"), 0, 0);
    grid.add(findField, 1, 0);
    grid.add(new Label("替换为:"), 0, 1);
    grid.add(replaceField, 1, 1);
    grid.add(matchCase, 0, 2, 2, 1);
    grid.add(wholeWord, 0, 3, 2, 1);
    
    dialog.getDialogPane().setContent(grid);
    
    Platform.runLater(() -> findField.requestFocus());
    
    // 处理全部替换按钮
    Button replaceAllButton = (Button) dialog.getDialogPane().lookupButton(replaceAllButtonType);
    replaceAllButton.addEventFilter(ActionEvent.ACTION, event -> {
        event.consume();
        String findText = findField.getText();
        String replaceText = replaceField.getText();
        if (!findText.trim().isEmpty()) {
            int count = docTab.replaceAll(findText, replaceText, matchCase.isSelected(), wholeWord.isSelected());
            showInfoDialog("替换完成", "已替换 " + count + " 处内容。");
            dialog.close();
        }
    });
    
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == replaceButtonType) {
            return new Pair<>(findField.getText(), replaceField.getText());
        }
        return null;
    });
    
    Optional<Pair<String, String>> result = dialog.showAndWait();
    result.ifPresent(pair -> {
        String findText = pair.getKey();
        String replaceText = pair.getValue();
        if (!findText.trim().isEmpty()) {
            docTab.setSearchText(findText, matchCase.isSelected(), wholeWord.isSelected());
            docTab.replaceNext(replaceText);
        }
    });
}

// 转到对话框
private void showGoToDialog(DocumentTab docTab) {
    Dialog<Integer> dialog = new Dialog<>();
    dialog.setTitle("转到");
    dialog.setHeaderText("转到指定行");
    
    ButtonType goButtonType = new ButtonType("转到", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(goButtonType, cancelButtonType);
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    
    TextField lineField = new TextField();
    lineField.setPromptText("输入行号");
    
    // 显示当前行号和总行数
    TextArea textArea = docTab.getTextArea();
    String text = textArea.getText();
    int totalLines = text.split("\\n", -1).length;
    int currentLine = text.substring(0, textArea.getCaretPosition()).split("\\n", -1).length;
    
    Label infoLabel = new Label("当前行: " + currentLine + ", 总行数: " + totalLines);
    
    grid.add(new Label("行号:"), 0, 0);
    grid.add(lineField, 1, 0);
    grid.add(infoLabel, 0, 1, 2, 1);
    
    dialog.getDialogPane().setContent(grid);
    
    Platform.runLater(() -> lineField.requestFocus());
    
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == goButtonType) {
            try {
                return Integer.parseInt(lineField.getText());
            } catch (NumberFormatException e) {
                showErrorDialog("无效行号", "请输入有效的行号。");
                return null;
            }
        }
        return null;
    });
    
    Optional<Integer> result = dialog.showAndWait();
    result.ifPresent(lineNumber -> {
        if (lineNumber > 0 && lineNumber <= totalLines) {
            docTab.goToLine(lineNumber);
        } else {
            showErrorDialog("行号超出范围", "行号必须在 1 到 " + totalLines + " 之间。");
        }
    });
}
}
