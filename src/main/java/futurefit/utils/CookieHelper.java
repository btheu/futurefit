package futurefit.utils;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Header;

public class CookieHelper {

    public static String findCookieByName(List<Header> headers, String cookieName) {

        List<Header> cookies = findHeadersByName(headers, "Set-Cookie");
        for (Header header : cookies) {
            String[] metas = header.getValue().split(";");
            for (String meta : metas) {

                String[] split = meta.split("=");
                if (split[0].equals(cookieName)) {
                    return split[1];
                }
            }
        }
        return null;
    }

    public static List<Header> findHeadersByName(List<Header> headers, String headerName) {
        List<Header> res = new ArrayList<>();
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase(headerName)) {
                res.add(header);
            }
        }
        return res;
    }

}
