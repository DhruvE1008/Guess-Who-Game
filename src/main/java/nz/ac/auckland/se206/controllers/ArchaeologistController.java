package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.GameStateContext;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class ArchaeologistController {

  @FXML private Rectangle rectCashier;
  @FXML private Rectangle rectPerson1;
  @FXML private Rectangle rectPerson2;
  @FXML private Rectangle rectPerson3;
  @FXML private Rectangle rectWaitress;
  @FXML private Button btnGuess;
  @FXML private ImageView crimeScene;
  @FXML private ImageView archaeologist;
  @FXML private ImageView journalist;
  @FXML private ImageView guide;
  @FXML private Button arrowButton;
  @FXML private VBox menu;

  private static GameStateContext context = new GameStateContext();

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    // if (isFirstTimeInit) {
    //   TextToSpeech.speak(
    //       "Chat with the three customers, and guess who is the " +
    // context.getProfessionToGuess());
    //   isFirstTimeInit = false;
    // }
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  @FXML
  private void onHover(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
  }

  @FXML
  private void onExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
  }

  @FXML
  private void toggleMenu() {
    boolean isVisible = menu.isVisible();

    // Rotate animation for the arrow button
    RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), arrowButton);
    rotateTransition.setByAngle(isVisible ? -180 : 180);

    // Move the arrow button left or right
    TranslateTransition translateTransition =
        new TranslateTransition(Duration.millis(300), arrowButton);
    translateTransition.setByX(isVisible ? -140 : 140);

    // Slide the menu in or out
    TranslateTransition menuTransition = new TranslateTransition(Duration.millis(300), menu);

    if (isVisible) {
      // Slide out
      menuTransition.setFromX(0);
      menuTransition.setToX(-menu.getWidth());
    } else {
      // Ensure the menu is off-screen before showing it
      menu.setTranslateX(-menu.getWidth());
      menu.setVisible(true);
      menuTransition.setFromX(-menu.getWidth());
      menuTransition.setToX(0);
    }

    // Play animations
    rotateTransition.play();
    translateTransition.play();
    menuTransition.play();

    // Toggle visibility after the animation completes (for sliding out)
    menuTransition.setOnFinished(
        event -> {
          if (isVisible) {
            menu.setVisible(false);
          }
        });
  }

  @FXML
  private void onProfileClick(MouseEvent event) throws IOException {
    ImageView clickedImageView = (ImageView) event.getSource();
    context.handleProfileClick(event, clickedImageView.getId());
    System.out.println("Clicked on " + clickedImageView.getId());
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    context.handleRectangleClick(event, clickedRectangle.getId());
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    context.handleGuessClick();
  }
}
