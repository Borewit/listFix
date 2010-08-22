package listfix.view.support;

public interface IDualProgressObserver<T>
{
    void reportTaskProgress(int percentComplete, T state);
    void reportOverallProgress(int percentComplete, T state);
}
