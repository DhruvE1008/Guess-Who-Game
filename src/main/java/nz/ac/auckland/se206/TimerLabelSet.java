package nz.ac.auckland.se206;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

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

          // if the timer reaches 0, check if the objective is completed
          if (totalSeconds == 0) {
            if (!App.getObjectiveCompleted()) {
              App.changeGameOver(0, "ran out of time, you didn't interact with the scenes enough!");
            } else {
              App.changeGuessing();
            }
          }

          // format the time to display in minutes and seconds
          return String.format("%02d:%02d", minutes, seconds);
        },
        // bind the timer label to the game timer
        gameTimer.timeInSecondsProperty());
  }
}
