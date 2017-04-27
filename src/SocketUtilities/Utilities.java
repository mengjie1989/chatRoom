package SocketUtilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by refuhoo on 4/23/17.
 */
public class Utilities {
    public static long readFromChannel(ByteBuffer buffers, SocketChannel socketChannel) throws IOException {
        long bytesRead = socketChannel.read(buffers);
        return bytesRead;
    }

    public static String readFromChannel(SocketChannel socketChannel, int capacity) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(capacity);
        long bytesRead = socketChannel.read(buff);
        if (bytesRead == 0 || bytesRead == -1) {
            return null;
        } else {
            //System.out.println("Bytes Read : " + bytesRead);
            String str = byteBufferToString(buff, (int)bytesRead);
            return str;
        }
    }

    public static void writeToChannel(ByteBuffer buffer, SocketChannel socketChannel) throws IOException {
        synchronized (socketChannel) {
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }
    }

    public static void writeToChannel(String str, SocketChannel socketChannel) throws IOException {
        ByteBuffer buf = stringToByteBuffer(str);
        writeToChannel(buf, socketChannel);
    }

    private static ByteBuffer stringToByteBuffer(String str) {
        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buff = ByteBuffer.allocate(str.length());

        buff.put(str.getBytes(Charset.forName("UTF-8")));
        buff.flip();
        return buff;
    }

    private static String byteBufferToString(ByteBuffer buff, int len) {
        return new String(buff.array(), 0, len, Charset.forName("UTF-8") );
    }
}
