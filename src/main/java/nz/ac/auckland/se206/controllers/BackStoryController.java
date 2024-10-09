package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.GameTimer;
import nz.ac.auckland.se206.TimerManager;
import nz.ac.auckland.se206.VolumeManager;

public class BackStoryController {

  private static GameStateContext context = new GameStateContext();

  public static void resetContext() {
    context = new GameStateContext();
  }

  public static GameStateContext getContext() {
    return context;
  }

  private MediaPlayer firstMediaPlayer;
  private MediaPlayer secondMediaPlayer;

  @FXML private Button skipButton;
  @FXML private Button continueButton;
  @FXML private ImageView tombImage;
  @FXML private ImageView image;
  @FXML private Label backstoryLabel;
  @FXML private Label subTitles;
  @FXML private Label timerLabel;
  @FXML private Slider volumeSlider;

  private GameTimer gameTimer;

  // This method initializes the backstory content
  @FXML
  private void initialize() {
    volumeSlider.setMin(0);
    volumeSlider.setMax(100);

    continueButton.setDisable(true); // Disable the continue button initially
    Task<Void> timerTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            TimerManager.startTimer();
            return null;
          }
        };
    new Thread(timerTask).start(); // Run timer in background
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
                  if (totalSeconds == 0
                      && !App.getObjectiveCompleted()
                      && context.getState() == context.getGameStartedState()) {
                    App.changeGameOver(
                        0, "ran out of time, you didn't interact with the scenes enough!");
                  }
                  return String.format("%02d:%02d", minutes, seconds);
                },
                gameTimer.timeInSecondsProperty()));
    // Ensure the image is inside src/main/resources/images/
    URL imageUrl = getClass().getResource("/images/crime_scene.jpg");
    if (imageUrl != null) {
      Image initialImage = new Image(imageUrl.toExternalForm());
      image.setImage(initialImage);
    } else {
      System.out.println("Image not found!");
    }
    volumeSlider.valueProperty().bindBidirectional(VolumeManager.getInstance().volumeProperty());

    playTwoSounds(); // Play the sounds and manage the subtitles
  }

  // Handle the "continue" button click to proceed to the next scene
  @FXML
  private void handleContinue(MouseEvent event) throws IOException {
    App.changeCrimeScene(event); // Navigate to the crime scene or game scene
  }

  @FXML
  private void handleSkip(MouseEvent event) throws IOException {
    // Stop the first media player if it's playing
    if (firstMediaPlayer != null) {
      firstMediaPlayer.stop(); // Stop the audio
    }

    // Stop the second media player if it's already started
    if (secondMediaPlayer != null) {
      secondMediaPlayer.stop(); // Stop the second audio if it has started
      secondMediaPlayer.dispose(); // Release resources
    }

    // Now change the scene after stopping the media players
    App.changeCrimeScene(event); // Navigate to the crime scene
  }

  // Method to change the image in the ImageView
  @FXML
  private void changeImage(String imagePath) {
    FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), image);
    fadeOut.setFromValue(1.0); // Start fully visible
    fadeOut.setToValue(0.0); // Fade out to invisible

    // When fade out finishes, change the image and fade back in
    fadeOut.setOnFinished(
        event -> {
          // Load the new image
          Image newImage = new Image(imagePath);
          image.setImage(newImage);

          // Create a FadeTransition for fading in
          FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), image);
          fadeIn.setFromValue(0.0); // Start invisible
          fadeIn.setToValue(1.0); // Fade in to fully visible
          fadeIn.play(); // Play fade in
        });

    fadeOut.play(); // Start the fade-out transition
  }

  // Method to play two sounds with subtitles
  // Method to play two sounds with subtitles
  private void playTwoSounds() {
    // Load the first sound file
    Media firstSound = new Media(getClass().getResource("/sounds/backstory1.mp3").toExternalForm());
    firstMediaPlayer = new MediaPlayer(firstSound);

    // Set initial volume and apply listener for first media player
    VolumeManager.setVolumeAndListener(firstMediaPlayer);

    // Play the first sound and set the subtitles to type while the audio plays
    firstMediaPlayer.play();
    showSubtitlesForFirstAudio(); // Start the first subtitle typing

    // When the first audio ends, change the image and play the second sound
    firstMediaPlayer.setOnEndOfMedia(
        () -> {
          changeImage("/images/suspects.png"); // Change the image after first audio ends
          image.setX(70);

          // Load the second sound file
          Media secondSound =
              new Media(getClass().getResource("/sounds/backstory2.mp3").toExternalForm());

          secondMediaPlayer = new MediaPlayer(secondSound);
          VolumeManager.setVolumeAndListener(secondMediaPlayer);

          // Set up the second media player to play after the first finishes
          secondMediaPlayer.setOnReady(
              () -> {
                secondMediaPlayer.play(); // Play the second audio once it's ready
                showSubtitlesForSecondAudio(); // Display subtitles for the second audio
              });

          // Enable the continue button after the second audio finishes
          secondMediaPlayer.setOnEndOfMedia(
              () -> {
                continueButton.setDisable(false);
                skipButton.setDisable(true); // Disable skip after both audios
              });
        });
  }

  // Method to display subtitles while the first audio is playing
  private void showSubtitlesForFirstAudio() {
    String firstSubtitles =
        "A priceless golden idol has been stolen from an ancient egyptian tomb...";
    typeSubtitles(firstSubtitles, 0); // Call the typing animation for first audio
  }

  // Method to display subtitles while the second audio is playing
  private void showSubtitlesForSecondAudio() {
    String secondSubtitles = "The last three people at the scene are the main suspects...";
    typeSubtitles(secondSubtitles, 0);
    continueButton.setDisable(false);
    skipButton.setDisable(true); // Enable the continue button after the second audio
  }

  // Method to handle typing animation of subtitles
  private void typeSubtitles(String text, int delayInMillis) {
    subTitles.setText(""); // Clear the label before typing the new subtitles

    Timeline timeline = new Timeline();
    for (int i = 0; i < text.length(); i++) {
      final int index = i;
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.millis(delayInMillis + i * 50), // Adjust the speed of typing here
              event -> subTitles.setText(text.substring(0, index + 1)));
      timeline.getKeyFrames().add(keyFrame);
    }
    timeline.play(); // Play the typing animation
  }
}
