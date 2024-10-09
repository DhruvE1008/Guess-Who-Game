package nz.ac.auckland.se206;

/** This class contains methods that are used to manage the game timer. */
public class TimerManager {
  private static GameTimer gameTimer;

  /**
   * Initializes the live timer that the game will be using.
   *
   * @param minutes The time we want to set the timer to in minutes
   */
  public static void initializeTimer(int minutes) {
    if (gameTimer == null) {
      gameTimer = new GameTimer(minutes);
    }
  }

  /**
   * Get the game timer instance created in the initializeTimer method.
   *
   * @return the game timer instance created in the initializeTimer method
   */
  public static GameTimer getGameTimer() {
    return gameTimer;
  }

  /** Checks if the timer is set and if it is then it starts it. */
  public static void startTimer() {
    if (gameTimer != null) {
      gameTimer.start();
    }
  }

  /** Reset the timer to one minute. */
  public static void resetTimerToOneMinute() {
    if (gameTimer != null) {
      gameTimer.resetToOneMinute();
    }
  }

  /** Checks if a timer exists and if it does then it stops. */
  public static void stopTimer() {
    if (gameTimer != null) {
      gameTimer.stop();
    }
  }

  /** Checks if the timer exists and if it does then it resets to a new time. */
  public static void resetTimer() {
    if (gameTimer != null) {
      gameTimer.reset();
    }
  }
}
