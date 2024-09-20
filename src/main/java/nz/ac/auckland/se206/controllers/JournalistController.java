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
public class JournalistController {
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
  @FXML private Button objectiveClose;
  @FXML private Button btnObjectives;
  @FXML private Button arrowButton;
  @FXML private ImageView crimeScene;
  @FXML private ImageView archaeologist;
  @FXML private ImageView journalist;
  @FXML private ImageView guide;
  @FXML private ImageView journbubble;
  @FXML private Label timerLabel;
  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private Text objective1Label;
  @FXML private Text objective2Label;
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

  @FXML
  private void handleToggleMenu() {
    SuspectOverlay.toggleMenu(suspectMenu, arrowButton);
  }

  @FXML
  private void togglingObjectives() {
    SuspectOverlay.toggleObjectives(
        objectiveMenu, objectiveClose, suspectMenu, this::handleToggleMenu);
  }

  @FXML
  private void closingObjectives() {
    SuspectOverlay.closeObjectivesMenu(objectiveMenu, objectiveClose);
  }

  @FXML
  private void onHovering(MouseEvent event) {
    SuspectOverlay.onHover(event);
  }

  @FXML
  private void onExiting(MouseEvent event) {
    SuspectOverlay.onExit(event);
  }

  @FXML
  private void onProfileClicking(MouseEvent event) throws IOException {
    if (player != null) {
      player.stop();
    }
    ImageView clickedImageView = (ImageView) event.getSource();
    context.handleProfileClick(event, clickedImageView.getId());
  }

  @FXML
  private void onSendMessage(ActionEvent event) {
    // Complete the first objective step if it's the first message
    if (isFirstMessage) {
      objectivesManager.completeObjectiveStep(0);
      isFirstMessage = false;
    }
    // if its the first time talking to the journalist clear the chat
    if (isFirstTime) {
      txtaChat.clear();
      isFirstTime = false;
    }
    String message = txtInput.getText().trim();
    // if the message is empty do nothing
    if (message.isEmpty()) {
      return;
    }
    txtInput.clear();
    Task<Void> getResponse =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // set the talking bubble to visible
            Platform.runLater(() -> journbubble.setVisible(true));
            try {
              ChatMessage msg = new ChatMessage("user", message);
              ChatMessage response = runGpt(new ChatMessage("system", msg.getContent()));
              // add the user message and the AI response to the chat
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

  // Update the objective labels
  public void updateObjectiveLabels() {
    SuspectOverlay.updateObjectiveLabels(objectivesManager, objective1Label, objective2Label);
  }

  private void getSystemPrompt() {
    // stores all the values for the prompt
    Map<String, String> map = new HashMap<>();
    map.put("profession", "a journalist who desperately needs new stories");
    map.put("shoeSize", "7");
    map.put(
        "reason",
        "you had a big story to cover the next day, so you were at home all night preparing for"
            + " it");
    map.put("kids", "a 9 year old son");
    String message = PromptEngineering.getPrompt("chat.txt", map);
    // sets up the AI chat
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
    // adds the message to the AI history
    chatCompletionRequest.addMessage(msg);
    // gets the response from the AI based on the requirements
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
