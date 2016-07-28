package futurefit.core;

import lombok.Getter;

import retrofit.RequestInterceptor.RequestFacade;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public class AuthentificationRequestFacade implements RequestFacade {

    @Getter
    private String name;
    @Getter
    private String value;
    @Getter
    private Type type;

    public void addHeader(String name, String value) {
        this.type = Type.HEADER;
        this.name = name;
        this.value = value;
    }

    public void addPathParam(String name, String value) {
        this.type = Type.PATH_PARAM;
        this.name = name;
        this.value = value;
    }

    public void addEncodedPathParam(String name, String value) {
        this.type = Type.ENCODED_PATH_PARAM;
        this.name = name;
        this.value = value;
    }

    public void addQueryParam(String name, String value) {
        this.type = Type.QUERY_PARAM;
        this.name = name;
        this.value = value;
    }

    public void addEncodedQueryParam(String name, String value) {
        this.type = Type.ENCODED_QUERY_PARAM;
        this.name = name;
        this.value = value;
    }

    public static enum Type {
        HEADER, PATH_PARAM, ENCODED_PATH_PARAM, QUERY_PARAM, ENCODED, ENCODED_QUERY_PARAM
    }
}
