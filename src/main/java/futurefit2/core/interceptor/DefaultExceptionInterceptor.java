package futurefit2.core.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import futurefit2.core.interceptor.DefaultExceptionInterceptor.FuturefitUrl.FuturefitUrlBuilder;
import futurefit2.utils.FuturefitException;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.OPTIONS;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.QueryName;
import retrofit2.http.Url;

/**
 * <p>
 * Parse method annotation for recreating Url at best effort in case of
 * exception
 * 
 * @author Benoit Theunissen
 *
 */
@Slf4j
public class DefaultExceptionInterceptor implements RequestInterceptor {

    private String baseUrl;

    public DefaultExceptionInterceptor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Object intercept(RequestInvocation invocation) {
        try {
            return invocation.invoke();
        } catch (Exception e) {
            Method method = invocation.method();
            String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
            String url;
            try {
                url = url(invocation);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
                url = "COMPILE URL ERROR";
            }
            log.debug("method=" + methodName + ", url=" + url, e);
            throw new FuturefitException(method, url, e);
        }
    }

    private String url(RequestInvocation invocation) {

        FuturefitUrl build = parseParameters(invocation.method(), invocation);

        String path = build.path;
        for (Entry<String, String> entry : build.paths.entrySet()) {
            path = path.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        okhttp3.HttpUrl.Builder newBuilder = HttpUrl.parse(baseUrl + path).newBuilder();

        for (Entry<String, String> entry : build.queries.entrySet()) {
            newBuilder.setQueryParameter(entry.getKey(), entry.getValue());
        }

        for (String queryName : build.queryNames) {
            newBuilder.query(queryName);
        }

        return newBuilder.build().toString();
    }

    private FuturefitUrl parseParameters(Method method, RequestInvocation invocation) {
        FuturefitUrlBuilder builder = FuturefitUrl.builder();

        DELETE aDelete = method.getAnnotation(DELETE.class);
        if (aDelete != null) {
            builder.method("DELETE");
            builder.path(aDelete.value());
        }
        GET aGet = method.getAnnotation(GET.class);
        if (aGet != null) {
            builder.method("GET");
            builder.path(aGet.value());
        }
        HEAD aHead = method.getAnnotation(HEAD.class);
        if (aHead != null) {
            builder.method("HEAD");
            builder.path(aHead.value());
        }
        OPTIONS aOptions = method.getAnnotation(OPTIONS.class);
        if (aOptions != null) {
            builder.method("OPTIONS");
            builder.path(aOptions.value());
        }
        PATCH aPatch = method.getAnnotation(PATCH.class);
        if (aPatch != null) {
            builder.method("PATCH");
            builder.path(aPatch.value());
        }
        POST aPost = method.getAnnotation(POST.class);
        if (aPost != null) {
            builder.method("POST");
            builder.path(aPost.value());
        }
        PUT aPut = method.getAnnotation(PUT.class);
        if (aPut != null) {
            builder.method("PUT");
            builder.path(aPut.value());
        }
        HTTP aHttp = method.getAnnotation(HTTP.class);
        if (aHttp != null) {
            builder.method(aHttp.method().toUpperCase());
            builder.path(aHttp.path());
        }

        Annotation[][] parameters = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            Annotation[] annotations = parameters[i];
            Object value = invocation.arguments()[i];

            for (int j = 0; j < annotations.length; j++) {
                Annotation annotation = annotations[j];

                if (annotation instanceof Url) {
                    builder.path(value.toString());
                } else if (annotation instanceof Path) {
                    Path path = (Path) annotation;
                    builder.path(path.value(), value.toString());
                } else if (annotation instanceof Query) {
                    Query query = (Query) annotation;
                    builder.query(query.value(), value.toString());
                } else if (annotation instanceof QueryMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) value;
                    for (Entry<String, Object> entry : map.entrySet()) {
                        builder.query(entry.getKey(), entry.getValue().toString());
                    }
                } else if (annotation instanceof QueryName) {
                    builder.queryName(value.toString());
                }
            }
        }

        return builder.build();

        // retrofit2.http.DELETE.class
        // retrofit2.http.GET.class
        // retrofit2.http.HEAD.class
        // retrofit2.http.OPTIONS.class
        // retrofit2.http.PATCH.class
        // retrofit2.http.POST.class
        // retrofit2.http.PUT.class
        // retrofit2.http.HTTP.class

        // retrofit2.http.Url.class
        // retrofit2.http.Path.class
        // retrofit2.http.Query.class
        // retrofit2.http.QueryMap.class
        // retrofit2.http.QueryName.class

        // retrofit2.http.Body.class
        // retrofit2.http.Field.class
        // retrofit2.http.FieldMap.class
        // retrofit2.http.FormUrlEncoded.class
        // retrofit2.http.Header.class
        // retrofit2.http.HeaderMap.class
        // retrofit2.http.Headers.class
        // retrofit2.http.Multipart.class
        // retrofit2.http.Part.class
        // retrofit2.http.PartMap.class
        // retrofit2.http.Streaming.class
        // retrofit2.http.Tag.class

    }

    @lombok.Builder
    public static class FuturefitUrl {
        String method;
        String path;

        @Singular("query")
        Map<String, String> queries;

        @Singular("queryName")
        List<String> queryNames;

        @Singular("path")
        Map<String, String> paths;
    }

}
