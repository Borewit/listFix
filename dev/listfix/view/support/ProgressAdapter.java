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

public class ProgressAdapter<T> implements IProgressObserver<T>
{
    public static <T> ProgressAdapter<T> wrap(IProgressObserver<T> observer)
    {
        if (observer instanceof ProgressAdapter)
            return (ProgressAdapter<T>)observer;
        else
            return new ProgressAdapter<T>(observer);
    }

    private ProgressAdapter(IProgressObserver<T> observer)
    {
        _observer = observer;
    }

    public void reportProgress(int progress)
    {
        if (_observer != null)
            _observer.reportProgress(progress);
    }

    public void reportProgress(int progress, T state)
    {
        if (_observer != null)
            _observer.reportProgress(progress, state);
    }

    private IProgressObserver _observer;




    public int getCompleted()
    {
        return _completed;
    }
    public void setCompleted(int completed)
    {
        _completed = completed;
        refreshPercentComplete();
    }
    private int _completed;

    public void stepCompleted()
    {
        _completed += 1;
        refreshPercentComplete();
    }

    public void stepCompleted(int done)
    {
        _completed += done;
        refreshPercentComplete();
    }

    public int getTotal()
    {
        return _total;
    }
    public void setTotal(int total)
    {
        boolean report = isValid() && _percentComplete != 0 && total != 0;
        _total = total;
        _completed = 0;
        _percentComplete = 0;
        if (report)
            reportProgress(_percentComplete);
    }
    private int _total;


    private void refreshPercentComplete()
    {
        if (isValid())
        {
            int pct = _completed * 100 / _total;
            if (pct != _percentComplete)
            {
                _percentComplete = pct;
                reportProgress(pct);
            }
        }
    }

    private boolean isValid()
    {
        return _total > 0 && _observer != null;
    }

    private int _percentComplete;

}
