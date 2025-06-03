package org.example.notepad1;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Stack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DocumentTab extends Tab {
    private TextArea textArea;
    private File file;
    private boolean hasUnsavedChanges;
    private String originalTitle;
    private double fontSize = 14.0;
    private String fontFamily = "微软雅黑";
    private boolean isBold = false;
    private boolean isItalic = false;
    
    // 撤销/重做功能
    private Stack<String> undoStack = new Stack<>();
    private Stack<String> redoStack = new Stack<>();
    private String lastSavedState = "";
    
    // 查找功能
    private String searchText = "";
    private boolean matchCase = false;
    private boolean wholeWord = false;
    private int lastSearchIndex = -1;
    
    // 构造函数 - 新文档
    public DocumentTab(String title) {
        this(title, null, "");
    }
    
    // 构造函数 - 打开现有文件
    public DocumentTab(String title, File file, String content) {
        super();
        this.file = file;
        this.originalTitle = title;
        this.hasUnsavedChanges = false;
        
        initializeTab(content);
        updateTitle();
    }
    
    private void initializeTab(String content) {
        // 创建文本区域
        textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: " + fontSize + "px;");
        
        // 初始化撤销栈
        lastSavedState = content;
        undoStack.push(content);
        
        // 监听文本变化
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!hasUnsavedChanges) {
                hasUnsavedChanges = true;
                updateTitle();
            }
            
            // 添加到撤销栈
            if (!newValue.equals(oldValue)) {
                undoStack.push(oldValue);
                redoStack.clear(); // 清空重做栈
                
                // 限制撤销栈大小
                if (undoStack.size() > 100) {
                    undoStack.remove(0);
                }
            }
        });
        
        // 设置标签页内容
        setContent(textArea);
        
        // 设置关闭请求处理
        setOnCloseRequest(event -> {
            if (hasUnsavedChanges) {
                if (!promptSaveChanges()) {
                    event.consume(); // 取消关闭操作
                }
            }
        });
    }
    
    // 获取文本内容
    public String getTextContent() {
        return textArea.getText();
    }
    
    // 设置文本内容
    public void setTextContent(String content) {
        textArea.setText(content);
        hasUnsavedChanges = false;
        updateTitle();
    }
    
    // 保存文件
    public boolean save() {
        if (file == null) {
            return saveAs();
        }
        
        try {
            Files.writeString(file.toPath(), textArea.getText());
            hasUnsavedChanges = false;
            updateTitle();
            return true;
        } catch (IOException e) {
            showErrorDialog("保存失败", "无法保存文件: " + file.getName() + "\n\n" + e.getMessage());
            return false;
        }
    }
    
    // 另存为
    public boolean saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("另存为");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("文本文件", "*.txt"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        if (file != null) {
            fileChooser.setInitialDirectory(file.getParentFile());
            fileChooser.setInitialFileName(file.getName());
        }
        
        Stage stage = (Stage) textArea.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);
        
        if (selectedFile != null) {
            this.file = selectedFile;
            this.originalTitle = selectedFile.getName();
            return save();
        }
        
        return false;
    }
    
    // 检查是否有未保存的更改
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    // 提示保存更改
    public boolean promptSaveChanges() {
        if (!hasUnsavedChanges) {
            return true;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("保存更改");
        alert.setHeaderText("是否保存对 " + originalTitle + " 的更改？");
        alert.setContentText("如果不保存，更改将丢失。");
        
        ButtonType saveButton = new ButtonType("保存");
        ButtonType dontSaveButton = new ButtonType("不保存");
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent()) {
            if (result.get() == saveButton) {
                return save();
            } else if (result.get() == dontSaveButton) {
                return true;
            }
        }
        
        return false; // 用户取消
    }
    
    // 编辑操作
    public void cut() {
        textArea.cut();
    }
    
    public void copy() {
        textArea.copy();
    }
    
    public void paste() {
        textArea.paste();
    }
    
    public void delete() {
        textArea.deleteText(textArea.getSelection());
    }
    
    public void selectAll() {
        textArea.selectAll();
    }
    
    // 查看操作
    public void toggleWordWrap() {
        textArea.setWrapText(!textArea.isWrapText());
    }
    
    public void zoomIn() {
        fontSize = Math.min(fontSize + 2, 72);
        updateFontSize();
    }
    
    public void zoomOut() {
        fontSize = Math.max(fontSize - 2, 8);
        updateFontSize();
    }
    
    public void restoreDefaultZoom() {
        fontSize = 14.0;
        updateFontSize();
    }
    
    private void updateFontSize() {
        textArea.setStyle("-fx-font-size: " + fontSize + "px;");
    }
    
    private void updateTitle() {
        String title = originalTitle;
        if (hasUnsavedChanges) {
            title = "● " + title;
        }
        setText(title);
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Getter方法
    public File getFile() {
        return file;
    }
    
    // 在DocumentTab类中添加以下方法
    
    public double getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
    
    public TextArea getTextArea() {
        return textArea;
    }
    
    // 撤销功能
    public void undo() {
        if (undoStack.size() > 1) {
            String currentText = textArea.getText();
            redoStack.push(currentText);
            undoStack.pop(); // 移除当前状态
            String previousText = undoStack.peek();
            
            // 临时移除监听器以避免无限循环
            textArea.textProperty().removeListener((observable, oldValue, newValue) -> {
                // 监听器逻辑
            });
            
            textArea.setText(previousText);
            
            // 重新添加监听器
            textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!hasUnsavedChanges) {
                    hasUnsavedChanges = true;
                    updateTitle();
                }
            });
        }
    }
    
    // 重做功能
    public void redo() {
        if (!redoStack.isEmpty()) {
            String redoText = redoStack.pop();
            undoStack.push(textArea.getText());
            
            // 临时移除监听器以避免无限循环
            textArea.textProperty().removeListener((observable, oldValue, newValue) -> {
                // 监听器逻辑
            });
            
            textArea.setText(redoText);
            
            // 重新添加监听器
            textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!hasUnsavedChanges) {
                    hasUnsavedChanges = true;
                    updateTitle();
                }
            });
        }
    }
    
    // 设置搜索参数
    public void setSearchText(String searchText, boolean matchCase, boolean wholeWord) {
        this.searchText = searchText;
        this.matchCase = matchCase;
        this.wholeWord = wholeWord;
        this.lastSearchIndex = -1;
    }
    
    // 查找下一个
    public void findNext() {
        if (searchText.isEmpty()) {
            return;
        }
        
        String text = textArea.getText();
        String searchPattern = searchText;
        
        if (!matchCase) {
            text = text.toLowerCase();
            searchPattern = searchPattern.toLowerCase();
        }
        
        if (wholeWord) {
            searchPattern = "\\b" + Pattern.quote(searchPattern) + "\\b";
            Pattern pattern = Pattern.compile(searchPattern, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(textArea.getText());
            
            int startPos = Math.max(0, textArea.getCaretPosition());
            if (matcher.find(startPos)) {
                textArea.selectRange(matcher.start(), matcher.end());
                lastSearchIndex = matcher.start();
                return;
            }
            
            // 从头开始搜索
            if (matcher.find(0)) {
                textArea.selectRange(matcher.start(), matcher.end());
                lastSearchIndex = matcher.start();
            } else {
                showInfoDialog("查找", "找不到 \"" + searchText + "\"");
            }
        } else {
            int startPos = Math.max(0, textArea.getCaretPosition());
            int index = text.indexOf(searchPattern, startPos);
            
            if (index == -1) {
                // 从头开始搜索
                index = text.indexOf(searchPattern, 0);
            }
            
            if (index != -1) {
                textArea.selectRange(index, index + searchText.length());
                lastSearchIndex = index;
            } else {
                showInfoDialog("查找", "找不到 \"" + searchText + "\"");
            }
        }
    }
    
    // 查找上一个
    public void findPrevious() {
        if (searchText.isEmpty()) {
            return;
        }
        
        String text = textArea.getText();
        String searchPattern = searchText;
        
        if (!matchCase) {
            text = text.toLowerCase();
            searchPattern = searchPattern.toLowerCase();
        }
        
        if (wholeWord) {
            searchPattern = "\\b" + Pattern.quote(searchPattern) + "\\b";
            Pattern pattern = Pattern.compile(searchPattern, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(textArea.getText());
            
            int endPos = Math.min(text.length(), textArea.getCaretPosition());
            int lastMatch = -1;
            
            while (matcher.find() && matcher.start() < endPos) {
                lastMatch = matcher.start();
            }
            
            if (lastMatch != -1) {
                matcher.find(lastMatch);
                textArea.selectRange(matcher.start(), matcher.end());
                lastSearchIndex = matcher.start();
            } else {
                // 从末尾开始搜索
                while (matcher.find()) {
                    lastMatch = matcher.start();
                }
                if (lastMatch != -1) {
                    matcher.find(lastMatch);
                    textArea.selectRange(matcher.start(), matcher.end());
                    lastSearchIndex = matcher.start();
                } else {
                    showInfoDialog("查找", "找不到 \"" + searchText + "\"");
                }
            }
        } else {
            int endPos = Math.min(text.length(), textArea.getCaretPosition());
            int index = text.lastIndexOf(searchPattern, endPos - 1);
            
            if (index == -1) {
                // 从末尾开始搜索
                index = text.lastIndexOf(searchPattern);
            }
            
            if (index != -1) {
                textArea.selectRange(index, index + searchText.length());
                lastSearchIndex = index;
            } else {
                showInfoDialog("查找", "找不到 \"" + searchText + "\"");
            }
        }
    }
    
    // 替换下一个
    public void replaceNext(String replaceText) {
        if (searchText.isEmpty()) {
            return;
        }
        
        String selectedText = textArea.getSelectedText();
        if (selectedText != null && selectedText.equals(searchText)) {
            textArea.replaceSelection(replaceText);
        }
        
        findNext();
    }
    
    // 全部替换
    public int replaceAll(String findText, String replaceText, boolean matchCase, boolean wholeWord) {
        String text = textArea.getText();
        String searchPattern = findText;
        int count = 0;
        
        if (wholeWord) {
            searchPattern = "\\b" + Pattern.quote(findText) + "\\b";
            Pattern pattern = Pattern.compile(searchPattern, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replaceText));
                count++;
            }
            matcher.appendTail(sb);
            textArea.setText(sb.toString());
        } else {
            if (!matchCase) {
                // 对于不区分大小写的替换，需要特殊处理
                Pattern pattern = Pattern.compile(Pattern.quote(findText), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(text);
                
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replaceText));
                    count++;
                }
                matcher.appendTail(sb);
                textArea.setText(sb.toString());
            } else {
                String newText = text.replace(findText, replaceText);
                count = (text.length() - newText.length()) / (findText.length() - replaceText.length());
                textArea.setText(newText);
            }
        }
        
        return count;
    }
    
    // 转到指定行
    public void goToLine(int lineNumber) {
        String text = textArea.getText();
        String[] lines = text.split("\\n", -1);
        
        if (lineNumber > 0 && lineNumber <= lines.length) {
            int position = 0;
            for (int i = 0; i < lineNumber - 1; i++) {
                position += lines[i].length() + 1; // +1 for newline character
            }
            
            textArea.positionCaret(position);
            textArea.requestFocus();
        }
    }
    
    // 插入时间日期
    public void insertTimeDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String timeDate = now.format(formatter);
        
        int caretPosition = textArea.getCaretPosition();
        textArea.insertText(caretPosition, timeDate);
    }
    
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    

// 设置字体
public void setFont(String fontFamily, double fontSize, boolean bold, boolean italic) {
    this.fontFamily = fontFamily;
    this.fontSize = fontSize;
    this.isBold = bold;
    this.isItalic = italic;
    updateFont();
}

// 更新字体样式
private void updateFont() {
    StringBuilder style = new StringBuilder();
    style.append("-fx-font-family: '").append(fontFamily).append("'; ");
    style.append("-fx-font-size: ").append(fontSize).append("px; ");
    
    if (isBold) {
        style.append("-fx-font-weight: bold; ");
    }
    
    if (isItalic) {
        style.append("-fx-font-style: italic; ");
    }
    
    textArea.setStyle(style.toString());
}


// Getter方法
public String getFontFamily() {
    return fontFamily;
}

public boolean isBold() {
    return isBold;
}

public boolean isItalic() {
    return isItalic;
}
}