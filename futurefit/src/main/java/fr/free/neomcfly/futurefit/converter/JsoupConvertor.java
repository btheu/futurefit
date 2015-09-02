package fr.free.neomcfly.futurefit.converter;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import fr.free.neomcfly.jsoupmapper.JsoupMapper;

public class JsoupConvertor implements Converter {

    @Override
    public Object fromBody(TypedInput body, Type type)
            throws ConversionException {
        try {
            return new JsoupMapper().fromBody(body.in(), type);
        } catch (IOException e) {
            throw new RuntimeException("Error occurs at conversion", e);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        throw new RuntimeException("Not implemented");
    }

}
