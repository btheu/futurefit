package futurefit2.convertor;

import java.io.IOException;
import java.lang.reflect.Type;

import estivate.EstivateMapper;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public class EstivateResponseConvertor<T> implements Converter<ResponseBody, T> {

    private Type type;

    private final EstivateMapper mapper = new EstivateMapper();

    public EstivateResponseConvertor() {
    }

    public EstivateResponseConvertor(String encoding) {
        this.setEncoding(encoding);
    }

    public EstivateResponseConvertor(Type type) {
        this.type = type;
    }

    public void setEncoding(String encoding) {
        this.mapper.setEncoding(encoding);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(ResponseBody value) throws IOException {
        try {
            return (T) this.mapper.map(value.byteStream(), type);
        } catch (IOException e) {
            throw new RuntimeException("Error occurs at conversion", e);
        }
    }

}
