package org.giftnotes.anticoin.utilities;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {


    public static String getResponseFromHttpUrl(String POST_URL, String postThisString) throws IOException {

        URL url = new URL(POST_URL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(urlConnection.getOutputStream());
            dStream.writeBytes(postThisString);
            dStream.flush();
            dStream.close();

            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
