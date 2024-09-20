package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
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
import nz.ac.auckland.se206.SuspectOverlay;
import nz.ac.auckland.se206.TimerLabelSet;
import nz.ac.auckland.se206.TimerManager;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class TourGuideController {
  private static GameStateContext context = new GameStateContext();
  private static boolean isFirstTimeInit = true;
  private static boolean isFirstTime = true;
  private static boolean isFirstMessage = true;
  private static ChatCompletionRequest chatCompletionRequest;

  public static void setisFirstTime() {
    isFirstTimeInit = true;
  }

  @FXML
  public static void setFirstMessage() {
    isFirstMessage = true;
  }

  @FXML private Button btnGuess;
  @FXML private Button arrowButton;
  @FXML private Button btnObjectives;
  @FXML private Button objectiveClose;
  @FXML private ImageView crimeScene;
  @FXML private ImageView archaeologist;
  @FXML private ImageView journalist;
  @FXML private ImageView guide;
  @FXML private ImageView guidebubble;
  @FXML private Label timerLabel;
  @FXML private Text objective1Label;
  @FXML private Text objective2Label;
  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private VBox suspectMenu;
  @FXML private VBox objectiveMenu;

  private GameTimer gameTimer;
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
    timerLabel.textProperty().bind(TimerLabelSet.createTimerStringBinding(gameTimer));
    guidebubble.setVisible(false);
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
                        "Tour Guide: I hope you find the idol because it has a special place in my"
                            + " heart as I believe that it belongs to my ancestors");
                    Media sound = null;
                    try {
                      sound =
                          new Media(App.class.getResource("/sounds/guide.mp3").toURI().toString());
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

  @FXML
  private void onToggleMenu() {
    SuspectOverlay.toggleMenu(suspectMenu, arrowButton);
  }

  @FXML
  private void toggleObjectivesGuide() {
    SuspectOverlay.toggleObjectives(objectiveMenu, objectiveClose, suspectMenu, this::onToggleMenu);
  }

  @FXML
  private void closeObjectivesGuide() {
    SuspectOverlay.closeObjectivesMenu(objectiveMenu, objectiveClose);
  }

  @FXML
  private void onHoverGuide(MouseEvent event) {
    SuspectOverlay.onHover(event);
  }

  @FXML
  private void onExitGuide(MouseEvent event) {
    SuspectOverlay.onExit(event);
  }

  @FXML
  private void onProfileClickGuide(MouseEvent event) throws IOException {
    if (player != null) {
      player.stop();
    }
    ImageView clickedImageView = (ImageView) event.getSource();
    context.handleProfileClick(event, clickedImageView.getId());
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

  @FXML
  private void onSendMessage(ActionEvent event) {
    // if this is the first message, complete the first objective step
    if (isFirstMessage) {
      objectivesManager.completeObjectiveStep(0);
      isFirstMessage = false;
    }
    // if this is the first time talking to the suspect, you should clear the chat
    if (isFirstTime) {
      txtaChat.clear();
      isFirstTime = false;
    }
    String message = txtInput.getText().trim();
    // if the message is empty, do nothing
    if (message.isEmpty()) {
      return;
    }
    txtInput.clear();
    Task<Void> getResponse =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // set the guide bubble to visible
            Platform.runLater(() -> guidebubble.setVisible(true));
            try {
              ChatMessage msg = new ChatMessage("user", message);
              ChatMessage response = runGpt(new ChatMessage("system", msg.getContent()));
              // add the user message and AI response to the chat area
              context.handleSendChatClick(txtaChat, message, "Tour Guide", response.getContent());
              Platform.runLater(() -> guidebubble.setVisible(false));
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

  public void updateObjectiveLabels() {
    SuspectOverlay.updateObjectiveLabels(objectivesManager, objective1Label, objective2Label);
  }

  private void getSystemPrompt() {
    // sets the values for the system prompt
    Map<String, String> map = new HashMap<>();
    map.put("profession", "a tour guide who believes that the idol belongs to his ancestors");
    map.put("shoeSize", "7");
    map.put("reason", "you were alone at your workplace reviewing the tours for the day alone");
    map.put("kids", "a 9 year old daughter");
    String message = PromptEngineering.getPrompt("chat.txt", map);
    // configures the AI for the chat and sends the system prompt
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
    // adds the message to AI history
    chatCompletionRequest.addMessage(msg);
    // gets the AI response and returns it
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
}
