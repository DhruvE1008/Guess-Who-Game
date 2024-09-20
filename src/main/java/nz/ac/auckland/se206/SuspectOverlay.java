package nz.ac.auckland.se206;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SuspectOverlay {

  public static void rotateButton(Button arrowButton, int angle, double xTranslation) {
    RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), arrowButton);
    rotateTransition.setByAngle(angle);

    TranslateTransition translateTransition =
        new TranslateTransition(Duration.millis(300), arrowButton);
    translateTransition.setByX(xTranslation);

    rotateTransition.play();
    translateTransition.play();
  }

  public static void toggleMenu(VBox menu, Button arrowButton) {
    boolean isVisible = menu.isVisible();
    if (!isVisible) {
      menu.setVisible(true);
      TranslateTransition menuTransition = new TranslateTransition(Duration.millis(300), menu);
      menuTransition.setFromX(-menu.getWidth());
      menuTransition.setToX(0);
      menuTransition.play();
      rotateButton(arrowButton, 180, 140);
    } else {
      TranslateTransition menuTransition = new TranslateTransition(Duration.millis(300), menu);
      menuTransition.setFromX(0);
      menuTransition.setToX(-menu.getWidth());
      menuTransition.setOnFinished(event -> menu.setVisible(false));
      menuTransition.play();
      rotateButton(arrowButton, -180, -140);
    }
  }
}
