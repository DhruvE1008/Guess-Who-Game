package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.GameTimer;
import nz.ac.auckland.se206.ObjectivesManager;
import nz.ac.auckland.se206.TimerManager;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class JournalistController {

  @FXML private Rectangle rectCashier;
  @FXML private Rectangle rectPerson1;
  @FXML private Rectangle rectPerson2;
  @FXML private Rectangle rectPerson3;
  @FXML private Rectangle rectWaitress;
  @FXML private Button btnGuess;
  @FXML private Text objective1Label;
  @FXML private Text objective2Label;
  @FXML private ImageView crimeScene;
  @FXML private ImageView archaeologist;
  @FXML private ImageView journalist;
  @FXML private ImageView guide;
  @FXML private Button arrowButton;
  @FXML private VBox suspectMenu;
  @FXML private Button btnObjectives;
  @FXML private VBox objectiveMenu;
  @FXML private Button objectiveClose;
  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private ImageView journbubble;
  @FXML private Label timerLabel;

  private static GameStateContext context = new GameStateContext();
  private GameTimer gameTimer;
  private static boolean isFirstTimeInit = true;
  private static boolean isFirstTime = true;
  private static ChatCompletionRequest chatCompletionRequest;
  private MediaPlayer player;
  private ObjectivesManager objectivesManager;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    gameTimer = TimerManager.getGameTimer();

    // Bind the timer label to display the time in minutes and seconds
    timerLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  int totalSeconds = gameTimer.getTimeInSeconds();
                  int minutes = totalSeconds / 60;
                  int seconds = totalSeconds % 60;
                  return String.format("%02d:%02d", minutes, seconds);
                },
                gameTimer.timeInSecondsProperty()));
    journbubble.setVisible(false);
    txtaChat.clear();
    txtInput.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            onSendMessage(null);
          }
        });
    if (isFirstTimeInit) {
      Task<Void> getGreeting =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              Platform.runLater(
                  () -> {
                    txtaChat.setText(
                        "Journalist: I was a really good journalist back in the day but there is a"
                            + " lack of interesting stories these days. If you find the idol it"
                            + " will be a good story for me");
                    Media sound = null;
                    try {
                      sound =
                          new Media(
                              App.class.getResource("/sounds/journalist.mp3").toURI().toString());
                    } catch (URISyntaxException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                    }
                    player = new MediaPlayer(sound);
                    player.play();
                  });
              getSystemPrompt();
              return null;
            }
          };
      Thread thread = new Thread(getGreeting);
      thread.setDaemon(true);
      thread.start();
      isFirstTimeInit = false;
    }
    objectivesManager = ObjectivesManager.getInstance();
    updateObjectiveLabels(); // Initial update

    // Register this controller as an observer to update the UI when objectives are completed
    objectivesManager.addObserver(this::updateObjectiveLabels);
  }

  // Update the objective labels
  public void updateObjectiveLabels() {
    // Update the first objective label
    if (objectivesManager.isObjectiveCompleted(0)) {
      objective1Label.setStyle("-fx-strikethrough: true;");
    }

    // Update the second objective label
    if (objectivesManager.isObjectiveCompleted(1)) {
      objective2Label.setStyle("-fx-strikethrough: true;");
    }
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  @FXML
  private void toggleMenu() {
    boolean isVisible = suspectMenu.isVisible();

    if (!isVisible) {
      // Close the objectives menu if it's open
      if (objectiveMenu.isVisible()) {
        closeObjectivesMenu();
      }

      // Rotate animation for the arrow button
      RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), arrowButton);
      rotateTransition.setByAngle(180);

      // Move the arrow button left or right
      TranslateTransition translateTransition =
          new TranslateTransition(Duration.millis(300), arrowButton);
      translateTransition.setByX(140);

      // Slide the menu in
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), suspectMenu);
      suspectMenu.setTranslateX(-suspectMenu.getWidth());
      suspectMenu.setVisible(true);
      menuTransition.setFromX(-suspectMenu.getWidth());
      menuTransition.setToX(0);

      // Play animations
      rotateTransition.play();
      translateTransition.play();
      menuTransition.play();
    } else {
      // Slide out
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), suspectMenu);
      menuTransition.setFromX(0);
      menuTransition.setToX(-suspectMenu.getWidth());

      // Play animation
      menuTransition.play();

      // Toggle visibility after the animation completes (for sliding out)
      menuTransition.setOnFinished(event -> suspectMenu.setVisible(false));

      // Rotate and move the arrow button back
      RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), arrowButton);
      rotateTransition.setByAngle(-180);

      TranslateTransition translateTransition =
          new TranslateTransition(Duration.millis(300), arrowButton);
      translateTransition.setByX(-140);

      rotateTransition.play();
      translateTransition.play();
    }
  }

  private void closeObjectivesMenu() {
    if (objectiveMenu.isVisible()) {
      // Slide the menu out
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), objectiveMenu);
      menuTransition.setFromY(0);
      menuTransition.setToY(-objectiveMenu.getHeight());

      TranslateTransition closeTransition =
          new TranslateTransition(Duration.millis(300), objectiveClose);
      closeTransition.setFromY(0);
      closeTransition.setToY(-objectiveMenu.getHeight());

      // Disable the close button and hide it once the menu is hidden
      menuTransition.setOnFinished(
          event -> {
            objectiveMenu.setVisible(false);
            objectiveClose.setVisible(false); // Hide the close button
            objectiveClose.setDisable(true); // Disable the close button
          });

      // Play animation
      menuTransition.play();
      closeTransition.play();
    }
  }

  @FXML
  private void toggleObjectives() {
    if (!objectiveMenu.isVisible()) {
      // Close the suspect menu if it's open
      if (suspectMenu.isVisible()) {
        toggleMenu(); // This will close the suspectMenu
      }

      // Ensure the menu is off-screen before showing it
      objectiveMenu.setTranslateY(-objectiveMenu.getHeight());
      objectiveClose.setTranslateY(-objectiveMenu.getHeight());
      objectiveMenu.setVisible(true);
      objectiveClose.setVisible(true); // Show the close button
      objectiveClose.setDisable(false); // Enable the close button

      // Slide the menu in
      TranslateTransition menuTransition =
          new TranslateTransition(Duration.millis(300), objectiveMenu);
      menuTransition.setFromY(-objectiveMenu.getHeight());
      menuTransition.setToY(0);

      TranslateTransition closeTransition =
          new TranslateTransition(Duration.millis(300), objectiveClose);
      closeTransition.setFromY(-objectiveMenu.getHeight());
      closeTransition.setToY(0);

      // Play animations
      menuTransition.play();
      closeTransition.play();
    }
  }

  @FXML
  private void closeObjectives() {
    closeObjectivesMenu();
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  @FXML
  private void onHover(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
  }

  @FXML
  private void onExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
  }

  @FXML
  private void onProfileClick(MouseEvent event) throws IOException {
    ImageView clickedImageView = (ImageView) event.getSource();
    context.handleProfileClick(event, clickedImageView.getId());
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    context.handleRectangleClick(event, clickedRectangle.getId());
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    context.handleGuessClick();
  }

  private void getSystemPrompt() {
    Map<String, String> map = new HashMap<>();
    map.put("profession", "a journalist who desperately needs new stories");
    map.put("shoeSize", "7");
    map.put(
        "reason",
        "you were at the office alone writing a story about software students pushing to main"
            + " branch");
    map.put("kids", "a 9 year old son");
    String message = PromptEngineering.getPrompt("chat.txt", map);
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setMaxTokens(100);
      runGpt(new ChatMessage("system", message));
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  @FXML
  public void onSendMessage(ActionEvent event) {
    objectivesManager.completeObjectiveStep(0);
    if (isFirstTime) {
      txtaChat.clear();
      isFirstTime = false;
    }
    String message = txtInput.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    txtInput.clear();
    Task<Void> getResponse =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            Platform.runLater(() -> journbubble.setVisible(true));
            try {
              ChatMessage msg = new ChatMessage("user", message);
              ChatMessage response = runGpt(new ChatMessage("system", msg.getContent()));
              context.handleSendChatClick(txtaChat, message, "Journalist", response.getContent());
              Platform.runLater(() -> journbubble.setVisible(false));
            } catch (IOException | ApiProxyException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            return null;
          }
        };
    Thread thread = new Thread(getResponse);
    thread.setDaemon(true);
    thread.start();
  }
}
