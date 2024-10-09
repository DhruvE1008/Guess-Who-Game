package nz.ac.auckland.se206;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/** GameTimer class which is used to keep track of the time in the game */
public class GameTimer {
  private final IntegerProperty timeInSeconds;
  private final int initialTimeInSeconds;
  private Timeline timeline;

  /**
   * Constructor for GameTimer which initializes the live timer in minutes
   *
   * @param startMinutes The initial time in minutes
   */
  public GameTimer(int startMinutes) {
    initialTimeInSeconds = startMinutes * 60;
    timeInSeconds = new SimpleIntegerProperty(initialTimeInSeconds);
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  int currentTime = timeInSeconds.get();
                  if (currentTime > 0) {
                    timeInSeconds.set(currentTime - 1);
                  } else {
                    timeline.stop(); // Stop when time reaches 0
                  }
                }));
    timeline.setCycleCount(Timeline.INDEFINITE); // Run indefinitely
  }

  public void start() {
    timeline.play(); // Start the timer
  }

  public void stop() {
    timeline.stop(); // Stop the timer
  }

  public void guessState() {
    timeline.pause(); // Pause the timer
  }

  public void reset() {
    timeline.stop();
    timeInSeconds.set(initialTimeInSeconds); // Reset to initial time
  }

  public void resetToOneMinute() {
    timeInSeconds.set(60); // Reset to 1 minute
  }

  public IntegerProperty timeInSecondsProperty() {
    return timeInSeconds;
  }

  public int getTimeInSeconds() {
    return timeInSeconds.get();
  }
}
