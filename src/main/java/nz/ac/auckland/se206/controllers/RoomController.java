package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
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
  private static boolean isFirstInit = true;

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
  @FXML private ImageView closeButtonImage;
  @FXML private ImageView closeButtonImage1;
  @FXML private ImageView closeButtonImage3;
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
  @FXML private ImageView envelopeFront;
  @FXML private ImageView envelopeBack;
  @FXML private ImageView imageClue;
  @FXML private Label envelopeLabel1;
  @FXML private Label envelopeLabel2;
  @FXML private Pane phonePopup;
  @FXML private ImageView phoneDisplay;
  @FXML private Rectangle leftarrow;
  @FXML private Rectangle rightarrow;
  @FXML private Rectangle unlockphone;
  @FXML private Rectangle gallery;
  @FXML private Rectangle calendar;

  private boolean clueVisible = false;
  private boolean isFKeyPressed = false;
  private GameTimer gameTimer;
  private Image backImage;
  private Image frontImage;
  private ObjectivesManager objectivesManager;
  private double yValue;
  private double initialImageClueY;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {

    if (isFirstInit) {
      isFirstInit = false;
      initialImageClueY = imageClue.getY();
    }
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

  @FXML
  private void onPhoneClick() {
    onCloseButtonPressed();
    onCloseButton1Pressed();
    handleCloseClick(null);
    // open the phone and update the objectives
    objectivesManager.completeObjectiveStep(1);
    phonePopup.setVisible(true);
    closeButtonImage3.setVisible(true);
    leftarrow.setDisable(true);
    rightarrow.setDisable(true);
    unlockphone.setDisable(false);
    gallery.setDisable(true);
    calendar.setDisable(true);
  }

  @FXML
  // open the gallery
  private void onPhotoClicked() {
    // enable the arrow buttons and disable the gallery and calendar buttons
    leftarrow.setDisable(false);
    rightarrow.setDisable(false);
    gallery.setDisable(true);
    calendar.setDisable(true);
    // current tracks what image is currently being displayed
    current = 1;
    // make the initial image the kid image
    phoneDisplay.setImage(
        new Image(getClass().getResourceAsStream("/images/phonegallerykidarrow.png")));
  }

  @FXML
  private void onBackPressed() {
    // go back to the home screen
    phoneDisplay.setImage(new Image(getClass().getResourceAsStream("/images/home_screen.png")));
    gallery.setDisable(false);
    calendar.setDisable(false);
    leftarrow.setDisable(true);
    rightarrow.setDisable(true);
  }

  @FXML
  private void onUnlockPhone() {
    // unlock the phone and go to the home screen
    phoneDisplay.setImage(new Image(getClass().getResourceAsStream("/images/home_screen.png")));
    unlockphone.setDisable(true);
    gallery.setDisable(false);
    calendar.setDisable(false);
  }

  @FXML
  private void onCalendarClicked() {
    // open the calendar
    phoneDisplay.setImage(new Image(getClass().getResourceAsStream("/images/calendar.png")));
    gallery.setDisable(true);
    calendar.setDisable(true);
  }

  @FXML
  private void onCloseButtonPressed() {
    phonePopup.setVisible(false); // Hide the phone popup when the close button is clicked
    phoneDisplay.setImage(new Image(getClass().getResourceAsStream("/images/phone.png")));
    closeButtonImage3.setVisible(false);
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
    } else if (event.getCode() == KeyCode.F) {
      rotate();
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
    if (scanTransition != null) {
      scanTransition.stop(); // Stop the scan line movement
    }
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
  public void nxtImg() {
    // basically a switch statement to change the image
    // to the right based on what slide it is currently on

    current = current + 1;
    System.out.println(current);
    if (current == 1) {
      phoneDisplay.setImage(
          new Image(getClass().getResourceAsStream("/images/phonegallerykidarrow.png")));
    } else if (current == 2) {
      phoneDisplay.setImage(
          new Image(getClass().getResourceAsStream("/images/phonegalleryegyptarrow.png")));
    } else {
      current = 3;
      phoneDisplay.setImage(
          new Image(getClass().getResourceAsStream("/images/gallerymapwitharrows.png")));
    }
  }

  @FXML
  public void prevImg() {
    // basically a switch statement to change the image
    // to the left based on what slide it is currently on
    System.out.println(current);
    current = current - 1;
    if (current == 2) {
      phoneDisplay.setImage(
          new Image(getClass().getResourceAsStream("/images/phonegalleryegyptarrow.png")));
    } else if (current == 3) {
      phoneDisplay.setImage(
          new Image(getClass().getResourceAsStream("/images/gallerymapwitharrows.png")));
    } else {
      current = 1;
      phoneDisplay.setImage(
          new Image(getClass().getResourceAsStream("/images/phonegallerykidarrow.png")));
    }
  }

  @FXML
  private void onCloseButton1Pressed() {
    stopScanLineMovement();
    scanLine.setTranslateY(0);
    footprint.setImage(new Image(getClass().getResourceAsStream("/images/startScan.png")));
    footprint.setVisible(false);
    scanLabel.setVisible(false);
    scanLabel.setText("Press 'S' to scan");
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
    // closes all the images related to the photo clue and labels associated with them
    clueVisible = false;
    flipLabel.setVisible(false);
    photoClue.setVisible(false);
    cross.setVisible(false);
    imageClue.setVisible(false);
    envelopeLabel1.setVisible(false);
    envelopeLabel2.setVisible(false);
    envelopeFront.setVisible(false);
    envelopeBack.setVisible(false);
  }

  @FXML
  private void handleFootClick() {
    onCloseButtonPressed();
    onCloseButton1Pressed();
    handleCloseClick(null);
    setupScanLineMovement();
    objectivesManager.completeObjectiveStep(1);
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
    // If the photo clue is clicked, update the objectives
    objectivesManager.completeObjectiveStep(1);

    // Close any other clues that are open
    onCloseButton1Pressed();

    if (!clueVisible) {
      // Set the clue as visible and display related UI elements
      clueVisible = true;
      flipLabel.setVisible(true);
      photoClue.setVisible(true);
      cross.setVisible(true);

      // Set up the key event handler for the photo clue
      Scene scene = photoClue.getScene(); // Get the scene from the photo clue
      scene.setOnKeyPressed(
          (EventHandler<? super KeyEvent>)
              new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                  // Rotate only when 'F' is tapped (not held)
                  if (event.getCode() == KeyCode.F && !isFKeyPressed) {
                    rotate(); // Rotate the photo clue
                    isFKeyPressed = true; // Set the flag to prevent multiple triggers
                  }
                }
              });

      // Also handle KeyReleased event to reset the flag
      scene.setOnKeyReleased(
          (EventHandler<? super KeyEvent>)
              new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                  if (event.getCode() == KeyCode.F) {
                    isFKeyPressed = false; // Reset the flag when the key is released
                  }
                }
              });
    }
  }

  @FXML
  private void handleEnvelopeClick(MouseEvent event) {
    onCloseButtonPressed();
    handleCloseClick(event);
    // if the envelope is clicked it will update the objectives
    objectivesManager.completeObjectiveStep(1);
    // closes the other clues
    onCloseButton1Pressed();
    imageClue.setVisible(true);
    envelopeLabel1.setVisible(true);
    envelopeLabel2.setVisible(true);
    envelopeFront.setVisible(true);
    envelopeBack.setVisible(true);
    cross.setVisible(true);
  }

  @FXML
  private void handleMousePressed(MouseEvent event) {
    yValue = event.getSceneY();
  }

  @FXML
  private void handleDrag(MouseEvent event) {
    // gets the position that the user is dragging the photo from
    double newY = event.getSceneY() - yValue;
    // stops the user from dragging the photo under the envelope
    if ((newY + imageClue.getFitHeight())
        <= (envelopeFront.getY() + envelopeFront.getFitHeight())) {
      imageClue.setY(newY);
    }
    // changes the label based on the position of the photo whether it is
    // above or in the envelope
    if ((imageClue.getY() + imageClue.getFitHeight()) < envelopeFront.getY()) {
      envelopeLabel2.setVisible(false);
      envelopeLabel1.setText("Let go of the photo");
    } else {
      envelopeLabel2.setVisible(true);
      envelopeLabel1.setText("Pull the photo up");
    }
  }

  @FXML
  private void handleDragFinish(MouseEvent event) {
    // if the photo is dragged above the envelope then a transition occurs
    if ((imageClue.getY() + imageClue.getFitHeight()) < envelopeFront.getY()) {
      imageClue.setY(initialImageClueY);
      // transition occurs in parallel for both parts of the envelope
      // the whole envelope moves down in 1 second
      TranslateTransition envelopeTransition = new TranslateTransition();
      envelopeTransition.setNode(envelopeFront);
      envelopeTransition.setDuration(Duration.seconds(1));
      envelopeTransition.setToY(500.0);
      TranslateTransition photoTransition = new TranslateTransition();
      photoTransition.setNode(envelopeBack);
      photoTransition.setDuration(Duration.seconds(1));
      photoTransition.setToY(500.0);
      ParallelTransition parallelTransition =
          new ParallelTransition(envelopeTransition, photoTransition);
      parallelTransition.setOnFinished(
          e -> {
            // once the transition is finished the envelope and photo are hidden
            envelopeBack.setVisible(false);
            envelopeFront.setVisible(false);
            envelopeBack.setTranslateY(0);
            envelopeFront.setTranslateY(0);
          });
      parallelTransition.play();
      imageClue.setVisible(false);
      envelopeLabel1.setVisible(false);
      envelopeLabel2.setVisible(false);
      handlePhotoClueClick(event);
      return;
    }
  }

  @FXML
  public void setGuessButton() {
    button = 2;
    btnGuess.setDisable(false);
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
