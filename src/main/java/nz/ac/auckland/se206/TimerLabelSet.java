package nz.ac.auckland.se206;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

public class TimerLabelSet {
  public static StringBinding createTimerStringBinding(GameTimer gameTimer) {
    return Bindings.createStringBinding(
        () -> {
          int totalSeconds = gameTimer.getTimeInSeconds();
          int minutes = totalSeconds / 60;
          int seconds = totalSeconds % 60;

          if (totalSeconds == 0) {
            if (!App.getObjectiveCompleted()) {
              App.changeGameOver(0, "ran out of time, you didn't interact with the scenes enough!");
            } else {
              App.changeGuessing();
            }
          }

          return String.format("%02d:%02d", minutes, seconds);
        },
        gameTimer.timeInSecondsProperty());
  }
}
