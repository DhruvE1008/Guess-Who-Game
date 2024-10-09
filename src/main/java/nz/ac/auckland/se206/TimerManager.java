package nz.ac.auckland.se206;

public class TimerManager {
  private static GameTimer gameTimer;

  /**
   * Initialize the timer
   *
   * @param minutes
   */
  public static void initializeTimer(int minutes) {
    if (gameTimer == null) {
      gameTimer = new GameTimer(minutes);
    }
  }

  /**
   * Get the game timer
   *
   * @return
   */
  public static GameTimer getGameTimer() {
    return gameTimer;
  }

  /** Start the timer */
  public static void startTimer() {
    if (gameTimer != null) {
      gameTimer.start();
    }
  }

  /** Reset the timer to one minute */
  public static void resetTimerToOneMinute() {
    if (gameTimer != null) {
      gameTimer.resetToOneMinute();
    }
  }

  /** Stop the timer */
  public static void stopTimer() {
    if (gameTimer != null) {
      gameTimer.stop();
    }
  }

  /** Pause the timer */
  public static void resetTimer() {
    if (gameTimer != null) {
      gameTimer.reset();
    }
  }
}
