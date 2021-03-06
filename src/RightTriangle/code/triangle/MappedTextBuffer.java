/*
 * Author: Justin Ritter
 * Date: 10/27/2018
 * File: MappedTextBuffer.java
 */

package triangle;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class MappedTextBuffer {

  private static final String errNullBuffer = "ByteBuffer can not be null";

  private MappedByteBuffer byteBuffer;

  MappedTextBuffer(MappedByteBuffer buffer) {
    setByteBuffer(buffer);
  }

  private void setByteBuffer(MappedByteBuffer buffer) {
    if(buffer == null) {
      throw new NullPointerException(errNullBuffer);
    }
    byteBuffer = buffer;
  }

  /**
   * Returns a read only ByteBuffer of the backing buffer
   *
   * @return ByteBuffer
   */
  public ByteBuffer getByteBuffer() {
    return byteBuffer.asReadOnlyBuffer();
  }

  public boolean hasRemaining() {
    return byteBuffer.hasRemaining();
  }

  public int position() {
    return byteBuffer.position();
  }

  public MappedByteBuffer position(int position) {
    return byteBuffer.position(position);
  }

  public int nextInt() {
    int ret = nextInt(byteBuffer.position());
    return ret;
  }

  public int nextInt(int pos) {
    StringBuilder intRep = new StringBuilder();
    char c;

    //sets buffer position
    position(pos);
    //loop and add chars until a space or line ending is hit
    while(byteBuffer.hasRemaining() &&
            (c = (char)byteBuffer.get()) != ' ' && c != '\n') {
      intRep.append(c);
      pos++;
    }

    //stupid windows line endings
    if(intRep.length() > 0 && intRep.charAt(intRep.length() - 1) == '\r') {
      intRep.deleteCharAt(intRep.length() - 1);
    }

    return Integer.valueOf(intRep.toString());
  }
}
