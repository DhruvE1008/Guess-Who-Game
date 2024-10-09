package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ObjectivesManager;
import nz.ac.auckland.se206.TimerManager;

public class GameOverController {

  @FXML private Label correctLabel;
  @FXML private Label incorrectLabel;
  @FXML private TextArea textArea;

  private int suspect;

  /**
   * Sets the feedback text to be displayed.
   *
   * @param feedback The feedback to be displayed
   */
  @FXML
  public void setFeedback(String feedback) {
    if (suspect == 2 || suspect == 0) {
      textArea.setText(feedback);
    }
  }

  /**
   * Handles the restart button being clicked.
   *
   * @param event The mouse event that triggered this method
   */
  @FXML
  public void handleRestart(MouseEvent event) {
    TimerManager.resetTimer();
    ObjectivesManager.getInstance().resetObjectives();
    App.restartGame();
  }

  /**
   * Handles the exit button being clicked.
   *
   * @param event The mouse event that triggered this method
   */
  @FXML
  private void handleExit(MouseEvent event) {

    App.exitGame();
  }

  /**
   * Sets the suspect value.
   *
   * @param suspect The suspect value
   */
  public void setSuspect(int suspect) {
    this.suspect = suspect;
    if (suspect == 2) {
      correctLabel.setVisible(true);
    } else {
      incorrectLabel.setVisible(true);
    }
  }
}
