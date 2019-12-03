package futurefit2.utils;

import java.io.IOException;

import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;

public class OkHttpUtil {

    @SneakyThrows
    public static Request readBodyToBytesIfNecessary(Request request) {

        RequestBody body = request.body();
        if (body == null || body instanceof TypedBytesRequestBody) {
            return request;
        }

        MediaType bodyMime = body.contentType();
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        body = new TypedBytesRequestBody(bodyMime, buffer.readByteArray());

        return request.newBuilder().method(request.method(), body).build();
    }

    @SneakyThrows
    public static Response readBodyToBytesIfNecessary(Response response) {
        ResponseBody body = response.body();
        if (body == null || body instanceof TypedBytesResponseBody) {
            return response;
        }
        MediaType bodyMime = body.contentType();
        body = new TypedBytesResponseBody(bodyMime, body.bytes());

        return response.newBuilder().body(body).build();
    }

    public static class TypedBytesRequestBody extends RequestBody {

        private MediaType bodyMime;
        private byte[]    byteArray;

        public TypedBytesRequestBody(MediaType bodyMime, byte[] byteArray) {
            this.bodyMime = bodyMime;
            this.byteArray = byteArray;
        }

        @Override
        public MediaType contentType() {
            return bodyMime;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            sink.write(byteArray);
        }

        public byte[] getBytes() {
            return byteArray;
        }
    }

    public static class TypedBytesResponseBody extends ResponseBody {

        private MediaType bodyMime;
        private byte[]    byteArray;

        public TypedBytesResponseBody(MediaType bodyMime, byte[] byteArray) {
            this.bodyMime = bodyMime;
            this.byteArray = byteArray;
        }

        @Override
        public MediaType contentType() {
            return bodyMime;
        }

        @Override
        public long contentLength() {
            return byteArray.length;
        }

        @Override
        public BufferedSource source() {
            Buffer buffer = new Buffer();
            buffer.write(byteArray);
            return buffer;
        }
    }

}
