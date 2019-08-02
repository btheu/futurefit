package futurefit2.core;

import java.io.IOException;
import java.lang.reflect.Type;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.CallAdapter;

@Slf4j
public class UnboxCallAdapter<R, T> implements CallAdapter<R, T> {

    private Type type;

    public UnboxCallAdapter(Type type) {
        this.type = type;
    }

    @Override
    public Type responseType() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T adapt(Call<R> call) {
        try {
            return (T) call.execute().body();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
