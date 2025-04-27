package listfix.io.progress;

import java.io.IOException;
import java.io.InputStream;
import listfix.view.support.IProgressObserver;

public class ObservableInputStream extends InputStream {

  private InputStream inputStream;
  private final long length;
  private long sumRead;
  private final IProgressObserver<String> observer;
  private double percent;

  public ObservableInputStream(
      InputStream inputStream, long length, IProgressObserver<String> observer) {
    this.inputStream = inputStream;
    this.observer = observer;
    sumRead = 0;
    this.length = length;
  }

  @Override
  public int read(byte[] b) throws IOException {
    int readCount = inputStream.read(b);
    evaluatePercent(readCount);
    return readCount;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int readCount = inputStream.read(b, off, len);
    evaluatePercent(readCount);
    return readCount;
  }

  @Override
  public long skip(long n) throws IOException {
    long skip = inputStream.skip(n);
    evaluatePercent(skip);
    return skip;
  }

  @Override
  public int read() throws IOException {
    int read = inputStream.read();
    if (read != -1) {
      evaluatePercent(1);
    }
    return read;
  }

  private void evaluatePercent(long readCount) {
    if (readCount != -1) {
      sumRead += readCount;
      percent = sumRead * 100.0 / length;
      int progress = (int) Math.round(percent);
      this.observer.reportProgress(progress);
    }
  }
}
