package listfix.view.support;

import javax.swing.*;

/**
 * Implement doInBackground() for easiest use. Designed for the single progress
 * bar operation of ProgressDialog.
 */
public abstract class ProgressWorker<T, V> extends SwingWorker<T, V> implements IProgressObserver<V>
{
  private String _message = "";

  @Override
  public void reportProgress(int progress)
  {
    setProgress(progress);
  }

  @Override
  public void reportProgress(int progress, V state)
  {
    setProgress(progress);
    if (state != null)
    {
      publish(state);
    }
  }

  @Override
  public boolean getCancelled()
  {
    return this.isCancelled();
  }

  public void setMessage(String message)
  {
    if (message.equals(_message))
    {
      return;
    }

    String oldMessage = _message;
    _message = message;
    if (getPropertyChangeSupport().hasListeners("message"))
    {
      firePropertyChange("message", oldMessage, _message);
    }
  }

  public String getMessage()
  {
    return _message;
  }
}
