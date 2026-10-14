package com.homucafe.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class WelcomeController {
    @FXML
    private Label statusLabel;

    @FXML
    private void showNextSteps() {
        statusLabel.setText("Bắt đầu từ README.md → chốt phạm vi → nhận Issue đầu tiên.");
    }
}
