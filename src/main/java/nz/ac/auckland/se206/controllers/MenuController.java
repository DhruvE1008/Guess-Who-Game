package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;

public class MenuController {
  @FXML private Rectangle play;

  @FXML
  private void onClick(MouseEvent event) throws IOException {
    App.changeBackStory(event);
  }
}
