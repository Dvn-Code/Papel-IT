package com.example.papelit;

import com.example.papelit.dao.DatabaseManager;
import com.example.papelit.ui.DashboardWindow;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;

public class StickyNotesApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        DatabaseManager.initializeDatabase();
        
        DashboardWindow dashboard = new DashboardWindow();
        dashboard.show();

        checkAndExitLogic();
    }

    private void checkAndExitLogic() {
    }

    public static void checkWindowsAndExit() {
        long openWindows = Window.getWindows().stream()
                .filter(Window::isShowing)
                .count();

        if (openWindows == 0) {
            System.out.println("No windows left open. Terminating Papel IT...");
            Platform.exit();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
