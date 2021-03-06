package futurefit2.core.interceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import futurefit2.utils.OkHttpUtil;
import futurefit2.utils.OkHttpUtil.TypedBytesRequestBody;
import futurefit2.utils.OkHttpUtil.TypedBytesResponseBody;
import lombok.Setter;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

public class HttpLoggingInterceptor implements Interceptor {

    final Log log = defaultLog();

    @Setter
    volatile Level level = Level.HEADERS;

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        HttpUrl url = request.url();

        Response proceed;
        try {

            Request req = logAndReplaceRequest("HTTP", request);

            long start = System.nanoTime();
            proceed = chain.proceed(req);
            long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            proceed = logAndReplaceResponse(url.toString(), proceed, elapsedTime);

        } catch (Exception e) {
            logException(e, url.toString());
            throw e;
        }

        return proceed;
    }

    /**
     * Log request headers and body. Consumes request body and returns identical
     * replacement.
     */
    Request logAndReplaceRequest(String name, Request request) throws IOException {
        log.log(String.format("---> %s %s %s", name, request.method(), request.url()));

        if (level.ordinal() >= Level.HEADERS.ordinal()) {
            log.log(request.headers().toString());

            String bodySize = "no";
            RequestBody body = request.body();
            if (body != null && body.contentLength() > 0) {
                String bodyMime = body.contentType().type();
                if (bodyMime != null) {
                    log.log("Content-Type: " + bodyMime);
                }

                long bodyLength = body.contentLength();
                bodySize = bodyLength + "-byte";
                if (bodyLength != -1) {
                    log.log("Content-Length: " + bodyLength);
                }

                if (level.ordinal() >= Level.FULL.ordinal()) {
                    if (request.headers().size() > 0) {
                        log.log("");
                    }
                    if (!(body instanceof TypedBytesRequestBody)) {
                        // Read the entire response body to we can log it and replace the original
                        // response
                        request = OkHttpUtil.readBodyToBytesIfNecessary(request);
                        body = request.body();
                    }

                    byte[] bodyBytes = ((TypedBytesRequestBody) body).getBytes();
                    log.log(new String(bodyBytes, "UTF-8"));
                } else if (level.ordinal() >= Level.HEADERS_AND_ARGS.ordinal()) {
                    if (request.headers().size() > 0) {
                        log.log("---> REQUEST:");
                    }
                }
            }

            log.log(String.format("---> END %s (%s body)", name, bodySize));
        }

        return request;
    }

    /**
     * Log response headers and body. Consumes response body and returns identical
     * replacement.
     */
    private Response logAndReplaceResponse(String url, Response response, long elapsedTime) throws IOException {
        log.log(String.format("<--- HTTP %s %s (%sms)", response.code(), url, elapsedTime));

        if (level.ordinal() >= Level.HEADERS.ordinal()) {
            log.log(response.headers().toString());

            long bodySize = 0;
            ResponseBody body = response.body();
            if (body != null) {
                bodySize = body.contentLength();

                if (level.ordinal() >= Level.FULL.ordinal()) {
                    if (response.headers().size() > 0) {
                        log.log("");
                    }

                    if (!(body instanceof TypedBytesResponseBody)) {
                        // Read the entire response body so we can log it and replace the original
                        // response
                        response = OkHttpUtil.readBodyToBytesIfNecessary(response);

                        body = response.body();
                    }

                    BufferedSource bodySources = ((TypedBytesResponseBody) body).source();
                    byte[] bodyBytes;
                    if ("gzip".equalsIgnoreCase(response.headers().get("Content-Encoding"))) {
                        GzipSource gzipSource = new GzipSource(bodySources);
                        Buffer buffer = new Buffer();
                        buffer.writeAll(gzipSource);
                        bodyBytes = buffer.readByteArray();
                        buffer.close();
                    } else {
                        bodyBytes = bodySources.readByteArray();
                    }

                    bodySize = bodyBytes.length;
                    log.log(new String(bodyBytes, "UTF-8"));
                }
            }

            log.log(String.format("<--- END HTTP (%s-byte body)", bodySize));
        }

        return response;
    }

    /**
     * Log an exception that occurred during the processing of a request or
     * response.
     */
    void logException(Throwable t, String url) {
        log.log(String.format("---- ERROR %s", url != null ? url : ""));
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        log.log(sw.toString());
        log.log("---- END ERROR");
    }

    private Log defaultLog() {
        return new Log() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        };
    }

    /** Simple logging abstraction for debug messages. */
    public interface Log {
        /** Log a debug message to the appropriate console. */
        void log(String message);

        /** A {@link Log} implementation which does not log anything. */
        Log NONE = new Log() {
            @Override
            public void log(String message) {
            }
        };
    }

    /** Controls the level of logging. */
    public enum Level {
        /** No logging. */
        NONE,
        /**
         * Log only the request method and URL and the response status code and
         * execution time.
         */
        BASIC,
        /** Log the basic information along with request and response headers. */
        HEADERS,
        /**
         * Log the basic information along with request and response objects via
         * toString().
         */
        HEADERS_AND_ARGS,
        /**
         * Log the headers, body, and metadata for both requests and responses.
         * <p>
         * Note: This requires that the entire request and response body be buffered in
         * memory!
         */
        FULL;

        public boolean log() {
            return this != NONE;
        }
    }

}
