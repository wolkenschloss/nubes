package wolkenschloss.gradle.testbed.domain

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.NoSuchElementException

/**
 * The Body of an application/x-www-form-urlencoded HTTP POST Request
 *
 *
 * The body of such a request has the form:
 *
 *
 * key1=value1&key2=value2...
 *
 *
 * Keys and values are URL Encoded. You will get this Body from an
 * HttpExchange.getRequestBody() method call.
 */
class FormData(private val requestBody: String) {
    /**
     * @param key The key of one value pair of the collection.
     * @return the value for key.
     * @throws UnsupportedEncodingException, NoSuchElementException
     */
    @Throws(UnsupportedEncodingException::class)
    fun value(key: String): String {
        val encoding = System.getProperty("file.encoding")
        val pairs = requestBody.split("&".toRegex()).toTypedArray()
        for (pair in pairs) {
            val items = pair.split("=".toRegex()).toTypedArray()
            if (URLDecoder.decode(items[0], encoding) == key) {
                return URLDecoder.decode(items[1], encoding).trim { it <= ' ' }
            }
        }
        throw NoSuchElementException(key)
    }
}