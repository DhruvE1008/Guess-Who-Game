package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
  @FXML private Pane phonePopup;
  @FXML private ImageView phoneDisplay;
  @FXML private Rectangle leftarrow;
  @FXML private Rectangle rightarrow;
  @FXML private Rectangle unlockphone;
  @FXML private Rectangle gallery;
  @FXML private Rectangle calendar;

  private boolean clueVisible = false;
  private boolean isFootprintVisible = false;
  private GameTimer gameTimer;
  private Image backImage;
  private Image frontImage;
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

  @FXML
  private void onPhoneClick() {
    phonePopup.setVisible(true);
    closeButtonImage3.setVisible(true);
    leftarrow.setDisable(true);
    rightarrow.setDisable(true);
    unlockphone.setDisable(false);
    gallery.setDisable(true);
    calendar.setDisable(true);
  }

  @FXML
  private void onPhotoClicked() {
    leftarrow.setDisable(false);
    rightarrow.setDisable(false);
    gallery.setDisable(true);
    calendar.setDisable(true);
    current = 1;
    phoneDisplay.setImage(
        new Image(getClass().getResourceAsStream("/images/phonegallerykidarrow.png")));
  }

  @FXML
  private void onBackPressed() {
    phoneDisplay.setImage(new Image(getClass().getResourceAsStream("/images/home_screen.png")));
    gallery.setDisable(false);
    calendar.setDisable(false);
    leftarrow.setDisable(true);
    rightarrow.setDisable(true);
  }

  @FXML
  private void onUnlockPhone() {
    phoneDisplay.setImage(new Image(getClass().getResourceAsStream("/images/home_screen.png")));
    unlockphone.setDisable(true);
    gallery.setDisable(false);
    calendar.setDisable(false);
  }

  @FXML
  private void onCalendarClicked() {
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

  @FXML
  private void handleFootClick() {
    // makes all the other clues invisible
    handleCloseClick(null);
    onCloseButton2Pressed();
    // makes the footprint visible and gets it ready for the scan
    startScan.setVisible(true);
    scanLabel.setVisible(true);
    isFootprintVisible = true;
    closeButtonImage1.setVisible(true);
    // since a clue has been interacted with it updates the objectives
    objectivesManager.completeObjectiveStep(1);
  }

  @FXML
  public void scanFootprint() {
    if (!isFootprintVisible) {
      return;
    }
    System.out.println("Scanning footprint...");
    startScan.setVisible(false);
    scanComplete.setVisible(false);
    scanningFootprint.setVisible(true);
    scanLabel.setVisible(false);
    PauseTransition pause = new PauseTransition(Duration.seconds(4));
    pause.setOnFinished(
        event -> {
          // Make phonePopup invisible and unlockedPhone visible after 4 seconds
          if (scanningFootprint.isVisible()) {
            scanningFootprint.setVisible(false);
            scanComplete.setVisible(true);
            scanLabel.setVisible(true);
          }
          System.out.println("Scan complete");
        });
    pause.play();
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
  private void onCloseButton2Pressed() {
    pictureBackground.setVisible(true);
    camSlide.setVisible(false);
    closeButtonImage2.setVisible(false);
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
    // closes all the clues and labels associated with the clues
    isFootprintVisible = false;
    scanLabel.setVisible(false);
    startScan.setVisible(false);
    scanningFootprint.setVisible(false);
    scanComplete.setVisible(false);
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
