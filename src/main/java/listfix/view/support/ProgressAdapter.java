package listfix.view.support;

public class ProgressAdapter<T>
{
  private final IProgressObserver<T> observer;
  private long _total;
  private long completed;
  private int percentComplete;

  private static final IProgressObserver<Object> dummyObserver = new IProgressObserver<>()
  {
    @Override
    public void reportProgress(int progress)
    {
    }

    @Override
    public void reportProgress(int progress, Object state)
    {
    }

    @Override
    public boolean getCancelled()
    {
      return false;
    }
  };

  public static <T> ProgressAdapter<T> make(IProgressObserver<T> observer)
  {
    return new ProgressAdapter<>(observer == null ? (IProgressObserver<T>) dummyObserver : observer);
  }

  public ProgressAdapter(IProgressObserver<T> observer)
  {
    this.observer = observer;
  }

  public long getCompleted()
  {
    return this.completed;
  }

  public void setCompleted(long completed)
  {
    this.completed = completed;
    refreshPercentComplete();
  }

  public void stepCompleted()
  {
    this.completed += 1;
    refreshPercentComplete();
  }

  public void stepCompleted(long done)
  {
    this.completed += done;
    refreshPercentComplete();
  }

  public long getTotal()
  {
    return _total;
  }

  public void setTotal(long total)
  {
    boolean report = isValid() && percentComplete != 0 && total != 0;
    this._total = total;
    this.completed = 0;
    this.percentComplete = 0;
    if (report)
      this.observer.reportProgress(percentComplete);
  }

  private void refreshPercentComplete()
  {
    if (isValid())
    {
      double pct = Math.round(this.calculateProgress(this.completed, this._total));
      if (pct != this.percentComplete)
      {
        this.percentComplete = (int) pct;
        this.observer.reportProgress(this.percentComplete);
      }
    }
  }

  protected double calculateProgress(long completed, long total)
  {
    return (completed * 100.0) / (double) total;
  }

  private boolean isValid()
  {
    return this._total > 0 && this.observer != null;
  }

  public boolean getCancelled()
  {
    return this.observer != null && this.observer.getCancelled();
  }

//  public IProgressObserver<T> getObserver() {
//    return this.observer;
//  }

  public int getPercentComplete()
  {
    return this.percentComplete;
  }

}
