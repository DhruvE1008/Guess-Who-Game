package nz.ac.auckland.se206;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

/**
 * This class contains methods that are used to create a string binding that displays the time in
 * minutes and seconds.
 */
public class TimerLabelSet {

  /**
   * This method creates a string binding that displays the time in minutes and seconds.
   *
   * @param gameTimer The game timer to bind the label to
   * @return The string binding that displays the time in minutes and seconds
   */
  public static StringBinding createTimerStringBinding(GameTimer gameTimer) {
    return Bindings.createStringBinding(
        () -> {
          // get the global timer seconds
          int totalSeconds = gameTimer.getTimeInSeconds();
          int minutes = totalSeconds / 60;
          int seconds = totalSeconds % 60;

          // format the time to display in minutes and seconds
          return String.format("%02d:%02d", minutes, seconds);
        },
        // bind the timer label to the game timer
        gameTimer.timeInSecondsProperty());
  }
}
