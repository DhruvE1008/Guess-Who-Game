package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class GuessingController {
  @FXML private Circle arcborder;
  @FXML private Circle journborder;
  @FXML private Circle guideborder;
  @FXML private TextArea textArea;
  @FXML private Button submit;
  @FXML private Label timerLabel;

  private int suspect = 0;
  private static ChatCompletionRequest chatCompletionRequest;
  private GameTimer gameTimer;

  @FXML
  private void initialize() throws IOException, URISyntaxException {
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
    arcborder.setVisible(false);
    journborder.setVisible(false);
    guideborder.setVisible(false);
    try {
      String txt =
          PromptEngineering.loadTemplate(
              PromptEngineering.class
                  .getClassLoader()
                  .getResource("prompts/guessChat.txt")
                  .toURI());
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setMaxTokens(100);
      Task<Void> setup =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              runGpt(new ChatMessage("system", txt));
              return null;
            }
          };
      Thread thread = new Thread(setup);
      thread.start();
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

  @FXML
  private void onArcHover(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    arcborder.setScaleX(1.2);
    arcborder.setScaleY(1.2);
    App.changeCursor("HAND");
  }

  @FXML
  private void onJournHover(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    journborder.setScaleX(1.2);
    journborder.setScaleY(1.2);
    App.changeCursor("HAND");
  }

  @FXML
  private void onGuideHover(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    guideborder.setScaleX(1.2);
    guideborder.setScaleY(1.2);
    App.changeCursor("HAND");
  }

  @FXML
  private void onArcExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    arcborder.setScaleX(1);
    arcborder.setScaleY(1);

    App.changeCursor("default");
  }

  @FXML
  private void onJournExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    journborder.setScaleX(1);
    journborder.setScaleY(1);
    App.changeCursor("default");
  }

  @FXML
  private void onGuideExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    guideborder.setScaleX(1);
    guideborder.setScaleY(1);
    App.changeCursor("default");
  }

  @FXML
  private void onClick(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
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
    checkSubmitEnabled();
  }

  @FXML
  private void changeText() {
    checkSubmitEnabled();
  }

  @FXML
  private void checkSubmitEnabled() {
    if (suspect != 0 && !textArea.getText().isEmpty()) {
      submit.setDisable(false);
    } else {
      submit.setDisable(true);
    }
  }

  @FXML
  private void handleSubmit(MouseEvent event) throws IOException {
    Task<Void> getResponse =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            ChatMessage feedback = runGpt(new ChatMessage("system", textArea.getText()));
            Platform.runLater(
                () -> {
                  try {
                    App.changeGameOver(event, suspect, feedback.getContent());
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
}
