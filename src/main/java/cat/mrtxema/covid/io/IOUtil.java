package cat.mrtxema.covid.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IOUtil {

    public static String readStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new IOUtilException(e.getMessage(), e);
        }
    }

    public static class IOUtilException extends RuntimeException {

        public IOUtilException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
