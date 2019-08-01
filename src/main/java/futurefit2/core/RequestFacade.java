package futurefit2.core;

public interface RequestFacade {
    /** Add a header to the request. This will not replace any existing headers. */
    void addHeader(String name, String value);

    /**
     * Add a path parameter replacement. This works exactly like a
     * {@link retrofit.http.Path &#64;Path}-annotated method argument.
     */
    void addPathParam(String name, String value);

    /**
     * Add a path parameter replacement without first URI encoding. This works
     * exactly like a {@link retrofit.http.Path &#64;Path}-annotated method argument
     * with {@code encode=false}.
     */
    void addEncodedPathParam(String name, String value);

    /**
     * Add an additional query parameter. This will not replace any existing query
     * parameters.
     */
    void addQueryParam(String name, String value);

    /**
     * Add an additional query parameter without first URI encoding. This will not
     * replace any existing query parameters.
     */
    void addEncodedQueryParam(String name, String value);
}
