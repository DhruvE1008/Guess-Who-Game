package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;

public class ObjectivesManager {

  private static ObjectivesManager instance;

  public static ObjectivesManager getInstance() {
    if (instance == null) {
      instance = new ObjectivesManager();
    }
    return instance;
  }

  private boolean[] objectivesCompleted;
  private int[] objectivesProgress; // Track progress for each objective
  private int[]
      stepsRequiredForObjectives; // Specify how many steps are required for each objective
  private List<Runnable> observers = new ArrayList<>();

  private ObjectivesManager() {
    initializeObjectives(
        2, new int[] {3, 1}); // For example, 3 steps for objective 0, 1 step for objective 1
  }

  public void initializeObjectives(int numObjectives, int[] stepsRequired) {
    objectivesCompleted = new boolean[numObjectives];
    objectivesProgress = new int[numObjectives]; // Initialize progress for each objective
    stepsRequiredForObjectives =
        stepsRequired; // Assign how many steps are required for each objective
  }

  public boolean isObjectiveCompleted(int index) {
    return objectivesCompleted[index];
  }

  public void completeObjectiveStep(int index) {
    if (!objectivesCompleted[index]) {
      // Increment progress for the objective
      objectivesProgress[index]++;

      // If progress reaches the required steps, mark the objective as completed
      if (objectivesProgress[index] >= stepsRequiredForObjectives[index]) {
        objectivesCompleted[index] = true;
        notifyObservers();
      }
      enableGuessButton();
    }
  }

  public void enableGuessButton() {
    for (int i = 0; i < objectivesCompleted.length; i++) {
      if (!objectivesCompleted[i]) {
        return;
      }
    }
    App.setGuessButton();
  }

  // New: Reset all objectives to their initial state
  public void resetObjectives() {
    for (int i = 0; i < objectivesCompleted.length; i++) {
      objectivesCompleted[i] = false; // Reset completion status
      objectivesProgress[i] = 0; // Reset progress
    }
    notifyObservers(); // Notify any observers that objectives have been reset
  }

  public void addObserver(Runnable observer) {
    observers.add(observer);
  }

  private void notifyObservers() {
    for (Runnable observer : observers) {
      observer.run();
    }
  }
}
