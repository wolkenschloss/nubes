package wolkenschloss;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.NoSuchElementException;

/**
 * The Body of an application/x-www-form-urlencoded HTTP POST Request
 * <p>
 * The body of such a request has the form:
 * <p>
 * key1=value1&key2=value2...
 * <p>
 * Keys and values are URL Encoded. You will get this Body from an
 * HttpExchange.getRequestBody() method call.
 */
public class FormData {
    private final String requestBody;

    public FormData(String requestBody) {

        this.requestBody = requestBody;
    }

    /**
     * @param key The key of one value pair of the collection.
     * @return the value for key.
     * @throws UnsupportedEncodingException, NoSuchElementException
     */
    public String value(String key) throws UnsupportedEncodingException {
        var encoding = System.getProperty("file.encoding");
        String[] pairs = this.requestBody.split("&");

        for (var pair : pairs) {
            var items = pair.split("=");
            if (URLDecoder.decode(items[0], encoding).equals(key)) {
                return URLDecoder.decode(items[1], encoding).trim();
            }
        }

        throw new NoSuchElementException(key);
    }
}
