package neomcfly.futurefit.converter;

import java.io.IOException;
import java.lang.reflect.Type;

import estivate.EstivateMapper2;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class EstivateConvertor implements Converter {

    private final EstivateMapper2 mapper = new EstivateMapper2();

    public EstivateConvertor() {
    }

    public EstivateConvertor(String encoding) {
        this.setEncoding(encoding);
    }

    public void setEncoding(String encoding) {
        this.mapper.setEncoding(encoding);
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            return this.mapper.map(body.in(), (Class) type);
        } catch (IOException e) {
            throw new RuntimeException("Error occurs at conversion", e);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        throw new RuntimeException("Convertion to body can't be for JSoupConvertor");
    }

}
