package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.GameTimer;
import nz.ac.auckland.se206.ObjectivesManager;
import nz.ac.auckland.se206.TimerManager;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

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
  @FXML private ImageView closeButtonImage;
  @FXML private ImageView closeButtonImage1;
  @FXML private ImageView closeButtonImage2;
  @FXML private Button arrowButton;
  @FXML private VBox suspectMenu;
  @FXML private Button btnObjectives;
  @FXML private VBox objectiveMenu;
  @FXML private Button objectiveClose;
  @FXML private Pane phonePopup;
  @FXML private Text objective1Label;
  @FXML private Text objective2Label;
  @FXML private Label label1, label2, label3, label4, incorrectPin;
  @FXML private Rectangle box1, box2, box3, box4;
  @FXML private Label timerLabel;
  private int currentBoxIndex = 0;
  private String[] enteredPasscode = new String[4];
  private final String correctPasscode = "0411";
  @FXML private ImageView photoClue;
  @FXML private ImageView camSlide;
  @FXML private ImageView cross;
  @FXML private ImageView unlockedPhone;
  @FXML private MediaView mediaView;
  @FXML private Label flipLabel;
  @FXML private ImageView pictureBackground;

  private MediaPlayer mediaPlayer;
  private GameTimer gameTimer;
  private static GameStateContext context = new GameStateContext();
  private Image frontImage;
  private Image backImage;
  private Image firstSlide;
  private Image secondSlide;
  private Image thirdSlide;
  private Image fourthSlide;
  private Image fifthSlide;
  private Image sixthSlide;

  private static int current = 1;
  private boolean clueVisible = false;
  private ObjectivesManager objectivesManager;
  private boolean isPinCorrect = false;
  private boolean isFirstInit = true;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    if (isFirstInit) {
      Task<Void> timerTask =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              TimerManager.startTimer();
              return null;
            }
          };
      new Thread(timerTask).start();
      isFirstInit = false;
    }
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
                  if (totalSeconds == 0) {
                    App.changeGuessing();
                  }

                  return String.format("%02d:%02d", minutes, seconds);
                },
                gameTimer.timeInSecondsProperty()));

    frontImage = new Image(getClass().getResourceAsStream("/images/photoClue.png"));
    backImage = new Image(getClass().getResourceAsStream("/images/pin.png"));

    firstSlide = new Image(getClass().getResourceAsStream("/images/720.png"));
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

    Task<Void> mediaTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              mediaView.setVisible(false);
              Media media =
                  new Media(getClass().getResource("/images/footprintAH.mp4").toExternalForm());
              mediaPlayer = new MediaPlayer(media);
              mediaPlayer.setAutoPlay(false);

              Platform.runLater(() -> mediaView.setMediaPlayer(mediaPlayer));

            } catch (Exception e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    new Thread(mediaTask).start();

    System.out.println("Resource URL: " + getClass().getResource("/images/footprintAH.mp4"));
  }

  @FXML
  private void playVideo() {
    if (mediaPlayer != null) {
      mediaPlayer.stop(); // Stop the video if it's playing
      mediaPlayer.seek(Duration.ZERO); // Rewind to the beginning
      mediaPlayer.setAutoPlay(true);
      mediaPlayer.play();
    }
  }

  @FXML
  private void stopVideo() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
    }
  }

  @FXML
  private void handleFootClick() {
    handleCloseClick(null);
    onCloseButton2Pressed();
    mediaView.setVisible(true);
    playVideo(); // Show the phone popup when the phone is clicked
    closeButtonImage1.setVisible(true);
    objectivesManager.completeObjectiveStep(1);
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
    if (suspectMenu.isVisible()) {
      toggleMenu();
    }
    handleCloseClick(null);
    onCloseButton1Pressed();
    closeButtonImage1.setVisible(false);
    objectivesManager.completeObjectiveStep(1);
    pictureBackground.setVisible(false);
    camSlide.setVisible(true);
    closeButtonImage2.setVisible(true);
    current = 1;
    camSlide.setImage(secondSlide);

    System.out.println("hi");
  }

  @FXML
  private void onCloseButton2Pressed() {
    pictureBackground.setVisible(true);
    camSlide.setVisible(false);
    closeButtonImage2.setVisible(false);
  }

  @FXML
  public void nxtImg() {
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
    mediaView.setVisible(false);
    closeButtonImage1.setVisible(false);
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
    App.changeCursor("hover");
  }

  @FXML
  private void onExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    App.changeCursor("default");
  }

  @FXML
  private void toggleMenu() {
    if (camSlide.isVisible()) {
      onCloseButton2Pressed();
    }
    boolean isVisible = suspectMenu.isVisible();
    onCloseButton2Pressed();

    if (!isVisible) {
      // Close the objectives menu if it's open
      if (objectiveMenu.isVisible()) {
        closeObjectivesMenu();
      }

      // Rotate animation for the arrow button
      RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), arrowButton);
      rotateTransition.setByAngle(180);

      // Move the arrow button left or right
      TranslateTransition translateTransition =
          new TranslateTransition(Duration.millis(300), arrowButton);
      translateTransition.setByX(140);

      // Slide the menu in
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), suspectMenu);
      suspectMenu.setTranslateX(-suspectMenu.getWidth());
      suspectMenu.setVisible(true);
      menuTransition.setFromX(-suspectMenu.getWidth());
      menuTransition.setToX(0);

      // Play animations
      rotateTransition.play();
      translateTransition.play();
      menuTransition.play();
    } else {
      // Slide out
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), suspectMenu);
      menuTransition.setFromX(0);
      menuTransition.setToX(-suspectMenu.getWidth());

      // Play animation
      menuTransition.play();

      // Toggle visibility after the animation completes (for sliding out)
      menuTransition.setOnFinished(event -> suspectMenu.setVisible(false));

      // Rotate and move the arrow button back
      RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), arrowButton);
      rotateTransition.setByAngle(-180);

      TranslateTransition translateTransition =
          new TranslateTransition(Duration.millis(300), arrowButton);
      translateTransition.setByX(-140);

      rotateTransition.play();
      translateTransition.play();
    }
  }

  private void closeObjectivesMenu() {
    if (objectiveMenu.isVisible()) {
      // Slide the menu out
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), objectiveMenu);
      menuTransition.setFromY(0);
      menuTransition.setToY(-objectiveMenu.getHeight());

      TranslateTransition closeTransition =
          new TranslateTransition(Duration.millis(300), objectiveClose);
      closeTransition.setFromY(0);
      closeTransition.setToY(-objectiveMenu.getHeight());

      // Disable the close button and hide it once the menu is hidden
      menuTransition.setOnFinished(
          event -> {
            objectiveMenu.setVisible(false);
            objectiveClose.setVisible(false); // Hide the close button
            objectiveClose.setDisable(true); // Disable the close button
          });

      // Play animation
      menuTransition.play();
      closeTransition.play();
    }
  }

  @FXML
  private void toggleObjectives() {
    if (!objectiveMenu.isVisible()) {
      // Close the suspect menu if it's open
      if (suspectMenu.isVisible()) {
        toggleMenu(); // This will close the suspectMenu
      }

      // Ensure the menu is off-screen before showing it
      objectiveMenu.setTranslateY(-objectiveMenu.getHeight());
      objectiveClose.setTranslateY(-objectiveMenu.getHeight());
      objectiveMenu.setVisible(true);
      objectiveClose.setVisible(true); // Show the close button
      objectiveClose.setDisable(false); // Enable the close button

      // Slide the menu in
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), objectiveMenu);
      menuTransition.setFromY(-objectiveMenu.getHeight());
      menuTransition.setToY(0);

      TranslateTransition closeTransition =
          new TranslateTransition(Duration.millis(300), objectiveClose);
      closeTransition.setFromY(-objectiveMenu.getHeight());
      closeTransition.setToY(0);

      // Play animations
      menuTransition.play();
      closeTransition.play();
    }
  }

  @FXML
  private void closeObjectives() {
    closeObjectivesMenu();
  }

  @FXML
  public void rotate() {
    if (!clueVisible) {
      return;
    }
    RotateTransition rotateOut = new RotateTransition(Duration.millis(250), photoClue);
    rotateOut.setByAngle(90);
    rotateOut.setAxis(Rotate.X_AXIS);

    RotateTransition rotateIn = new RotateTransition(Duration.millis(250), photoClue);
    rotateIn.setByAngle(90);
    rotateIn.setAxis(Rotate.X_AXIS);

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
    objectivesManager.completeObjectiveStep(1);
    onCloseButton1Pressed();
    onCloseButton2Pressed();
    if (!clueVisible) {
      clueVisible = true;
      flipLabel.setVisible(true);
      photoClue.setVisible(true);
      cross.setVisible(true);
    }
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
    App.changeGuessing();
    // }
  }
}
