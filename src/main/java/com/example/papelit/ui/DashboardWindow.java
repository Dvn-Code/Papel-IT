package com.example.papelit.ui;

import com.example.papelit.StickyNotesApp;
import com.example.papelit.dao.NoteDAO;
import com.example.papelit.dao.PreferencesDAO;
import com.example.papelit.model.Note;
import com.example.papelit.model.UserPreferences;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.Screen;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardWindow extends Stage {
    private final NoteDAO noteDAO = new NoteDAO();
    private final PreferencesDAO prefsDAO = new PreferencesDAO();
    private UserPreferences preferences;

    private ObservableList<Note> allNotes;
    private ObservableList<Note> filteredNotes;
    private FlowPane notesGrid;
    private TextField searchField;
    private String currentFilterTag = "Home";
    private final List<StickyNoteWindow> activeWindows = new ArrayList<>();
    private Timer autoSaveTimer;

    private final String[] COLORS = {"#FFD8A8", "#B2F2BB", "#D0EBFF", "#FFC9C9", "#E5DBFF", "#FFF3BF"};

    public DashboardWindow() {
        preferences = prefsDAO.getPreferences();
        setTitle("Papel IT");
        
        // 60% Width and 50% Height based on screen resolution
        double screenW = Screen.getPrimary().getVisualBounds().getWidth();
        double screenH = Screen.getPrimary().getVisualBounds().getHeight();
        setMinWidth(screenW * 0.60);
        setMinHeight(screenH * 0.50);
        
        // Final Polish: Set the taskbar/dock icon
        try {
            var iconRes = getClass().getResource("/icon.png");
            if (iconRes != null) getIcons().add(new Image(iconRes.toExternalForm()));
        } catch (Exception ignored) {}

        allNotes = FXCollections.observableArrayList(noteDAO.getAllNotes());
        filteredNotes = FXCollections.observableArrayList();

        setupUI(); // Build the UI first
        updateFilter(); // THEN sort and filter once components exist
        refreshGrid(); // Finally show the notes

        setOnCloseRequest(e -> {
            shutdown();
            // We use Platform.runLater to check for exit AFTER the window is hidden
            Platform.runLater(StickyNotesApp::checkWindowsAndExit);
        });

        startAutoSave();
        openPreviouslyOpenNotes();
    }

    private void setupUI() {
        StackPane rootLayer = new StackPane();
        rootLayer.getStyleClass().add("dashboard-root");

        BorderPane mainLayout = new BorderPane();

        // --- Sidebar ---
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setPrefWidth(220);

        ImageView logoImage = new ImageView();
        try {
            var resource = getClass().getResource("/icon.png");
            if (resource != null) {
                logoImage.setImage(new Image(resource.toExternalForm()));
                logoImage.setFitWidth(32); logoImage.setFitHeight(32);
                logoImage.setPreserveRatio(true);
            }
        } catch (Exception e) {}

        Label logoLabel = new Label("Papel IT");
        logoLabel.getStyleClass().add("logo-text");
        HBox logoContainer = new HBox(12, logoImage, logoLabel);
        logoContainer.setAlignment(Pos.CENTER_LEFT);
        
        VBox navMenu = new VBox(5);
        
        Button homeBtn = createNavButton("Home", "M12 3L4 9v12h5v-7h6v7h5V9l-8-6z", "Home".equalsIgnoreCase(currentFilterTag));
        homeBtn.setOnAction(e -> applyTagFilter("Home", homeBtn, navMenu));
        
        Button workBtn = createNavButton("Work", "M20 7h-4V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2zM10 5h4v2h-4V5z", "Work".equalsIgnoreCase(currentFilterTag));
        workBtn.setOnAction(e -> applyTagFilter("Work", workBtn, navMenu));

        Button personalBtn = createNavButton("Personal", "M12 12a4 4 0 100-8 4 4 0 000 8zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z", "Personal".equalsIgnoreCase(currentFilterTag));
        personalBtn.setOnAction(e -> applyTagFilter("Personal", personalBtn, navMenu));

        Button ideasBtn = createNavButton("Ideas", "M9 21h6v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7z", "Ideas".equalsIgnoreCase(currentFilterTag));
        ideasBtn.setOnAction(e -> applyTagFilter("Ideas", ideasBtn, navMenu));

        Button settingsBtn = createNavButton("Settings", "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 00.12-.61l-1.92-3.32a.488.488 0 00-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 00-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 00-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.49.49 0 00-.12-.61l-2.03-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6 0-1.98 1.62-3.6 3.6-3.6 1.98 0 3.6 1.62 3.6 3.6 0 1.98-1.62 3.6-3.6 3.6z", false);
        settingsBtn.setOnAction(e -> openSettings());

        navMenu.getChildren().addAll(homeBtn, workBtn, personalBtn, ideasBtn, settingsBtn);

        Region navSpacer = new Region();
        VBox.setVgrow(navSpacer, Priority.ALWAYS);

        Button newNoteBtn = new Button("+ New Note");
        newNoteBtn.getStyleClass().add("new-note-btn-high");
        newNoteBtn.setMaxWidth(Double.MAX_VALUE);
        newNoteBtn.setOnAction(e -> createNewNote());

        sidebar.getChildren().addAll(logoContainer, new Region(){{setPrefHeight(20);}}, navMenu, navSpacer, newNoteBtn);
        mainLayout.setLeft(sidebar);

        // --- Main Dashboard Area ---
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(40));
        mainLayout.setCenter(mainContent);

        searchField = new TextField();
        searchField.setPromptText("Search your notes...");
        searchField.getStyleClass().add("search-field-mockup");
        searchField.setPrefWidth(450);
        searchField.textProperty().addListener((o, ov, nv) -> {
            updateFilter();
            refreshGrid();
        });

        StackPane searchContainer = new StackPane();
        SVGPath searchIcon = createSVGIcon("M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z", "#777", 18);
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 15));

        // Centered Search Row
        HBox searchRow = new HBox(searchContainer);
        searchRow.setAlignment(Pos.CENTER);

        Label mainTitle = new Label("My Notes");
        mainTitle.getStyleClass().add("main-title");

        notesGrid = new FlowPane();
        notesGrid.setHgap(20);
        notesGrid.setVgap(20);
        
        ScrollPane scroll = new ScrollPane(notesGrid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("no-border-scroll");
        // Re-aligned to the left as requested
        notesGrid.setAlignment(Pos.TOP_LEFT);
        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            notesGrid.setPrefWrapLength(newW.doubleValue() - 40);
        });

        mainContent.getChildren().addAll(searchRow, mainTitle, scroll);
        rootLayer.getChildren().add(mainLayout);
        
        Scene scene = new Scene(rootLayer, 1100, 750);
        applyTheme(scene);
        setScene(scene);
    }

    private void applyTagFilter(String tag, Button source, VBox menu) {
        currentFilterTag = tag;
        for (Node n : menu.getChildren()) if (n instanceof Button) n.getStyleClass().remove("active");
        source.getStyleClass().add("active");
        updateFilter();
        refreshGrid();
    }

    private void updateFilter() {
        String query = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase() : "";
        filteredNotes.clear();
        for (Note note : allNotes) {
            boolean matchesSearch = note.getTitle().toLowerCase().contains(query) || 
                                   (note.getContent() != null && note.getContent().toLowerCase().contains(query));
            boolean matchesTag = currentFilterTag.equalsIgnoreCase("Home") || currentFilterTag.equalsIgnoreCase(note.getTag());
            if (matchesSearch && matchesTag) filteredNotes.add(note);
        }
        
        // --- Sorting: Pinned first, then by ID (Newest created first) ---
        filteredNotes.sort((n1, n2) -> {
            if (n1.isAlwaysOnTop() != n2.isAlwaysOnTop()) {
                return n1.isAlwaysOnTop() ? -1 : 1;
            }
            return Integer.compare(n2.getId(), n1.getId());
        });
    }

    private Button createNavButton(String text, String svgPath, boolean isActive) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn-mockup");
        if (isActive) btn.getStyleClass().add("active");
        btn.setGraphic(createSVGIcon(svgPath, isActive ? "#111" : "#666", 20));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphicTextGap(15);
        return btn;
    }

    private SVGPath createSVGIcon(String path, String color, double size) {
        SVGPath svg = new SVGPath();
        svg.setContent(path);
        svg.setFill(Color.web(color));
        double scale = size / 24.0; 
        svg.setScaleX(scale); svg.setScaleY(scale);
        return svg;
    }

    private void refreshGrid() {
        if (notesGrid == null) return;
        notesGrid.getChildren().clear();
        for (Note note : filteredNotes) {
            VBox card = createNoteCard(note);
            // Bulletproof fixed width
            card.setMinWidth(300);
            card.setPrefWidth(300);
            card.setMaxWidth(300);
            notesGrid.getChildren().add(card);
        }
    }

    private VBox createNoteCard(Note note) {
        VBox card = new VBox(0);
        card.getStyleClass().add("bento-card");
        card.setStyle("-fx-background-color: " + (note.getColor() != null ? note.getColor() : "#FFD8A8") + ";");
        card.setPrefWidth(260); card.setMinHeight(180); card.setMaxWidth(380);

        BorderPane inner = new BorderPane();
        VBox.setVgrow(inner, Priority.ALWAYS);

        HBox header = new HBox();
        header.setPadding(new Insets(20, 20, 0, 20));
        
        Label titleLabel = new Label(note.getTitle());
        titleLabel.getStyleClass().add("bento-card-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        header.getChildren().add(titleLabel);

        if (note.isAlwaysOnTop()) {
             SVGPath pinIcon = createSVGIcon("M16 11V3h-1l-1 1H7l-1-1H5v8l-1 1v2h7v4l1 1 1-1v-4h7v-2l-1-1z", "rgba(0,0,0,0.25)", 14);
             header.getChildren().add(pinIcon);
        }
        inner.setTop(header);

        // Content
        Label content = new Label(stripHtml(note.getContent()));
        content.getStyleClass().add("bento-card-body");
        content.setWrapText(true); 
        content.setPadding(new Insets(10, 20, 0, 20));
        content.setMaxHeight(Double.MAX_VALUE);
        content.setAlignment(Pos.TOP_LEFT);
        inner.setCenter(content);
        BorderPane.setAlignment(content, Pos.TOP_LEFT);

        // Footer
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 20, 20, 20)); 
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label tagLabel = new Label(note.getTag());
        tagLabel.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: rgba(0,0,0,0.08); -fx-background-radius: 6;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button openBtn = createIconBtn("M19 19H5V5h7V3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z");
        openBtn.setOnAction(e -> openNoteWindow(note));
        
        footer.getChildren().addAll(tagLabel, spacer, openBtn);
        inner.setBottom(footer);

        card.getChildren().add(inner);
        card.setOnMouseClicked(e -> { if(e.getClickCount() == 2) openNoteWindow(note); });
        return card;
    }

    private Button createIconBtn(String path) {
        Button btn = new Button();
        btn.setGraphic(createSVGIcon(path, "#555", 16));
        btn.getStyleClass().add("bento-icon-btn");
        return btn;
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        // Remove style and script tags and their content
        String clean = html.replaceAll("(?s)<style>.*?</style>", "")
                          .replaceAll("(?s)<script>.*?</script>", "");
        // Remove all remaining tags
        clean = clean.replaceAll("<[^>]*>", "");
        // Replace common entities and clean up whitespace
        return clean.replaceAll("&nbsp;", " ").replaceAll("&lt;", "<").replaceAll("&gt;", ">").trim();
    }

    public void applyTheme(Scene scene) {
        String t = preferences.getTheme();
        var res = getClass().getResource("/style-" + t + ".css");
        if (res != null) {
            String url = res.toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(url);
        }
    }

    private void updateThemes() {
        applyTheme(getScene());
        for(StickyNoteWindow w : activeWindows) {
            applyTheme(w.getScene());
        }
    }

    private void createNewNote() {
        Note note = new Note();
        note.setColor(COLORS[(int)(Math.random() * COLORS.length)]);
        note.setTag(currentFilterTag.equalsIgnoreCase("Home") ? "Home" : currentFilterTag);
        noteDAO.insertNote(note);
        allNotes.add(note);
        updateFilter(); // Re-sort and re-filter immediately
        openNoteWindow(note);
        refreshGrid();
    }

    private void openNoteWindow(Note note) {
        note.setOpen(true);
        saveNote(note);
        for (StickyNoteWindow w : activeWindows) { if(w.getNoteId() == note.getId()) { w.requestFocus(); return; } }
        StickyNoteWindow nw = new StickyNoteWindow(note, this);
        activeWindows.add(nw);
        nw.show();
        nw.setOnHidden(evt -> {
            activeWindows.remove(nw);
            // Check for exit when a note is closed
            Platform.runLater(StickyNotesApp::checkWindowsAndExit);
        });
    }

    public void saveNote(Note note) { 
        noteDAO.updateNote(note); 
        updateFilter(); 
        refreshGrid(); 
    }

    public void deleteNote(Note note) {
        noteDAO.deleteNote(note.getId());
        allNotes.remove(note);
        updateFilter();
        refreshGrid();
    }

    private void openSettings() {
        Stage s = new Stage(); 
        s.setTitle("Settings");
        s.setResizable(false); 
        
        VBox root = new VBox(25); root.setPadding(new Insets(40)); root.getStyleClass().add("settings-pane");
        Label title = new Label("Settings"); title.getStyleClass().add("main-title");
        
        VBox content = new VBox(15, new Label("Theme"), new ComboBox<String>(FXCollections.observableArrayList("light", "dark")) {{
            setValue(preferences.getTheme());
            setMaxWidth(Double.MAX_VALUE);
            valueProperty().addListener((o, ov, nv) -> { 
                preferences.setTheme(nv); 
                updateThemes(); 
                prefsDAO.savePreferences(preferences); // Save immediately
            });
        }});
        
        Button done = new Button("Done"); 
        done.getStyleClass().add("new-note-btn-high"); 
        done.setMaxWidth(Double.MAX_VALUE);
        done.setOnAction(e -> { prefsDAO.savePreferences(preferences); s.close(); });
        
        root.getChildren().addAll(title, content, new Region(){{VBox.setVgrow(this, Priority.ALWAYS);}}, done);
        
        Scene scene = new Scene(root, 380, 420); 
        applyTheme(scene);
        s.setScene(scene);
        s.show();
    }

    private void openPreviouslyOpenNotes() { for (Note note : allNotes) { if (note.isOpen()) openNoteWindow(note); } }

    private void startAutoSave() {
        if (autoSaveTimer != null) autoSaveTimer.cancel();
        autoSaveTimer = new Timer(true);
        long ms = preferences.getAutoSaveInterval() * 1000L;
        autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { Platform.runLater(() -> {
                for (StickyNoteWindow w : activeWindows) w.saveNoteContent();
                for (Note n : allNotes) noteDAO.updateNote(n);
                refreshGrid();
            });}
        }, ms, ms);
    }

    public void shutdown() {
        for (StickyNoteWindow w : activeWindows) w.saveNoteContent();
        for (Note n : allNotes) noteDAO.updateNote(n);
    }
    public UserPreferences getPreferences() { return preferences; }
}
