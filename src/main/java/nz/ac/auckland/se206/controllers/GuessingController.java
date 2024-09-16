package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import nz.ac.auckland.se206.App;

public class GuessingController {
  @FXML private Circle arcborder;
  @FXML private Circle journborder;
  @FXML private Circle guideborder;
  @FXML private TextArea textArea;
  @FXML private Button submit;

  private int suspect = 0;

  @FXML
  private void initialize() {
    arcborder.setVisible(false);
    journborder.setVisible(false);
    guideborder.setVisible(false);
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
    App.changeGameOver(event);
  }
}
