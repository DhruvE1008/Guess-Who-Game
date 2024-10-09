package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;

/** ObjectivesManager class which is used to keep track of the objectives in the game. */
public class ObjectivesManager {

  private static ObjectivesManager instance;

  /** Get the singleton instance of ObjectivesManager. */
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

  /** Constructor for ObjectivesManager. */
  private ObjectivesManager() {
    initializeObjectives(
        2, new int[] {3, 1}); // For example, 3 steps for objective 0, 1 step for objective 1
  }

  /**
   * Initialize the objectives arrays with the number of objectives and the number of steps required
   * for each objective.
   *
   * @param numObjectives The objectives to be completed
   * @param stepsRequired The number of steps required for each objective
   */
  public void initializeObjectives(int numObjectives, int[] stepsRequired) {
    objectivesCompleted = new boolean[numObjectives];
    objectivesProgress = new int[numObjectives]; // Initialize progress for each objective
    stepsRequiredForObjectives =
        stepsRequired; // Assign how many steps are required for each objective
  }

  /**
   * Check if an objective is completed.
   *
   * @param index The index of the objective
   * @return true if the objective is completed, false otherwise
   */
  public boolean isObjectiveCompleted(int index) {
    return objectivesCompleted[index];
  }

  /**
   * Get the progress for an objective.
   *
   * @param index The index of the objective
   */
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

  /** Enable the guess button if all objectives are completed. */
  public void enableGuessButton() {
    for (int i = 0; i < objectivesCompleted.length; i++) {
      if (!objectivesCompleted[i]) {
        return;
      }
    }
    App.setGuessButton();
  }

  /**
   * Get the progress for an objective.
   *
   * @param index The index of the objective
   */
  public void resetObjectives() {
    for (int i = 0; i < objectivesCompleted.length; i++) {
      objectivesCompleted[i] = false; // Reset completion status
      objectivesProgress[i] = 0; // Reset progress
    }
    notifyObservers(); // Notify any observers that objectives have been reset
  }

  /**
   * Add an observer to the list of observers.
   *
   * @param observer the text label that depends on the objectives
   */
  public void addObserver(Runnable observer) {
    observers.add(observer);
  }

  /** Notify all observers and makes all the observers run. */
  private void notifyObservers() {
    for (Runnable observer : observers) {
      observer.run();
    }
  }
}
