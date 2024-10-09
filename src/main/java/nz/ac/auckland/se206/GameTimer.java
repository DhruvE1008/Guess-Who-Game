package nz.ac.auckland.se206;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class GameTimer {
  private final IntegerProperty timeInSeconds;
  private final int initialTimeInSeconds;
  private Timeline timeline;

  /**
   * Constructor for GameTimer
   *
   * @param startMinutes
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

  /** Start the timer */
  public void start() {
    timeline.play(); // Start the timer
  }

  /** Stop the timer */
  public void stop() {
    timeline.stop(); // Stop the timer
  }

  /** Pause the timer */
  public void guessState() {
    timeline.pause(); // Pause the timer
  }

  /** Resume the timer */
  public void reset() {
    timeline.stop();
    timeInSeconds.set(initialTimeInSeconds); // Reset to initial time
  }

  /** Reset the timer to 1 minute */
  public void resetToOneMinute() {
    timeInSeconds.set(60); // Reset to 1 minute
  }

  /**
   * Get the time in seconds
   *
   * @return
   */
  public IntegerProperty timeInSecondsProperty() {
    return timeInSeconds;
  }

  /**
   * Get the time in seconds
   *
   * @return
   */
  public int getTimeInSeconds() {
    return timeInSeconds.get();
  }
}
