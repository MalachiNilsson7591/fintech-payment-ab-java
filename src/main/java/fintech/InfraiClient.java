package fintech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Small REST client that decodes the Infrai envelope before HTTP status handling. */
public final class InfraiClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String apiKey;

    public InfraiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public String get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() == 429) {
                Thread.sleep(retryDelay(response, attempt));
                continue;
            }
            if (!body.contains("\"ok\":true")) {
                throw new InfraiException(extract(body, "code"), extract(body, "message"), response.statusCode());
            }
            return body;
        }
        throw new IOException("request retries exhausted");
    }

    private static long retryDelay(HttpResponse<?> response, int attempt) {
        try { return Long.parseLong(response.headers().firstValue("Retry-After").orElse("0")) * 1000L; }
        catch (NumberFormatException ignored) { return 250L * (1L << attempt); }
    }

    static String extract(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return "";
        start += marker.length();
        int end = json.indexOf('"', start);
        return end < 0 ? "" : json.substring(start, end);
    }

    public static final class InfraiException extends IOException {
        public final String code;
        public final int status;
        InfraiException(String code, String message, int status) { super(message); this.code = code; this.status = status; }
    }
}
