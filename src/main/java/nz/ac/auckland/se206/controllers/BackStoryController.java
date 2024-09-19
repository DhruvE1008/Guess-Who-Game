package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.App;

public class BackStoryController {

  @FXML private Label backstoryLabel;
  @FXML private ImageView tombImage;
  @FXML private Button skipButton;
  @FXML private Button continueButton;
  @FXML private ImageView image;

  // This method initializes the backstory content
  @FXML
  private void initialize() {}

  // Handle the "continue" button click to proceed to the next scene
  @FXML
  private void handleContinue(MouseEvent event) throws IOException {
    App.changeCrimeScene(event); // Navigate to the crime scene or game scene
  }

  @FXML
  private void handleSkip(MouseEvent event) throws IOException {
    App.changeCrimeScene(event); // Navigate back to the main menu
  }
}
