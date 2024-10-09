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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
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
import nz.ac.auckland.se206.VolumeManager;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class ArchaeologistController {
  private static GameStateContext archContext = new GameStateContext();
  private static boolean isFirstTimeArchInit = true;
  private static boolean isFirstArchMessage = true;
  private static ChatCompletionRequest archChatCompletionRequest;

  public static void setisFirstTime() {
    isFirstTimeArchInit = true;
  }

  @FXML
  public static void setFirstMessage() {
    isFirstArchMessage = true;
  }

  @FXML private Button arrowButton;
  @FXML private Button btnObjectives;
  @FXML private Button objectiveClose;
  @FXML private ImageView crimeScene;
  @FXML private ImageView archaeologist;
  @FXML private ImageView journalist;
  @FXML private ImageView guide;
  @FXML private ImageView arcbubble;
  @FXML private Label timerLabel;
  @FXML private Text objective1Label;
  @FXML private Text objective2Label;
  @FXML private TextArea archTxtChat;
  @FXML private TextField archTxtInput;
  @FXML private VBox suspectMenu;
  @FXML private VBox objectiveMenu;
  @FXML private Button btnSend;
  @FXML private Label setupLabel;
  @FXML private ProgressIndicator progressIndicator;
  @FXML private Label readyMessageLabel;
  @FXML private Slider volumeSlider;

  private GameTimer gameTimer;
  private MediaPlayer archPlayer;
  private ObjectivesManager objectivesManager;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   *
   * @throws URISyntaxException
   * @throws IOException
   * @throws ApiProxyException
   * @throws InterruptedException
   */
  @FXML
  public void initialize() {
    volumeSlider.setMin(0);
    volumeSlider.setMax(100);
    volumeSlider.valueProperty().bindBidirectional(VolumeManager.getInstance().volumeProperty());
    gameTimer = TimerManager.getGameTimer();

    // Bind the timer label to display the time in minutes and seconds
    timerLabel.textProperty().bind(TimerLabelSet.createTimerStringBinding(gameTimer));
    arcbubble.setVisible(false);
    archTxtChat.clear();
    archTxtInput.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            onSendMessage(null);
          }
        });
    if (isFirstTimeArchInit) {
      archTxtInput.setDisable(true);
      progressIndicator.setVisible(true);
      setupLabel.setVisible(true);
      arrowButton.setDisable(true);
      Task<Void> getGreeting =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              // sets up the chat with the AI by sending an initial message
              Task<Void> systemPromptThread =
                  new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                      // sends initial set up message
                      getSystemPrompt();
                      Platform.runLater(
                          () -> {
                            // sets up the chat so that the user can type a message
                            archTxtInput.setDisable(false);
                            btnSend.setDisable(false);
                            setupLabel.setVisible(false);
                            progressIndicator.setVisible(false);
                            readyMessageLabel.setVisible(true);
                            arrowButton.setDisable(false);
                          });
                      return null;
                    }
                  };
              Thread systemThread = new Thread(systemPromptThread);
              systemThread.setDaemon(true);
              systemThread.start();
              // sets the text up for the archaeologist
              Platform.runLater(
                  () -> {
                    archTxtChat.setText(
                        "Archaeologist: It's terrible that my last excavation ended like this. I"
                            + " can't afford to go on another one because I was recently denied"
                            + " funding.");
                    Media sound = null;
                    // plays the archaeologist's intro sound
                    try {
                      sound =
                          new Media(App.class.getResource("/sounds/arch.mp3").toURI().toString());
                    } catch (URISyntaxException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                    }
                    archPlayer = new MediaPlayer(sound);
                    VolumeManager.setVolumeAndListener(archPlayer);
                    archPlayer.play();
                  });
              return null;
            }
          };
      Thread thread = new Thread(getGreeting);
      thread.setDaemon(true);
      thread.start();
      isFirstTimeArchInit = false;
    }
    objectivesManager = ObjectivesManager.getInstance();
    updateObjectiveLabels(); // Initial update

    // Register this controller as an observer to update the UI when objectives are completed
    objectivesManager.addObserver(this::updateObjectiveLabels);
  }

  /**
   * Handles the hover event for the suspect overlay.
   *
   * @param event
   */
  @FXML
  private void onHover(MouseEvent event) {
    SuspectOverlay.onHover(event);
  }

  /**
   * Handles the exit event for the suspect overlay.
   *
   * @param event
   */
  @FXML
  private void onExit(MouseEvent event) {
    SuspectOverlay.onExit(event);
  }

  /** Toggles the suspect overlay menu. */
  @FXML
  private void onToggleMenu() {
    SuspectOverlay.toggleMenu(suspectMenu, arrowButton, objectiveMenu, objectiveClose);
  }

  /** Toggles the objectives overlay menu. */
  @FXML
  private void toggleObjectives() {
    SuspectOverlay.toggleObjectives(objectiveMenu, objectiveClose, suspectMenu, this::onToggleMenu);
  }

  /** Closes the objectives overlay menu. */
  @FXML
  private void closeObjectives() {
    SuspectOverlay.closeObjectivesMenu(objectiveMenu, objectiveClose);
  }

  /**
   * Handles the click event for the suspect overlay.
   *
   * @param event
   * @throws IOException
   */
  @FXML
  private void onProfileClick(MouseEvent event) throws IOException {
    if (archPlayer != null) {
      archPlayer.stop();
    }
    ImageView clickedImageView = (ImageView) event.getSource();
    archContext.handleProfileClick(event, clickedImageView.getId());
  }

  /**
   * Handles the click event for the continue button.
   *
   * @param event
   * @throws IOException
   * @throws InterruptedException
   * @throws ApiProxyException
   */
  @FXML
  private void onSendMessage(ActionEvent event) {
    // if it's the first message, complete the first objective step for the archaeologist
    if (isFirstArchMessage) {
      objectivesManager.completeObjectiveStep(0);
      isFirstArchMessage = false;
      readyMessageLabel.setVisible(false);
    }
    String message = archTxtInput.getText().trim();
    // if the message is empty, do nothing
    if (message.isEmpty()) {
      return;
    }
    archTxtInput.clear();
    Task<Void> getResponse =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // show the loading bubble
            Platform.runLater(() -> arcbubble.setVisible(true));
            try {
              ChatMessage msg = new ChatMessage("user", message);
              ChatMessage response = runGpt(new ChatMessage("system", msg.getContent()));
              // update the chat area with the user's message and the system's response
              archContext.handleSendChatClick(
                  archTxtChat, message, "Archaeologist", response.getContent());
              Platform.runLater(() -> arcbubble.setVisible(false));
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

  /**
   * Gets the system prompt for the archaeologist.
   *
   * @throws ApiProxyException
   */
  private void getSystemPrompt() {
    // sets the values for the system prompt
    Map<String, String> map = new HashMap<>();
    map.put("profession", "an archaeologist who was recently denied funding");
    map.put("shoeSize", "8");
    map.put(
        "reason",
        "your phone broke last week and you couldn’t get a replacement due to denied funding");
    map.put("kids", "a 9 year old son");
    map.put(
        "interview", "you had an interview by the journalist about your latest find at the studio");
    String message = PromptEngineering.getPrompt("chat.txt", map);
    // sets up the chat with the system prompt
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      archChatCompletionRequest =
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

  /**
   * Updates the objective labels.
   *
   * @throws IOException
   * @throws ApiProxyException
   */
  public void updateObjectiveLabels() {
    SuspectOverlay.updateObjectiveLabels(objectivesManager, objective1Label, objective2Label);
  }

  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    // adds the message to the AI history
    archChatCompletionRequest.addMessage(msg);
    // runs the AI model and gets the response from our prompt
    try {
      ChatCompletionResult chatCompletionResult = archChatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      archChatCompletionRequest.addMessage(result.getChatMessage());
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }
}
