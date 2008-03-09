package listfix.view.support;

public interface ProgressObserver 
  {
  /** Set the progress amount to <code>progress</code>, which is a value
    * between 0 and 100. (Out of range values should be silently clipped.)
    *
    * @progress The percentage of the task completed.
    */

  public void setProgress(int progress);
  }
