package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import nz.ac.auckland.se206.controllers.ChatController;
import nz.ac.auckland.se206.controllers.GameOverController;
import nz.ac.auckland.se206.controllers.MenuController;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.speech.FreeTextToSpeech;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {
  private static Stage primaryStage;
  private static Scene scene;
  private static RoomController roomController;
  private static boolean isObjectiveCompleted = false;

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  /**
   * Sets the root of the scene to the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFxml(fxml));
  }

  public static Stage getPrimaryStage() {
    return primaryStage;
  }

  public static void changeArchaeologist(MouseEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/archaeologist.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    scene = new Scene(root);
    scene.setCursor(Cursor.DEFAULT);
    stage.setScene(scene);
    stage.show();
  }

  public static void changeJournalist(MouseEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/journalist.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    scene = new Scene(root);
    scene.setCursor(Cursor.DEFAULT);
    stage.setScene(scene);
    stage.show();
  }

  public static void changeGuide(MouseEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/tourGuide.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    scene = new Scene(root);
    scene.setCursor(Cursor.DEFAULT);
    stage.setScene(scene);
    stage.show();
  }

  public static void changeCrimeScene(MouseEvent event) throws IOException {
    Task<Void> loadSceneTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/room.fxml"));
            Parent root = loader.load();
            roomController = loader.getController();

            Platform.runLater(
                () -> {
                  try {
                    // Get the current stage
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    // Create a new scene and set up the key event handler
                    scene = new Scene(root);
                    scene.setOnKeyPressed(
                        new EventHandler<KeyEvent>() {
                          @Override
                          public void handle(KeyEvent event) {
                            if (event.getCode() == KeyCode.F) {
                              roomController.rotate(); // Rotate the room when 'F' key is pressed
                            } else if (event.getCode() == KeyCode.S) {
                              roomController.scanFootprint();
                            }
                          }
                        });

                    // Set the new scene on the stage
                    stage.setScene(scene);
                    stage.show();
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                });

            return null;
          }
        };

    Thread backgroundThread = new Thread(loadSceneTask);
    backgroundThread.setDaemon(true);
    backgroundThread.start();
  }

  public static void changeGuessing() throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/guessing.fxml"));
    Parent root = loader.load();

    Stage stage =
        (Stage) App.getPrimaryStage().getScene().getWindow(); // Adjusted to use App's primary stage
    scene = new Scene(root);

    scene.setCursor(Cursor.DEFAULT);
    stage.setScene(scene);
    stage.show();
  }


  public static void changeBackStory(MouseEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/backStory.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    scene = new Scene(root);
    scene.setCursor(Cursor.DEFAULT);
    stage.setScene(scene);
    stage.show();
  }


  public static void changeGameOver(int suspect, String feedback) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/gameOver.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) App.getPrimaryStage().getScene().getWindow();
    scene = new Scene(root);
    GameOverController controller = loader.getController();
    controller.setSuspect(suspect);
    controller.setFeedback(feedback);
    scene.setCursor(Cursor.DEFAULT);
    stage.setScene(scene);
    stage.show();
  }

  public static void changeCursor(String cursor) {
    if (cursor.equals("hover")) {
      scene.setCursor(
          new ImageCursor(new Image(App.class.getResourceAsStream("/images/cursor.png"))));
    } else if (cursor.equals("HAND")) {
      scene.setCursor(Cursor.HAND);
    } else {
      scene.setCursor(Cursor.DEFAULT);
    }
  }

  /**
   * Loads the FXML file and returns the associated node. The method expects that the file is
   * located in "src/main/resources/fxml".
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the root node of the FXML file
   * @throws IOException if the FXML file is not found
   */
  private static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * Opens the chat view and sets the profession in the chat controller.
   *
   * @param event the mouse event that triggered the method
   * @param profession the profession to set in the chat controller
   * @throws IOException if the FXML file is not found
   */
  public static void openChat(MouseEvent event, String profession) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/chat.fxml"));
    Parent root = loader.load();

    ChatController chatController = loader.getController();
    chatController.setProfession(profession);

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    scene = new Scene(root);
    stage.setScene(scene);
    stage.show();
  }

  public static void restartGame() {
    try {
      exitGame();
      App app = new App();
      app.start(new Stage());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void setGuessButton() {
    roomController.setGuessButton();
    isObjectiveCompleted = true;
  }

  public static boolean getObjectiveCompleted() {
    return isObjectiveCompleted;
  }

  public static void exitGame() {
    Stage stage = (Stage) scene.getWindow();
    stage.close();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "room" scene.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/room.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    TimerManager.initializeTimer(5);
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
    primaryStage = stage;
    Parent root = loader.load();
    MenuController controller = loader.getController();

    scene = new Scene(root);
    stage.setScene(scene);
    stage.show();
    stage.setOnCloseRequest(event -> handleWindowClose(event));
    root.requestFocus();
  }

  private void handleWindowClose(WindowEvent event) {
    FreeTextToSpeech.deallocateSynthesizer();
  }
}
