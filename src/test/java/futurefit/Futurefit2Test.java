package futurefit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;

import estivate.annotations.Select;
import estivate.annotations.Text;
import futurefit.converter.EstivateConverterFactory;
import junit.framework.TestCase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Slf4j
public class Futurefit2Test {

    @Test
    public void test() throws IOException {

        retrofit2.CallAdapter.Factory factory2 = new retrofit2.CallAdapter.Factory() {
            @Override
            public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {

                return null;
            }
        };

        Retrofit adapter = new Retrofit.Builder().baseUrl("https://www.google.com/")
                .addConverterFactory(EstivateConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();

        GoogleApi create = adapter.create(GoogleApi.class);

        String stats = create.search("retrofit").execute().body().getResultStatistics();

        this.assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);

    }

    private void assertNotEmpty(String sentence) {
        TestCase.assertNotNull(sentence);
        Assert.assertNotEquals("", sentence.trim());
    }

    public static interface GoogleApi {

        @Estivate
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public Call<Page> search(@Query("q") String query);

        @Estivate
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public Page searchDirect(@Query("q") String query);

    }

    @Data
    public static class Page {

        // get the div holding statistics
        @Select("#resultStats")
        @Text
        public String resultStatistics;

    }

}
