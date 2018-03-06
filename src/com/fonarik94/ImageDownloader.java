package com.fonarik94;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class ImageDownloader {
    public void downloadImage(String workDirPath) {
        try {
            URL url = new URL("https://source.unsplash.com/random/?nature");
            HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
//            String workDir = System.getenv("TEMP") + "\\img.jpg";
            try (InputStream inputStream = new BufferedInputStream(httpsConnection.getInputStream());
                 OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(workDirPath))) {
                if (httpsConnection.getResponseCode() == 200) {
                    byte[] buffer = new byte[1024 * 8]; //Buffer size 8KB
                    int length = 0;
                    while ((length = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    System.out.println("Image downloaded");
                }
                else {
                    System.out.println("Image downloading error. HTTP response code: " + httpsConnection.getResponseCode());
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
