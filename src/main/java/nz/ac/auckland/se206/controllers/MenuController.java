package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;

/**
 * This class contains methods that are used to manage the main menu screen. The main menu screen is
 * displayed when the game is first launched.
 */
public class MenuController {
  @FXML private Rectangle play;

  /**
   * Handles the play button being clicked.
   *
   * @param event The mouse event that triggered this method
   * @throws IOException If the fxml file cannot be found
   */
  @FXML
  private void onClick(MouseEvent event) throws IOException {
    App.changeBackStory(event);
  }
}
