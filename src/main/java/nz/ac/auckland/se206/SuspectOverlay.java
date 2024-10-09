package nz.ac.auckland.se206;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * This class contains methods that are used to animate the suspect overlay and objectives menu. The
 * suspect overlay is a menu that displays information about the suspects in the game.
 */
public class SuspectOverlay {
  /**
   * This method rotates the arrow button by the specified angle and translates it by the specified
   * amount.
   *
   * @param arrowButton the button to opens and closes the overlay
   * @param angle the angle to rotate the button by
   * @param horiTranslation the amount to translate the button horizontally
   */
  public static void rotateButton(Button arrowButton, int angle, double horiTranslation) {
    RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), arrowButton);
    rotateTransition.setByAngle(angle);

    // Translate the button by the specified amount
    TranslateTransition translateTransition =
        new TranslateTransition(Duration.millis(300), arrowButton);
    translateTransition.setByX(horiTranslation);
    // Play animations
    rotateTransition.play();
    translateTransition.play();
  }

  /**
   * This method toggles the suspect menu by sliding it in and out of view.
   *
   * @param menu The suspect menu to toggle
   * @param arrowButton The button that toggles the menu
   */
  public static void toggleMenu(
      VBox menu, Button arrowButton, VBox objectiveMenu, Button objectiveClose) {
    // Check if the menu is visible
    boolean isVisible = menu.isVisible();
    if (objectiveMenu.isVisible()) {
      // Close the objectives menu if it's open
      closeObjectivesMenu(objectiveMenu, objectiveClose);
    }

    // If the menu is not visible, slide it in
    if (!isVisible) {
      menu.setVisible(true);
      TranslateTransition menuTransition = new TranslateTransition(Duration.millis(300), menu);
      menuTransition.setFromX(-menu.getWidth());
      menuTransition.setToX(0);
      menuTransition.play();
      rotateButton(arrowButton, 180, 140);
      // If the menu is visible, slide it out
    } else {
      TranslateTransition menuTransition = new TranslateTransition(Duration.millis(300), menu);
      menuTransition.setFromX(0);
      menuTransition.setToX(-menu.getWidth());
      menuTransition.setOnFinished(event -> menu.setVisible(false));
      menuTransition.play();
      rotateButton(arrowButton, -180, -140);
    }
  }

  /**
   * This method is called when the user hovers over an image view. It scales the image view to 1.2x
   * its original size and changes the cursor to a hand.
   *
   * @param event The mouse event that triggered this method
   */
  public static void onHover(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1.2);
    hoveredImageView.setScaleY(1.2);
    App.changeCursor("HAND");
  }

  /**
   * This method is called when the user exits an image view. It scales the image view back to its
   * original size and changes the cursor to the default cursor.
   *
   * @param event The mouse event that triggered this method
   */
  public static void onExit(MouseEvent event) {
    ImageView hoveredImageView = (ImageView) event.getSource();
    hoveredImageView.setScaleX(1);
    hoveredImageView.setScaleY(1);
    App.changeCursor("default");
  }

  /**
   * This method toggles the objectives menu by sliding it in and out of view.
   *
   * @param objectiveMenu The objectives menu to toggle
   * @param objectiveClose The button that closes the objectives menu
   * @param suspectMenu The suspect menu that should be closed when the objectives menu is opened
   * @param closeSuspectMenuAction The action to run when the suspect menu is closed
   */
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

  /**
   * This method closes the objectives menu by sliding it out of view.
   *
   * @param objectiveMenu The objectives menu to close
   * @param objectiveClose The button that closes the objectives menu
   */
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

  /**
   * This method updates the objective labels based on the objectives manager.
   *
   * @param objectivesManager The objectives manager to get the objectives from
   * @param objective1Label The label for the first objective
   * @param objective2Label The label for the second objective
   */
  public static void updateObjectiveLabels(
      ObjectivesManager objectivesManager, Text objective1Label, Text objective2Label) {
    // Update the first objective label
    if (objectivesManager.isObjectiveCompleted(0)) {
      objective1Label.setStyle("-fx-strikethrough: true;");
    }

    // Update the second objective label
    if (objectivesManager.isObjectiveCompleted(1)) {
      objective2Label.setStyle("-fx-strikethrough: true;");
    }
  }
}
