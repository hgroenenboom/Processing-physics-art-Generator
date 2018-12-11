package cg;
/**
 * Interface for result listeners of the SimpleSettingsGUI
 * @author Max
 */
public interface ResultListener {
  /**
   * Ran when the first button is clicked.
   * @param settings The settings object the GUI generates.
   */
  public void run1(Settings settings);
  /**
   * Ran when the second button is clicked.
   * @param settings The settings object the GUI generates.
   */
  public void run2(Settings settings);
}
