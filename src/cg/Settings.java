package cg;
import java.util.HashMap;

/**
 * Simple settings object storing a hashmap with String-Value pairs.
 * @author Max
 */
public class Settings {
  private HashMap<String, Object> values = new HashMap<String, Object>();
  
  /**
   * Empty constructor for simple settings creation.
   */
  public Settings() {
    // TODO Auto-generated constructor stub
  }
  
  /**
   * Constructor with hashmap param for use when some settings are known already.
   * @param data
   */
  public Settings(HashMap<String, Object> data) {
    values = data;
  }
  
  /**
   * Put a key, value binding in the settings list.
   * @param key The key of the value.
   * @param value The value.
   */
  public void set(String key, Object value) {
    values.put(key, value);
  }
  
  /**
   * Get the value for the key 'key' and cast it to 'T'.
   * @param key The key of the value.
   * @return The resulting value.
   */
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    Object o = values.get(key);
    if (o == null) {
      return null;
    }
    return (T) o; // Unfortunately it is impossible to catch exceptions in this generic cast.
  }
  
  /**
   * Get the hashmap stored in the settings object.
   * @return The stored hashmap.
   */
  public HashMap<String, Object> getData() {
    return values;
  }
}
