package nz.ac.auckland.se206;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

  public static void onHover(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    App.changeCursor("hover");
  }

  public static void onExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    App.changeCursor("default");
  }

  public static void toggleObjectives(
      VBox objectiveMenu,
      Button objectiveClose,
      VBox suspectMenu,
      Runnable closeSuspectMenuAction) {
    if (!objectiveMenu.isVisible()) {
      // Close the suspect menu if it's open
      if (suspectMenu.isVisible()) {
        closeSuspectMenuAction.run(); // Close the suspect menu
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

  public static void closeObjectivesMenu(VBox objectiveMenu, Button objectiveClose) {
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
}
