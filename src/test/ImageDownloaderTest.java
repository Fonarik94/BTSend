
import com.fonarik94.ImageDownloader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

class ImageDownloaderTest {
    @BeforeAll
    static void setUp(){

    }

    @Test
    void download_correct_Test() throws IOException {
        byte[] result = ImageDownloader.download("https://i.imgur.com/QkWQNQL.jpg");
        assertNotEquals(result, null);
    }
    @Test
    void download_incorrect_Test() throws IOException {
        assertThrows(IOException.class, () -> ImageDownloader.download("https://fonarik94.com"));
    }

    @Test
    void download_null_Test() {
       assertThrows(MalformedURLException.class, () -> ImageDownloader.download(null));
    }
}
