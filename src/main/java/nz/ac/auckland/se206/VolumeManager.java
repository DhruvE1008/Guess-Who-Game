package nz.ac.auckland.se206;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.MediaPlayer;

/** VolumeManager class which is used to manage the volume of the MediaPlayer. */
public class VolumeManager {
  private static VolumeManager instance;

  // Volume is now in the range 0 to 100 (consistent with the slider)
  private DoubleProperty volume = new SimpleDoubleProperty(50); // Default volume is 50%

  private VolumeManager() {}

  /**
   * Get the singleton instance of VolumeManager.
   *
   * @return the singleton instance of VolumeManager
   */
  public static VolumeManager getInstance() {
    if (instance == null) {
      instance = new VolumeManager();
    }
    return instance;
  }

  /**
   * Gets the volume property that has been set.
   *
   * @return the volume property
   */
  public DoubleProperty volumeProperty() {
    return volume;
  }

  /**
   * Get the volume that is being used for the players.
   *
   * @return the volume variable
   */
  public double getVolume() {
    return volume.get();
  }

  /**
   * Set the volume variable to a new value.
   *
   * @param volume the volume that has been set
   */
  public void setVolume(double volume) {
    this.volume.set(volume);
  }

  /**
   * Set the volume and add a listener to update the MediaPlayer volume whenever the volume slider
   * is changed.
   *
   * @param player The MediaPlayer to set the volume for
   */
  public static void setVolumeAndListener(MediaPlayer player) {
    // Set initial volume by converting the VolumeManager's 0-100 value to 0.0-1.0
    player.setVolume(VolumeManager.getInstance().getVolume() / 100);

    // Add listener to update mediaPlayer volume whenever the volume slider is changed
    VolumeManager.getInstance()
        .volumeProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              // Convert 0-100 slider value to 0.0-1.0 range for MediaPlayer
              player.setVolume(
                  newValue.doubleValue() / 100); // Dividing by 100 to scale appropriately
            });
  }
}
