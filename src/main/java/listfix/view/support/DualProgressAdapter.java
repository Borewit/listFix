package listfix.view.support;

public final class DualProgressAdapter<T>
{
  private final IDualProgressObserver<T> observer;
  private final ProgressAdapter<T> overall;
  private final IProgressObserver<T> task;
  private int taskPercentageComplete = 0;

  public DualProgressAdapter(IDualProgressObserver<T> observer)
  {
    this.observer = observer;

    this.task = new IProgressObserver<T>()
    {
      @Override
      public void reportProgress(int progress)
      {
        this.reportProgress(progress, null);
      }

      @Override
      public void reportProgress(int progress, T state)
      {
        DualProgressAdapter.this.observer.reportTaskProgress(progress, state);
        // Adjust overall progress
        DualProgressAdapter.this.taskPercentageComplete = progress;
        // Trigger total progress
        DualProgressAdapter.this.overall.setCompleted(DualProgressAdapter.this.overall.getCompleted());
      }

      @Override
      public boolean getCancelled()
      {
        return DualProgressAdapter.this.observer.getCancelled();
      }
    };

    this.overall = new ProgressAdapter<T>(new IProgressObserver<T>()
    {
      @Override
      public void reportProgress(int progress)
      {
        this.reportProgress(progress, null);
      }

      @Override
      public void reportProgress(int progress, T state)
      {
        DualProgressAdapter.this.observer.reportOverallProgress(progress, state);
      }

      @Override
      public boolean getCancelled()
      {
        return DualProgressAdapter.this.observer.getCancelled();
      }
    })
    {
      @Override
      protected double calculateProgress(long completed, long total)
      {
        double taskPortion = (double) DualProgressAdapter.this.taskPercentageComplete / total;
        return super.calculateProgress(completed, total) + taskPortion;
      }
    };
  }

  public IProgressObserver<T> getTask()
  {
    return this.task;
  }

  public ProgressAdapter<T> getOverall()
  {
    return overall;
  }

}
