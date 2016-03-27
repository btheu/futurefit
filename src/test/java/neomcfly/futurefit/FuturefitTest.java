package neomcfly.futurefit;

import org.junit.Assert;
import org.junit.Test;

import btheu.jsoupmapper.JSoupSelect;
import btheu.jsoupmapper.JSoupText;
import btheu.retrofit.jsoup.converter.JSoupConverter;
import junit.framework.TestCase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import retrofit.RestAdapter.LogLevel;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

@Slf4j
public class FuturefitTest {

    @Test
    public void test() {

        Futurefit adapter = new Futurefit.Builder().setLogLevel(LogLevel.NONE)
                .setEndpoint("https://www.google.com/")
                .setConverter(new JSoupConverter()).build();

        GoogleApi create = adapter.create(GoogleApi.class);

        String stats = create.search("retrofit").getResultStatistics();

        assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);

    }

    private void assertNotEmpty(String sentence) {
        TestCase.assertNotNull(sentence);
        Assert.assertNotEquals("", sentence.trim());
    }

    public static interface GoogleApi {

        @JSoup
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla" })
        public Page search(@Query("q") String query);

    }

    @Data
    public static class Page {

        // get the div holding statistics
        @JSoupSelect("#resultStats")
        @JSoupText
        public String resultStatistics;

    }

}
