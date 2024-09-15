package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;

public class ObjectivesManager {

  private static ObjectivesManager instance;
  private boolean[] objectivesCompleted;
  private int[] objectivesProgress; // New: Track progress for each objective
  private int[]
      stepsRequiredForObjectives; // New: Specify how many steps are required for each objective
  private List<Runnable> observers = new ArrayList<>();

  private ObjectivesManager() {
    initializeObjectives(
        2, new int[] {3, 1}); // For example, 3 steps for objective 0, 1 step for objective 1
  }

  public static ObjectivesManager getInstance() {
    if (instance == null) {
      instance = new ObjectivesManager();
    }
    return instance;
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
    }
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
