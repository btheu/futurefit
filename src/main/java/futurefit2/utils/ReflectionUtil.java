package futurefit2.utils;

import java.lang.annotation.Annotation;

public class ReflectionUtil {

    @SuppressWarnings("unchecked")
    public static <T> T findAnnotation(Class<T> clazz, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAssignableFrom(clazz)) {
                return (T) annotation;
            }
        }
        return null;
    }

}
