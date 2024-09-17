package nz.ac.auckland.se206;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class GameTimer {
  private final IntegerProperty timeInSeconds;
  private Timeline timeline = new Timeline();

  public GameTimer(int startMinutes) {
    timeInSeconds =
        new SimpleIntegerProperty(startMinutes * 60); // Initialize with countdown time in seconds
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
    // Reinitialize with the starting time (5 minutes by default)
    timeInSeconds.set(timeInSeconds.get() + 5 * 60);
  }

  public IntegerProperty timeInSecondsProperty() {
    return timeInSeconds;
  }

  public int getTimeInSeconds() {
    return timeInSeconds.get();
  }
}
