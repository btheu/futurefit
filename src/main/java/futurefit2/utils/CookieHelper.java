package futurefit2.utils;

import java.util.List;

import okhttp3.Headers;

public class CookieHelper {

    public static String findCookieByName(Headers headers, String cookieName) {

        List<String> cookies = headers.values("Set-Cookie");
        for (String header : cookies) {
            String[] metas = header.split(";");
            for (String meta : metas) {

                String[] split = meta.split("=");
                if (split[0].equals(cookieName)) {
                    return split[1];
                }
            }
        }
        return null;
    }

}
