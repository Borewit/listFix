
package listfix.view.support;

public class DualProgressAdapter<T> implements IDualProgressObserver<T>
{
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
        };
        _overall = ProgressAdapter.wrap(overallObserver);
    }

    public void reportTaskProgress(int percentComplete, T state)
    {
        if (_observer != null)
            _observer.reportTaskProgress(percentComplete, state);
    }

    public void reportOverallProgress(int percentComplete, T state)
    {
        if (_observer != null)
            _observer.reportOverallProgress(percentComplete, state);
    }

    IDualProgressObserver<T> _observer;

    public ProgressAdapter<T> getTask()
    {
        return _task;
    }
    ProgressAdapter<T> _task;

    public ProgressAdapter<T> getOverall()
    {
        return _overall;
    }
    ProgressAdapter<T> _overall;

}
