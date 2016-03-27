package neomcfly.futurefit;

import org.junit.Test;

import btheu.retrofit.jsoup.converter.JSoupConverter;
import retrofit.RestAdapter.LogLevel;

public class FuturefitTest {

    @Test
    public void test() {

        Futurefit adapter = new Futurefit.Builder().setLogLevel(LogLevel.FULL)
                .setEndpoint("https://www.google.com/")
                .setConverter(new JSoupConverter()).build();

        Api create = adapter.create(Api.class);

    }

    public static interface Api {

    }

}
