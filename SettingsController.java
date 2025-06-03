package org.example.notepad1;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.Scene;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> fontFamilyComboBox;
    @FXML private Spinner<Integer> fontSizeSpinner;
    @FXML private CheckBox boldCheckBox;
    @FXML private CheckBox italicCheckBox;
    @FXML private ComboBox<String> encodingComboBox;
    @FXML private CheckBox autoSaveCheckBox;
    @FXML private ComboBox<String> timeFormatComboBox;
    @FXML private CheckBox applyToAllTabsCheckBox; // 新添加的复选框
    @FXML private Button cancelButton;
    @FXML private Button applyButton;
    
    private Scene mainScene; // 主窗口场景引用
    private NotepadController mainController; // 主控制器引用
    
    // 设置主窗口场景引用的方法
    public void setMainScene(Scene scene) {
        this.mainScene = scene;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化主题选项
        themeComboBox.setItems(FXCollections.observableArrayList(
            "浅色", "深色", "跟随系统"
        ));
        
        // 初始化字体列表
        fontFamilyComboBox.setItems(FXCollections.observableArrayList(
            "微软雅黑", "宋体", "黑体", "楷体", "仿宋", "Arial", "Times New Roman", "Courier New"
        ));
        
        // 初始化编码选项
        encodingComboBox.setItems(FXCollections.observableArrayList(
            "UTF-8", "GBK", "ASCII"
        ));
        
        // 初始化时间格式选项
        timeFormatComboBox.setItems(FXCollections.observableArrayList(
            "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy HH:mm", "dd/MM/yyyy HH:mm"
        ));
        
        // 初始化字体大小Spinner
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 72, 12));
        
        // 设置默认值
        themeComboBox.setValue("浅色");
        fontFamilyComboBox.setValue("微软雅黑");
        encodingComboBox.setValue("UTF-8");
        timeFormatComboBox.setValue("yyyy-MM-dd HH:mm:ss");
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleApply() {
        // 应用主题设置
        applyTheme();
        
        // 应用字体设置
        applyFontSettings();
        
        Stage stage = (Stage) applyButton.getScene().getWindow();
        stage.close();
    }
    
    private void applyFontSettings() {
        if (mainController != null) {
            String fontFamily = fontFamilyComboBox.getValue();
            int fontSize = fontSizeSpinner.getValue();
            boolean bold = boldCheckBox.isSelected();
            boolean italic = italicCheckBox.isSelected();
            boolean applyToAll = applyToAllTabsCheckBox.isSelected();
            
            if (applyToAll) {
                // 应用到所有标签页
                mainController.applyFontToAllTabs(fontFamily, fontSize, bold, italic);
            } else {
                // 只应用到当前活动的标签页
                mainController.applyFontToCurrentTab(fontFamily, fontSize, bold, italic);
            }
        }
    }
    
    // 设置主控制器引用的方法
    public void setMainController(NotepadController controller) {
        this.mainController = controller;
    }
    
    private void applyTheme() {
        if (mainScene != null) {
            String selectedTheme = themeComboBox.getValue();
            
            // 清除现有样式表
            mainScene.getStylesheets().clear();
            
            // 根据选择的主题应用对应的CSS
            switch (selectedTheme) {
                case "深色":
                    mainScene.getStylesheets().add(
                        getClass().getResource("/org/example/notepad1/dark-theme.css").toExternalForm()
                    );
                    break;
                case "浅色":
                    mainScene.getStylesheets().add(
                        getClass().getResource("/org/example/notepad1/light-theme.css").toExternalForm()
                    );
                    break;
                case "跟随系统":
                    // 可以根据系统主题来决定使用哪个主题
                    // 这里暂时默认使用浅色主题
                    mainScene.getStylesheets().add(
                        getClass().getResource("/org/example/notepad1/light-theme.css").toExternalForm()
                    );
                    break;
            }
        }
    }
}