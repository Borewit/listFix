/*
 * listFix() - Fix Broken Playlists!
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.view.support;

import javax.swing.SwingWorker;

// implement doInBackground() for easiest use. Designed for the single progress
// bar operation of ProgressDialog.
/**
 *
 * @author jcaron
 * @param <T>
 * @param <V>
 */
public abstract class ProgressWorker<T, V> extends SwingWorker<T, V> implements IProgressObserver<V>
{
  private String _message = "";

  /**
   *
   * @param progress
   */
  public void reportProgress(int progress)
  {
    setProgress(progress);
  }

  /**
   *
   * @param progress
   * @param state
   */
  public void reportProgress(int progress, V state)
  {
    setProgress(progress);
    if (state != null)
    {
      publish(state);
    }
  }

  /**
   *
   * @return
   */
  public boolean getCancelled()
  {
    return this.isCancelled();
  }

  /**
   *
   * @param message
   */
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

  /**
   *
   * @return
   */
  public String getMessage()
  {
    return _message;
  }
}
