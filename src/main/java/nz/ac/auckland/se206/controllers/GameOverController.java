package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class GameOverController {

  @FXML private Label correctLabel;
  @FXML private Label incorrectLabel;
  @FXML private TextArea textArea;

  private int suspect;

  @FXML
  public void setSuspect(int suspect) {
    this.suspect = suspect;
    if (suspect == 2) {
      correctLabel.setVisible(true);
    } else {
      incorrectLabel.setVisible(true);
    }
  }

  @FXML
  public void setFeedback(String feedback) {
    if (suspect == 2) {
      textArea.setText(feedback);
    }
  }
}
