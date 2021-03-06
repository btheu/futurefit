package futurefit2.core;

import lombok.Getter;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public class ProxyRequestFacade implements RequestFacade {

    @Getter
    private String name;
    @Getter
    private String value;
    @Getter
    private Type   type = Type.NONE;

    @Override
    public void addHeader(String name, String value) {
        this.type = Type.HEADER;
        this.name = name;
        this.value = value;
    }

    @Override
    public void addPathParam(String name, String value) {
        this.type = Type.PATH_PARAM;
        this.name = name;
        this.value = value;
    }

    @Override
    public void addEncodedPathParam(String name, String value) {
        this.type = Type.ENCODED_PATH_PARAM;
        this.name = name;
        this.value = value;
    }

    @Override
    public void addQueryParam(String name, String value) {
        this.type = Type.QUERY_PARAM;
        this.name = name;
        this.value = value;
    }

    @Override
    public void addEncodedQueryParam(String name, String value) {
        this.type = Type.ENCODED_QUERY_PARAM;
        this.name = name;
        this.value = value;
    }

    public static enum Type {
        NONE,
        HEADER,
        PATH_PARAM,
        ENCODED_PATH_PARAM,
        QUERY_PARAM,
        ENCODED_QUERY_PARAM,
    }
}
