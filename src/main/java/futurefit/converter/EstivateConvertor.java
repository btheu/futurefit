package futurefit.converter;

import java.io.IOException;
import java.lang.reflect.Type;

import estivate.EstivateMapper;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public class EstivateConvertor implements Converter {

    private final EstivateMapper mapper = new EstivateMapper();

    public EstivateConvertor() {
    }

    public EstivateConvertor(String encoding) {
        this.setEncoding(encoding);
    }

    public void setEncoding(String encoding) {
        this.mapper.setEncoding(encoding);
    }

    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            return this.mapper.map(body.in(), type);
        } catch (IOException e) {
            throw new RuntimeException("Error occurs at conversion", e);
        }
    }

    public TypedOutput toBody(Object object) {
        throw new RuntimeException("Convertion to body can't be for EstivateConvertor");
    }

}
