package org.elasticsearch.plugin.ingest.rest;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.CheckedSupplier;
import org.elasticsearch.core.SuppressForbidden;
import org.elasticsearch.rest.RestStatus;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

@SuppressWarnings("removal")
class HttpClient {

    byte[] getBytes(String url) throws IOException {
        return get(url).readAllBytes();
    }

    InputStream get(String urlToGet) throws IOException {
        return doPrivileged(() -> {
            String url = urlToGet;
            HttpURLConnection conn = createConnection(url);
            int redirectsCount = 0;
            while (true) {
                switch (conn.getResponseCode()) {
                    case HTTP_OK:
                        return new BufferedInputStream(getInputStream(conn));
                    case HTTP_MOVED_PERM:
                    case HTTP_MOVED_TEMP:
                    case HTTP_SEE_OTHER:
                        if (redirectsCount++ > 50) {
                            throw new IllegalStateException("too many redirects connection to [" + urlToGet + "]");
                        }
                        String location = conn.getHeaderField("Location");
                        URL base = new URL(url);
                        URL next = new URL(base, location); // Deal with relative URLs
                        url = next.toExternalForm();
                        conn = createConnection(url);
                        break;
                    case HTTP_NOT_FOUND:
                        throw new ResourceNotFoundException("{} not found", urlToGet);
                    default:
                        int responseCode = conn.getResponseCode();
                        throw new ElasticsearchStatusException("error during downloading {}",
                                RestStatus.fromCode(responseCode), urlToGet);
                }
            }
        });
    }

    public static InputStream stringToInputStream(String input) {
        // Convert the string to a byte array
        byte[] bytes = input.getBytes();
        // Create an InputStream from the byte array
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

    InputStream post(String urlToGet, String method, String authorization, String content_type,
            String body, Map<String, String> parameters) throws IOException {
        return doPrivileged(() -> {
            String url = urlToGet;
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (url.contains("?")) {
                    url += "&" + entry.getKey() + "=" + entry.getValue();
                } else {
                    url += "?" + entry.getKey() + "=" + entry.getValue();
                }
            }
            // Write the JSON data to the output stream
            byte[] postData = body.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection conn = createConnection(url, method, authorization, content_type, parameters);
            conn.getOutputStream().write(postData);
            int redirectsCount = 0;
            while (true) {
                switch (conn.getResponseCode()) {
                    case HTTP_OK:
                        return new BufferedInputStream(conn.getInputStream());
                    case HTTP_MOVED_PERM:
                    case HTTP_MOVED_TEMP:
                    case HTTP_SEE_OTHER:
                        if (redirectsCount++ > 50) {
                            return new BufferedInputStream(stringToInputStream("too many redirects"));
                            // throw new IllegalStateException("too many redirects connection to [" +
                            // urlToGet + "]");
                        }
                        break;
                    case HTTP_NOT_FOUND:
                        return new BufferedInputStream(stringToInputStream("url not found"));
                    // throw new ResourceNotFoundException("{} not found", urlToGet);
                    default:
                        int responseCode = conn.getResponseCode();
                        return new BufferedInputStream(stringToInputStream(
                                "error during downloading " + responseCode + " " + conn.getResponseMessage()));
                    // throw new ElasticsearchStatusException("error during downloading {}",
                    // RestStatus.fromCode(responseCode), urlToGet);

                }
            }
        });
    }

    @SuppressForbidden(reason = "we need socket connection to download data from internet")
    private static InputStream getInputStream(HttpURLConnection conn) throws IOException {
        return conn.getInputStream();
    }

    private static HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(false);
        conn.setInstanceFollowRedirects(false);
        return conn;
    }

    private static HttpURLConnection createConnection(String url, String method, String authorization,
            String content_type, Map<String, String> parameters) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        // first parameter has a ? before it, the rest have a & before them
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (url.contains("?")) {
                url += "&" + entry.getKey() + "=" + entry.getValue();
            } else {
                url += "?" + entry.getKey() + "=" + entry.getValue();
            }
        }
        conn.addRequestProperty("Authorization", authorization);
        conn.addRequestProperty("Content-Type", content_type);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setInstanceFollowRedirects(true);
        return conn;
    }

    private static <R> R doPrivileged(CheckedSupplier<R, IOException> supplier) throws IOException {
        SpecialPermission.check();
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<R>) supplier::get);
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getCause();
        }
    }
}