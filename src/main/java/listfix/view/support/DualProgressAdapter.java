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

/**
 *
 * @author jcaron
 * @param <T>
 */
public final class DualProgressAdapter<T> implements IDualProgressObserver<T>
{
    /**
   *
   * @param <T>
   * @param observer
   * @return
   */
  public static <T> DualProgressAdapter<T> wrap(IDualProgressObserver<T> observer)
    {
        if (observer instanceof DualProgressAdapter)
            return (DualProgressAdapter<T>)observer;
        else
            return new DualProgressAdapter(observer);
    }

    private DualProgressAdapter(IDualProgressObserver<T> observer)
    {
        _observer = observer;

        IProgressObserver<T> taskObserver = new IProgressObserver<T>()
        {
            public void reportProgress(int progress)
            {
                _observer.reportTaskProgress(progress, null);
            }

            public void reportProgress(int progress, T state)
            {
                _observer.reportTaskProgress(progress, state);
            }

      @Override
      public boolean getCancelled()
      {
        return _observer.getCancelled();
      }
        };
        _task = ProgressAdapter.wrap(taskObserver);

        IProgressObserver<T> overallObserver = new IProgressObserver<T>()
        {
            public void reportProgress(int progress)
            {
                _observer.reportOverallProgress(progress, null);
            }

            public void reportProgress(int progress, T state)
            {
                _observer.reportOverallProgress(progress, state);
            }

      @Override
      public boolean getCancelled()
      {
        return _observer.getCancelled();
      }
        };
        _overall = ProgressAdapter.wrap(overallObserver);
    }

    /**
   *
   * @param percentComplete
   * @param state
   */
  public void reportTaskProgress(int percentComplete, T state)
    {
        if (_observer != null)
            _observer.reportTaskProgress(percentComplete, state);
    }

    /**
   *
   * @param percentComplete
   * @param state
   */
  public void reportOverallProgress(int percentComplete, T state)
    {
        if (_observer != null)
            _observer.reportOverallProgress(percentComplete, state);
    }

    IDualProgressObserver<T> _observer;

    /**
   *
   * @return
   */
  public ProgressAdapter<T> getTask()
    {
        return _task;
    }
    ProgressAdapter<T> _task;

    /**
   *
   * @return
   */
  public ProgressAdapter<T> getOverall()
    {
        return _overall;
    }
    ProgressAdapter<T> _overall;

  /**
   *
   * @return
   */
  @Override
  public boolean getCancelled()
  {
    return _observer.getCancelled();
  }

}
