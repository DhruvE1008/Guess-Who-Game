package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.GameTimer;
import nz.ac.auckland.se206.ObjectivesManager;
import nz.ac.auckland.se206.SuspectOverlay;
import nz.ac.auckland.se206.TimerManager;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {
  private TranslateTransition scanTransition;
  private static int button = 1;
  private static int current = 1;
  private static GameStateContext context = BackStoryController.getContext();

  @FXML
  public static void resetGuessButton() {
    button = 1;
    context = BackStoryController.getContext();
  }

  @FXML private Button arrowButton;
  @FXML private Button btnGuess;
  @FXML private Button btnObjectives;
  @FXML private Button objectiveClose;
  @FXML private ImageView archaeologist;
  @FXML private ImageView camSlide;
  @FXML private ImageView closeButtonImage;
  @FXML private ImageView closeButtonImage1;
  @FXML private ImageView closeButtonImage2;
  @FXML private ImageView crimeScene;
  @FXML private ImageView cross;
  @FXML private ImageView guide;
  @FXML private ImageView journalist;
  @FXML private ImageView photoClue;
  @FXML private ImageView pictureBackground;
  @FXML private ImageView scanComplete;
  @FXML private ImageView scanningFootprint;
  @FXML private ImageView startScan;
  @FXML private Label flipLabel;
  @FXML private Label scanLabel;
  @FXML private Label timerLabel;
  @FXML private Text objective1Label;
  @FXML private Text objective2Label;
  @FXML private VBox objectiveMenu;
  @FXML private VBox suspectMenu;
  @FXML private Line scanLine;
  @FXML private ImageView footprint;

  private boolean clueVisible = false;
  private boolean isFootprintVisible = false;
  private GameTimer gameTimer;
  private Image backImage;
  private Image fifthSlide;
  private Image frontImage;
  private Image fourthSlide;
  private Image secondSlide;
  private Image sixthSlide;
  private Image thirdSlide;
  private ObjectivesManager objectivesManager;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    closeButtonImage2.setVisible(false);
    pictureBackground.setVisible(true);
    gameTimer = TimerManager.getGameTimer();

    // Bind the timer label to display the time in minutes and seconds
    timerLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  int totalSeconds = gameTimer.getTimeInSeconds();
                  int minutes = totalSeconds / 60;
                  int seconds = totalSeconds % 60;
                  if (totalSeconds == 0 && context.getState() == context.getGameStartedState()) {
                    if (!App.getObjectiveCompleted()) {
                      App.changeGameOver(
                          0, "ran out of time, you didn't interact with the scenes enough!");
                    } else {
                      context.setState(context.getGuessingState());
                      App.changeGuessing();
                    }
                  }
                  return String.format("%02d:%02d", minutes, seconds);
                },
                gameTimer.timeInSecondsProperty()));

    frontImage = new Image(getClass().getResourceAsStream("/images/kid.jpg"));
    backImage = new Image(getClass().getResourceAsStream("/images/pin.png"));

    secondSlide = new Image(getClass().getResourceAsStream("/images/730.png"));
    thirdSlide = new Image(getClass().getResourceAsStream("/images/740.png"));
    fourthSlide = new Image(getClass().getResourceAsStream("/images/750.png"));
    fifthSlide = new Image(getClass().getResourceAsStream("/images/static.png"));
    sixthSlide = new Image(getClass().getResourceAsStream("/images/stolen.png"));
    photoClue.setImage(frontImage);

    objectivesManager = ObjectivesManager.getInstance();
    updateObjectiveLabels(); // Initial update

    // Register this controller as an observer to update the UI when objectives are completed
    objectivesManager.addObserver(this::updateObjectiveLabels);

    if (button == 1) {
      btnGuess.setDisable(true);
    } else {
      btnGuess.setDisable(false);
    }
  }

  private void setupScanLineMovement() {
    // Create the translate transition for the scanLine on the Y-axis
    scanTransition = new TranslateTransition(Duration.millis(2000), scanLine);
    scanTransition.setByY(200); // Move 200 units up and down along the Y-axis
    scanTransition.setCycleCount(2); // Only one full cycle
    scanTransition.setAutoReverse(true); // Automatically reverse direction after reaching the end

    // Add a listener to detect when the transition completes
    scanTransition.setOnFinished(
        event -> {
          completeScan(); // Call method to handle post-scan logic
        });

    // Add a listener to the parent node or scene to ensure the key events are captured
    scanLabel.getScene().setOnKeyPressed(this::handleKeyPressed);
    scanLabel.getScene().setOnKeyReleased(this::handleKeyReleased);
  }

  private void completeScan() {
    stopScanLineMovement(); // Stop the scan line movement

    footprint.setImage(
        new Image(
            getClass()
                .getResourceAsStream("/images/scancomplete.jpg"))); // Change the footprint image
    scanLabel.setVisible(false); // Hide the scan label
    scanLine.setVisible(false); // Hide the scan line
  }

  private void handleKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.S) {
      scanLabel.setText("Scanning...");
      startScanLineMovement(); // Start scan when 'S' is pressed
    }
  }

  private void handleKeyReleased(KeyEvent event) {
    if (event.getCode() == KeyCode.S) {
      // Do nothing on key release in this case
    }
  }

  private void startScanLineMovement() {
    scanTransition.play(); // Start the scan line movement
  }

  private void stopScanLineMovement() {
    scanTransition.stop(); // Stop the scan line movement
  }

  // Update the objective labels
  public void updateObjectiveLabels() {
    // Update the first objective label
    if (objectivesManager.isObjectiveCompleted(0)) {
      objective1Label.setStyle("-fx-strikethrough: true;");
    }

    // Update the second objective label
    if (objectivesManager.isObjectiveCompleted(1)) {
      objective2Label.setStyle("-fx-strikethrough: true;");
    }
  }

  @FXML
  private void onCamClicked() {
    // if the suspect menu is visible then it will be closed
    if (suspectMenu.isVisible()) {
      onToggleMenu();
    }
    // closes all the other clues
    handleCloseClick(null);
    onCloseButton1Pressed();
    closeButtonImage1.setVisible(false);
    // updates the objectives since a clue has been clicked
    objectivesManager.completeObjectiveStep(1);
    pictureBackground.setVisible(false);
    // sets the current slide to the first slide
    camSlide.setVisible(true);
    closeButtonImage2.setVisible(true);
    current = 1;
    camSlide.setImage(secondSlide);
  }

  @FXML
  private void onCloseButton2Pressed() {
    pictureBackground.setVisible(true);
    camSlide.setVisible(false);
    closeButtonImage2.setVisible(false);
  }

  @FXML
  public void nxtImg() {
    // basically a switch statement to change the image
    // to the right based on what slide it is currently on
    System.out.println(current);
    current = current + 1;
    if (current == 2) {
      camSlide.setImage(thirdSlide);
    } else if (current == 3) {
      camSlide.setImage(fourthSlide);
    } else if (current == 4) {
      camSlide.setImage(fifthSlide);
    } else if (current == 5) {
      camSlide.setImage(sixthSlide);
    } else {
      current = 5;
      camSlide.setImage(sixthSlide);
    }
  }

  @FXML
  public void prevImg() {
    // basically a switch statement to change the image
    // to the left based on what slide it is currently on
    System.out.println(current);
    current = current - 1;
    if (current == 2) {
      camSlide.setImage(thirdSlide);
    } else if (current == 3) {
      camSlide.setImage(fourthSlide);
    } else if (current == 4) {
      camSlide.setImage(fifthSlide);
    } else if (current == 5) {
      camSlide.setImage(sixthSlide);
    } else {
      current = 1;
      camSlide.setImage(secondSlide);
    }
  }

  @FXML
  private void onCloseButton1Pressed() {
    System.out.println("hi");
    footprint.setImage(new Image(getClass().getResourceAsStream("/images/startScan.png")));
    footprint.setVisible(false);
    scanLabel.setVisible(false);
    scanLine.setVisible(false);
    closeButtonImage1.setVisible(false);
  }

  @FXML
  private void onHover(MouseEvent event) {
    SuspectOverlay.onHover(event);
  }

  @FXML
  private void onExit(MouseEvent event) {
    SuspectOverlay.onExit(event);
  }

  @FXML
  private void onToggleMenu() {
    if (camSlide.isVisible()) {
      onCloseButton2Pressed();
    }

    onCloseButton2Pressed();

    SuspectOverlay.toggleMenu(suspectMenu, arrowButton, objectiveMenu, objectiveClose);
  }

  @FXML
  private void toggleObjectives() {
    SuspectOverlay.toggleObjectives(objectiveMenu, objectiveClose, suspectMenu, this::onToggleMenu);
  }

  @FXML
  private void closeObjectives() {
    SuspectOverlay.closeObjectivesMenu(objectiveMenu, objectiveClose);
  }

  @FXML
  public void rotate() {
    // if the photo clue is not visible then it will not rotate
    if (!clueVisible) {
      return;
    }
    // the transition for the visible photo clue to rotate
    RotateTransition rotateOut = new RotateTransition(Duration.millis(250), photoClue);
    rotateOut.setByAngle(90);
    rotateOut.setAxis(Rotate.X_AXIS);

    // the transition for the invisible photo clue to rotate
    RotateTransition rotateIn = new RotateTransition(Duration.millis(250), photoClue);
    rotateIn.setByAngle(90);
    rotateIn.setAxis(Rotate.X_AXIS);

    // once the photo clue is rotated out it will be replaced with the other image
    rotateOut.setOnFinished(
        event -> {
          if (photoClue.getImage().equals(frontImage)) {
            photoClue.setImage(backImage);
          } else {
            photoClue.setImage(frontImage);
          }
          rotateIn.play();
        });
    rotateOut.play();
  }

  @FXML
  private void handleCloseClick(MouseEvent event) {
    if (clueVisible) {
      clueVisible = false;
      flipLabel.setVisible(false);
      photoClue.setVisible(false);
      cross.setVisible(false);
    }
  }

  @FXML
  private void handleFootClick() {
    setupScanLineMovement();
    footprint.setVisible(true);
    scanLabel.setVisible(true);
    scanLine.setVisible(true);
    closeButtonImage1.setVisible(true);
  }

  @FXML
  private void onProfileClick(MouseEvent event) throws IOException {
    ImageView clickedImageView = (ImageView) event.getSource();
    context.handleProfileClick(event, clickedImageView.getId());
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

  @FXML
  private void handlePhotoClueClick(MouseEvent event) {
    // if the photo clue is clicked it will update the objectives
    objectivesManager.completeObjectiveStep(1);
    // closes the other clues
    onCloseButton1Pressed();
    onCloseButton2Pressed();
    if (!clueVisible) {
      // if the user is still looking at the clue then it will be rotated
      clueVisible = true;
      flipLabel.setVisible(true);
      photoClue.setVisible(true);
      cross.setVisible(true);
    }
  }

  @FXML
  public void setGuessButton() {
    button = 2;
    btnGuess.setDisable(false);
    System.out.println("hi");
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    // if (objectivesManager.isObjectiveCompleted(0) && objectivesManager.isObjectiveCompleted(1)) {
    context.setState(context.getGuessingState());
    App.changeGuessing();
    // }
  }
}
