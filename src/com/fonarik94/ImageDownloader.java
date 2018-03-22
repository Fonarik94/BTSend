package com.fonarik94;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class ImageDownloader {
    public File downloadImage(String workFile) throws IOException {

        URL url = new URL("https://source.unsplash.com/random/?nature");
        HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
        File image = new File(workFile);
        if (!image.exists()) {
            image.createNewFile();
        }
        try (InputStream inputStream = new BufferedInputStream(httpsConnection.getInputStream());
             OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(image))) {
            if (httpsConnection.getResponseCode() == 200) {
                byte[] buffer = new byte[1024 * 8]; //Buffer size 8KB
                int length = 0;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                System.out.println("New image downloaded");
            } else {
                System.out.println("Image downloading error. HTTP response code: " + httpsConnection.getResponseCode());
            }
        }
        return image;
    }
}
