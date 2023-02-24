

package listfix.view.support;

public final class ProgressAdapter<T> implements IProgressObserver<T>
{
  public static <T> ProgressAdapter<T> wrap(IProgressObserver<T> observer)
  {
    if (observer instanceof ProgressAdapter)
      return (ProgressAdapter<T>) observer;
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

  public long getCompleted()
  {
    return _completed;
  }

  public void setCompleted(long completed)
  {
    _completed = completed;
    refreshPercentComplete();
  }

  private long _completed;

  public void stepCompleted()
  {
    _completed += 1;
    refreshPercentComplete();
  }

  public void stepCompleted(long done)
  {
    _completed += done;
    refreshPercentComplete();
  }

  public long getTotal()
  {
    return _total;
  }

  public void setTotal(long total)
  {
    boolean report = isValid() && _percentComplete != 0 && total != 0;
    _total = total;
    _completed = 0;
    _percentComplete = 0;
    if (report)
      reportProgress(_percentComplete);
  }

  private long _total;


  private void refreshPercentComplete()
  {
    if (isValid())
    {
      double pct = Math.round(((double)_completed * 100.0) / (double)_total);
      if (pct != _percentComplete)
      {
        _percentComplete = (int) pct;
        reportProgress(_percentComplete);
      }
    }
  }

  private boolean isValid()
  {
    return _total > 0 && _observer != null;
  }

  private int _percentComplete;

  public boolean getCancelled()
  {
    return _observer != null ? _observer.getCancelled() : false;
  }

}
