package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameTimer;
import nz.ac.auckland.se206.TimerManager;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the guessing view. Handles user interactions within the room where the user
 * can guess the profession of the suspect.
 */
public class GuessingController {
  private static ChatCompletionRequest chatCompletionRequest;
  @FXML private Button submit;
  @FXML private Circle arcborder;
  @FXML private Circle journborder;
  @FXML private Circle guideborder;
  @FXML private Label timerLabel;
  @FXML private TextArea textArea;
  @FXML private ProgressIndicator progressIndicator;

  private GameTimer gameTimer;
  private int suspect = 0;

  /**
   * Initializes the guessing view.
   *
   * @throws IOException if an I/O error occurs
   * @throws URISyntaxException if a URI syntax error occurs
   */
  @FXML
  private void initialize() throws IOException, URISyntaxException {
    progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    arcborder.setVisible(false);
    journborder.setVisible(false);
    guideborder.setVisible(false);
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
                  if (totalSeconds == 0) {
                    handleTimerEnd();
                  }
                  return String.format("%02d:%02d", minutes, seconds);
                },
                gameTimer.timeInSecondsProperty()));

    try {
      // loads the template for the chat
      String txt =
          PromptEngineering.loadTemplate(
              PromptEngineering.class
                  .getClassLoader()
                  .getResource("prompts/guessChat.txt")
                  .toURI());
      // reads the config file
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setMaxTokens(100);
      // sets up the chat
      Task<Void> setup =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              runGpt(new ChatMessage("system", txt));
              return null;
            }
          };
      // starts the thread
      Thread thread = new Thread(setup);
      thread.start();
    } catch (ApiProxyException e) {
      e.printStackTrace();
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

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  /**
   * Handles the key typed event.
   *
   * @param event the key event
   */
  @FXML
  private void onArcHover(MouseEvent event) {
    // if the mouse hovers over the image, the image will scale up
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    arcborder.setScaleX(1.2);
    arcborder.setScaleY(1.2);
    // the cursor will change to a hand when the mouse hovers over the image
    App.changeCursor("HAND");
  }

  /**
   * Handles the mouse hover event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onJournHover(MouseEvent event) {
    // when the mouse hovers over the image, the image will scale up
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    journborder.setScaleX(1.2);
    journborder.setScaleY(1.2);
    // the cursor also changes to a hand when hovered over the image
    App.changeCursor("HAND");
  }

  /**
   * Handles the mouse hover event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onGuideHover(MouseEvent event) {
    // make the image scale up when the mouse hovers over the image
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    guideborder.setScaleX(1.2);
    guideborder.setScaleY(1.2);
    // change the cursor to a hand when the mouse hovers over the image
    App.changeCursor("HAND");
  }

  /**
   * Handles the mouse exit event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onArcExit(MouseEvent event) {
    // when the mouse exits the image, the image will scale back to normal
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    arcborder.setScaleX(1);
    arcborder.setScaleY(1);
    // when the mouse exits the image, the cursor will go back to default
    App.changeCursor("default");
  }

  /**
   * Handles the mouse exit event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onJournExit(MouseEvent event) {
    // sets the values back to normal when the mouse exits the image
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    journborder.setScaleX(1);
    journborder.setScaleY(1);
    // sets the cursor back to default when the mouse exits the image
    App.changeCursor("default");
  }

  /**
   * Handles the mouse exit event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onGuideExit(MouseEvent event) {
    // when exiting the image, the image will scale back to normal
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    guideborder.setScaleX(1);
    guideborder.setScaleY(1);
    // the cursor will be set to the default cursor when the mouse exits the image
    App.changeCursor("default");
  }

  /**
   * Handles the mouse click event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onClick(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    // basically allows the user to select a suspect
    // the suspect clicked will be highlighted
    // we allow for suspects to be changed
    if (hoveredImageView.getId().equals("arc")) {
      journborder.setVisible(false);
      guideborder.setVisible(false);
      arcborder.setVisible(true);
      suspect = 1;
    }
    if (hoveredImageView.getId().equals("journ")) {
      arcborder.setVisible(false);
      guideborder.setVisible(false);
      journborder.setVisible(true);
      suspect = 2;
    }
    if (hoveredImageView.getId().equals("guide")) {
      arcborder.setVisible(false);
      journborder.setVisible(false);
      guideborder.setVisible(true);
      suspect = 3;
    }
    // check if the submit button should be enabled
    checkSubmitEnabled();
  }

  /** Handles the text change event. */
  @FXML
  private void changeText() {
    checkSubmitEnabled();
  }

  /** Checks if the submit button should be enabled. */
  @FXML
  private boolean checkSubmitEnabled() {
    // if the suspect has been selected and the text area is not empty, the submit button will be
    // enabled
    if (suspect != 0 && !textArea.getText().isEmpty()) {
      submit.setDisable(false);
      return true;
    } else {
      submit.setDisable(true);
      return false;
    }
  }

  /**
   * Handles the submit button click event.
   *
   * @param event the mouse event
   * @throws IOException if an I/O error occurs
   */
  @FXML
  private void handleSubmit(MouseEvent event) throws IOException {
    TimerManager.stopTimer();
    Task<Void> getResponse =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            Platform.runLater(() -> progressIndicator.setVisible(true));
            ChatMessage feedback = runGpt(new ChatMessage("system", textArea.getText()));
            Platform.runLater(
                () -> {
                  try {
                    App.changeGameOver(suspect, feedback.getContent());
                  } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                });
            return null;
          }
        };
    Thread thread = new Thread(getResponse);
    thread.start();
  }

  /** Handles when the timer ends. */
  private void handleTimerEnd() {
    TimerManager.stopTimer();
    // when the timer has been stopped, the users progress will be checked.
    if (!checkSubmitEnabled()) {
      try {
        // if the user hasn't made a guess, the game will end
        App.changeGameOver(0, "ran out of time, you didnt make a guess!!");
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try {
        // otherwise the user's guess will be submitted even after running out of time
        handleSubmit(null);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    // adds our message to the AI history
    chatCompletionRequest.addMessage(msg);
    // gets our response from the AI model based on our prompt and returns it
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
