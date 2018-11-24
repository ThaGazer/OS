import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

public class AsynchronousFileChannelFactory {
  public static AsynchronousFileChannel open(Path file, ExecutorService executor)
      throws IOException {
    return AsynchronousFileChannel.open(file, Collections.<OpenOption>emptySet(), executor);
  }
}