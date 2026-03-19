package com.example.papelit.ui;

import com.example.papelit.model.Note;
import com.example.papelit.model.NoteColor;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Screen;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.util.Duration;
import java.time.LocalDateTime;

public class StickyNoteWindow extends Stage {
    private final Note note;
    private final DashboardWindow dashboard;

    private HTMLEditor htmlEditor;
    private TextField titleField;
    private ComboBox<String> tagCombo;
    private HBox formatBar;
    private VBox root;
    private WebEngine editorWebEngine;
    private boolean formatBarVisible = false;

    private static final double DEFAULT_WIDTH  = 400;
    private static final double DEFAULT_HEIGHT = 500;
    private static final double MIN_WIDTH = 350;
    private static final double MIN_HEIGHT = 400;

    public StickyNoteWindow(Note note, DashboardWindow dashboard) {
        this.note = note;
        this.dashboard = dashboard;

        initStyle(StageStyle.DECORATED);
        setTitle(note.getTitle().isBlank() ? "Untitled Note" : note.getTitle());
        setMinWidth(MIN_WIDTH);
        setMinHeight(MIN_HEIGHT);
        
        // Final Polish: Set the taskbar/dock icon
        try {
            var iconRes = getClass().getResource("/icon.png");
            if (iconRes != null) getIcons().add(new javafx.scene.image.Image(iconRes.toExternalForm()));
        } catch (Exception ignored) {}
        
        // 60-65% Max Height Cap based on screen resolution
        double screenH = Screen.getPrimary().getVisualBounds().getHeight();
        setMaxHeight(screenH * 0.65);

        double w = (note.getWidth()  > MIN_WIDTH)  ? note.getWidth()  : DEFAULT_WIDTH;
        double h = (note.getHeight() > MIN_HEIGHT) ? note.getHeight() : DEFAULT_HEIGHT;

        setupUI(w, h);

        if (note.getX() > 0 && note.getY() > 0) {
            setX(note.getX());
            setY(note.getY());
        }
        setAlwaysOnTop(note.isAlwaysOnTop());

        xProperty().addListener((obs, o, n2) -> note.setX(n2.doubleValue()));
        yProperty().addListener((obs, o, n2) -> note.setY(n2.doubleValue()));
        widthProperty().addListener((obs, o, n2) -> note.setWidth(n2.doubleValue()));
        heightProperty().addListener((obs, o, n2) -> note.setHeight(n2.doubleValue()));

        setOnCloseRequest(e -> {
            saveNoteContent();
            note.setOpen(false);
            dashboard.saveNote(note);
            // CHECK FOR EXIT: If this was the last window (even if dashboard is closed), shutdown.
            Platform.runLater(com.example.papelit.StickyNotesApp::checkWindowsAndExit);
        });
    }

    private void setupUI(double w, double h) {
        root = new VBox(0);
        root.getStyleClass().add("note-window-root");
        
        // Initial Background Style
        NoteColor nc = NoteColor.fromHex(note.getColor());
        root.getStyleClass().add(nc.getCssClassName());

        // --- TOP: Header + Toolbars ---
        HBox header = new HBox(12);
        header.setPadding(new Insets(16, 16, 0, 16));
        header.setAlignment(Pos.CENTER_LEFT);

        titleField = new TextField(note.getTitle());
        titleField.setPromptText("Note title...");
        titleField.getStyleClass().add("editor-title-field");
        titleField.textProperty().addListener((obs, o, nv) -> {
            setTitle(nv.isBlank() ? "Untitled Note" : nv);
            note.setTitle(nv);
        });
        HBox.setHgrow(titleField, Priority.ALWAYS);

        Button pinBtn = createToolbarBtn("M16 9v4l1 1v2h-4v4l-1 1-1-1v-4H7v-2l1-1V9c0-1.66 1.34-3 3-3V5a1 1 0 012 0v1c1.66 0 3 1.34 3 3z", "Pin");
        updatePinButtonStyle(pinBtn, note.isAlwaysOnTop());
        pinBtn.setOnAction(e -> {
            boolean pinned = !note.isAlwaysOnTop();
            note.setAlwaysOnTop(pinned);
            setAlwaysOnTop(pinned);
            updatePinButtonStyle(pinBtn, pinned);
            dashboard.saveNote(note);
        });

        Button deleteBtn = createToolbarBtn("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z", "Delete");
        deleteBtn.setOnAction(e -> { dashboard.deleteNote(note); close(); });

        header.getChildren().addAll(titleField, pinBtn, deleteBtn);

        formatBar = new HBox(8);
        formatBar.setPadding(new Insets(8, 16, 8, 16));
        formatBar.setAlignment(Pos.CENTER_LEFT);
        formatBar.getStyleClass().add("format-bar-overlay");

        Button boldBtn = createMiniBtn("M15.6 10.79c.97-.67 1.65-1.77 1.65-2.79 0-2.26-1.75-4-4-4H7v14h7.04c2.09 0 3.71-1.7 3.71-3.79 0-1.52-.86-2.82-2.15-3.42zM10 6.5h3c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5h-3v-3zm3.5 9H10v-3h3.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5z", "Bold");
        boldBtn.setOnAction(e -> exec("bold"));
        Button italicBtn = createMiniBtn("M10 4v3h2.21l-3.42 8H6v3h8v-3h-2.21l3.42-8H18V4h-8z", "Italic");
        italicBtn.setOnAction(e -> exec("italic"));

        HBox colorPicker = new HBox(12);
        colorPicker.setAlignment(Pos.CENTER_LEFT);
        for (NoteColor color : NoteColor.values()) {
            Button dot = new Button();
            dot.setPadding(Insets.EMPTY);
            dot.setMinWidth(22); dot.setMinHeight(22);
            dot.setPrefWidth(22); dot.setPrefHeight(22);
            dot.setStyle("-fx-background-color: " + color.getHex() + "; -fx-background-radius: 50%; -fx-cursor: hand; -fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 50%; -fx-border-width: 1.5;");
            dot.setOnAction(e -> applyNoteColor(color));
            colorPicker.getChildren().add(dot);
        }

        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        tagCombo = new ComboBox<>(FXCollections.observableArrayList("Home", "Work", "Personal", "Ideas"));
        tagCombo.setValue(note.getTag() != null ? note.getTag() : "Home");
        tagCombo.getStyleClass().add("mini-tag-combo");
        tagCombo.setOnAction(e -> { note.setTag(tagCombo.getValue()); dashboard.saveNote(note); });

        formatBar.getChildren().addAll(boldBtn, italicBtn, new Separator(), colorPicker, filler, tagCombo);

        // --- MIDDLE: HTMLEditor ---
        htmlEditor = new HTMLEditor();
        htmlEditor.getStyleClass().add("html-editor");
        htmlEditor.setHtmlText(note.getContent() != null ? note.getContent() : "");
        htmlEditor.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(htmlEditor, Priority.ALWAYS);
        
        Platform.runLater(() -> {
            for (Node n : htmlEditor.lookupAll(".tool-bar")) { n.setVisible(false); n.setManaged(false); }
            Node wvNode = htmlEditor.lookup("WebView");
            if (wvNode instanceof WebView) {
                WebView wv = (WebView) wvNode;
                editorWebEngine = wv.getEngine();
                wv.setStyle("-fx-background-color: transparent;");
                wv.setContextMenuEnabled(false);
                wv.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        applyNoteColor(NoteColor.fromHex(note.getColor()));
                    }
                });
            }
        });

        root.getChildren().addAll(header, formatBar, htmlEditor);

        Scene scene = new Scene(root, w, h);
        String theme = dashboard.getPreferences().getTheme();
        scene.getStylesheets().add(getClass().getResource("/style-" + theme + ".css").toExternalForm());
        setScene(scene);

        hideFormatBar(false);
        root.setOnMouseEntered(e -> showFormatBar());
        root.setOnMouseExited(e -> { if(!tagCombo.isFocused()) hideFormatBar(true); });
        
        Platform.runLater(() -> {
            if(note.getTitle().isBlank()) titleField.requestFocus(); else htmlEditor.requestFocus();
        });
    }

    public void applyNoteColor(NoteColor selectedColor) {
        if (root == null || selectedColor == null) return;
        
        // 1. Apply to root container (JavaFX)
        root.getStyleClass().removeIf(s -> s.startsWith("note-color-"));
        root.getStyleClass().add(selectedColor.getCssClassName());
        note.setColor(selectedColor.getHex());
        dashboard.saveNote(note);
        
        // 2. Inject CSS Style tag for permanent color matching (Master Prompt method)
        if (editorWebEngine != null) {
            String script = String.format(
                "var style = document.getElementById('noteEditorBackgroundColorStyle');" +
                "if (!style) {" +
                "  style = document.createElement('style');" +
                "  style.id = 'noteEditorBackgroundColorStyle';" +
                "  document.head.appendChild(style);" +
                "}" +
                "style.innerHTML = 'body { background-color: %s !important; }';" +
                "document.body.style.setProperty('background-color', '%s', 'important');",
                selectedColor.getHex(), selectedColor.getHex()
            );
            try {
                editorWebEngine.executeScript(script);
            } catch (Exception ignored) {}
        }
    }

    private void showFormatBar() {
        if (formatBarVisible) return;
        formatBarVisible = true;
        formatBar.setManaged(true);
        formatBar.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(150), formatBar);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void hideFormatBar(boolean animate) {
        if (!animate) {
            formatBar.setOpacity(0);
            formatBar.setManaged(false);
            formatBar.setVisible(false);
            formatBarVisible = false;
            return;
        }
        if (!formatBarVisible) return;
        FadeTransition ft = new FadeTransition(Duration.millis(200), formatBar);
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(e -> {
            formatBar.setManaged(false);
            formatBar.setVisible(false);
            formatBarVisible = false;
        });
        ft.play();
    }

    private void exec(String cmd) {
        Node wv = htmlEditor.lookup("WebView");
        if (wv instanceof WebView) ((WebView)wv).getEngine().executeScript("document.execCommand('" + cmd + "', false, null)");
    }

    private void updatePinButtonStyle(Button btn, boolean isPinned) {
        if (isPinned) {
            btn.setStyle("-fx-background-color: #3B82F6; -fx-background-radius: 8px;");
            if (btn.getGraphic() instanceof SVGPath) ((SVGPath) btn.getGraphic()).setFill(Color.WHITE);
        } else {
            btn.setStyle("");
            if (btn.getGraphic() instanceof SVGPath) ((SVGPath) btn.getGraphic()).setFill(Color.web("#333333"));
        }
    }

    private Button createToolbarBtn(String path, String tip) {
        Button b = new Button();
        SVGPath s = new SVGPath(); s.setContent(path);
        s.setFill(Color.web("#333")); s.setScaleX(0.7); s.setScaleY(0.7);
        b.setGraphic(s); b.getStyleClass().add("editor-toolbar-btn");
        b.setTooltip(new Tooltip(tip)); return b;
    }

    private Button createMiniBtn(String path, String tip) {
        Button b = new Button();
        SVGPath s = new SVGPath(); s.setContent(path);
        s.setFill(Color.web("#555")); s.setScaleX(0.6); s.setScaleY(0.6);
        b.setGraphic(s); b.getStyleClass().add("mini-format-btn");
        b.setTooltip(new Tooltip(tip)); return b;
    }

    public void saveNoteContent() { 
        if(htmlEditor != null) {
            String html = htmlEditor.getHtmlText();
            // SANITIZER: Strip out the injected style tag before saving to prevent polluted previews
            if (html != null) {
                html = html.replaceAll("(?s)<style id=\"noteEditorBackgroundColorStyle\">.*?</style>", "");
                html = html.replaceAll("style=\"background-color: [^;]*;\"", "");
            }
            note.setContent(html);
            note.setLastModified(LocalDateTime.now());
        }
    }

    public int getNoteId() { return note.getId(); }
}
