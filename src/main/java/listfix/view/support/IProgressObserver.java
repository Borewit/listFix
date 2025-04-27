package listfix.view.support;

public interface IProgressObserver<T> {
  void reportProgress(int progress);

  void reportProgress(int progress, T state);

  boolean getCancelled();
}
