package nz.ac.auckland.se206;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.MediaPlayer;

public class VolumeManager {
  private static VolumeManager instance;

  // Volume is now in the range 0 to 100 (consistent with the slider)
  private DoubleProperty volume = new SimpleDoubleProperty(50); // Default volume is 50%

  private VolumeManager() {}

  public static VolumeManager getInstance() {
    if (instance == null) {
      instance = new VolumeManager();
    }
    return instance;
  }

  public DoubleProperty volumeProperty() {
    return volume;
  }

  public double getVolume() {
    return volume.get();
  }

  public void setVolume(double volume) {
    this.volume.set(volume);
  }

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
