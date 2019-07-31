package futurefit;

import org.junit.Assert;
import org.junit.Test;

import estivate.annotations.Select;
import estivate.annotations.Text;
import futurefit.converter.EstivateConvertor;
import junit.framework.TestCase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import retrofit.RestAdapter.LogLevel;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Slf4j
public class FuturefitTest {

    @Test
    public void test() {

        Futurefit adapter = new Futurefit.Builder().setLogLevel(LogLevel.NONE).setEndpoint("https://www.google.com/")
                .setConverter(new EstivateConvertor()).build();

        GoogleApi create = adapter.create(GoogleApi.class);

        String stats = create.search("retrofit").getResultStatistics();

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
        public Page search(@Query("q") String query);

    }

    @Data
    public static class Page {

        // get the div holding statistics
        @Select("#resultStats")
        @Text
        public String resultStatistics;

    }

}
