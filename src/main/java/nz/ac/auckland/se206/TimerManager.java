package nz.ac.auckland.se206;

public class TimerManager {
  private static GameTimer gameTimer;

  public static void initializeTimer(int minutes) {
    if (gameTimer == null) {
      gameTimer = new GameTimer(minutes);
    }
  }

  public static GameTimer getGameTimer() {
    return gameTimer;
  }

  public static void startTimer() {
    if (gameTimer != null) {
      gameTimer.start();
    }
  }

  public static void stopTimer() {
    if (gameTimer != null) {
      gameTimer.stop();
    }
  }

  public static void resetTimer() {
    if (gameTimer != null) {
      gameTimer.reset();
    }
  }
}
