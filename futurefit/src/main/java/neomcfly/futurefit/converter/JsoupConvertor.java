package neomcfly.futurefit.converter;

import java.io.IOException;
import java.lang.reflect.Type;

import neomcfly.jsoupmapper.JsoupMapper;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class JsoupConvertor implements Converter {

	private final JsoupMapper jsoupMapper;

	public JsoupConvertor() {
		jsoupMapper = new JsoupMapper();
	}

	public JsoupConvertor(String encoding) {
		this();
		this.setEncoding(encoding);
	}

	public void setEncoding(String encoding) {
		jsoupMapper.setEncoding(encoding);
	}

	@Override
	public Object fromBody(TypedInput body, Type type) throws ConversionException {
		try {
			return jsoupMapper.fromBody(body.in(), type);
		} catch (IOException e) {
			throw new RuntimeException("Error occurs at conversion", e);
		}
	}

	@Override
	public TypedOutput toBody(Object object) {
		throw new RuntimeException("Convertion to body can't be for JSoupConvertor");
	}

}
