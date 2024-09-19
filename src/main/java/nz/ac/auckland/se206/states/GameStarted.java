package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * The GameStarted state of the game. Handles the initial interactions when the game starts,
 * allowing the player to chat with characters and prepare to make a guess.
 */
public class GameStarted implements GameState {

  private final GameStateContext context;

  /**
   * Constructs a new GameStarted state with the given game state context.
   *
   * @param context the context of the game state
   */
  public GameStarted(GameStateContext context) {
    this.context = context;
  }

  /**
   * Handles the event when a rectangle is clicked. Depending on the clicked rectangle, it either
   * provides an introduction or transitions to the chat view.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    // Transition to chat view or provide an introduction based on the clicked rectangle
    switch (rectangleId) {
      case "rectCashier":
        TextToSpeech.speak("Welcome to my cafe!");
        return;
      case "rectWaitress":
        TextToSpeech.speak("Hi, let me know when you are ready to order!");
        return;
    }
    App.openChat(event, context.getProfession(rectangleId));
  }

  public void handleSendChatClick(
      TextArea chatTextArea, String userInput, String person, String aiResponse) {
    chatTextArea.appendText("\n" + "You: " + userInput + "\n\n");
    chatTextArea.appendText(person + ": " + aiResponse + "\n");
    chatTextArea.setScrollTop(Double.MAX_VALUE);
  }

  public void handleProfileClick(MouseEvent event, String profile) throws IOException {
    // switches scenes based on the profile clicked
    // uses the App class to switch scenes
    switch (profile) {
      case "archaeologist":
        App.changeArchaeologist(event);
        return;
      case "journalist":
        App.changeJournalist(event);
        return;
      case "guide":
        App.changeGuide(event);
        return;
      case "crimeScene":
        App.changeCrimeScene(event);
        return;
    }
  }

  /**
   * Handles the event when the guess button is clicked. Prompts the player to make a guess and
   * transitions to the guessing state.
   *
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleGuessClick() throws IOException {
    TextToSpeech.speak("Make a guess, click on the " + context.getProfessionToGuess());
    context.setState(context.getGuessingState());
  }
}
