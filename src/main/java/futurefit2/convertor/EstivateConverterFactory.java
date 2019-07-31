package futurefit2.convertor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import futurefit2.Estivate;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;

public class EstivateConverterFactory extends Factory {

    private EstivateConverterFactory() {
        // private
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Estivate.class)) {
                return new EstivateResponseConvertor<>(type);
            }
        }
        return null;
    }

    public static Factory create() {
        return new EstivateConverterFactory();
    }

}