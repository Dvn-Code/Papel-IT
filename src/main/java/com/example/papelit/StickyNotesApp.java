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
        // Prevents exit when windows close, allowing notes to persist after dashboard closes
        Platform.setImplicitExit(false);
        DatabaseManager.initializeDatabase();
        
        DashboardWindow dashboard = new DashboardWindow();
        dashboard.show();

        // Check for exits when any window is closed
        checkAndExitLogic();
    }

    private void checkAndExitLogic() {
        // We add a listener to all windows to detect when the last one closes.
        // Actually, we can just check periodically or listen to Stage events.
        // Since setImplicitExit(false), we must call Platform.exit() manually.
    }

    public static void checkWindowsAndExit() {
        // Get all open windows
        long openWindows = Window.getWindows().stream()
                .filter(Window::isShowing)
                .count();

        // If no windows are left showing, terminate the process to save memory
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
