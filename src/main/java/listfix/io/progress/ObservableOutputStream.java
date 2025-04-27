package listfix.io.progress;

import java.io.IOException;
import java.io.OutputStream;
import listfix.view.support.IProgressObserver;

public class ObservableOutputStream extends OutputStream {

  private OutputStream outputStream;
  private final long length;
  private long sumWrite;
  private final IProgressObserver<String> observer;
  private double percent;

  public ObservableOutputStream(
      OutputStream outputStream, long length, IProgressObserver<String> observer) {
    this.outputStream = outputStream;
    this.observer = observer;
    sumWrite = 0;
    this.length = length == 0 ? 10000 : length;
  }

  @Override
  public void write(int i) throws IOException {
    this.outputStream.write(i);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    this.outputStream.write(b, off, len);
    evaluatePercent(len);
  }

  private void evaluatePercent(long writeCount) {
    if (writeCount != -1) {
      sumWrite += writeCount;
      percent = sumWrite * 100.0 / length;
      int progress = Math.min((int) Math.round(percent), 100);
      this.observer.reportProgress(progress);
    }
  }

  @Override
  public void flush() throws IOException {
    this.outputStream.flush();
  }

  @Override
  public void close() throws IOException {
    this.outputStream.close();
  }
}
