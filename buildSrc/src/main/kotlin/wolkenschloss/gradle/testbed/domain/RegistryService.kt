package wolkenschloss.gradle.testbed.domain

import com.google.cloud.tools.jib.api.*
import com.jayway.jsonpath.JsonPath
import org.gradle.api.GradleException
import wolkenschloss.gradle.testbed.domain.RegistryService
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ExecutionException
import java.util.function.Consumer

class RegistryService(ip: String?) {
    val address: String

    init {
        address = String.format("%s:32000", ip)
    }

    @Throws(IOException::class, InterruptedException::class)
    fun listCatalogs(): List<String> {
        val client = HttpClient.newHttpClient()
        val uri = URI.create(String.format("http://%s/v2/_catalog", address))
        val request = HttpRequest.newBuilder().uri(uri).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw GradleException(String.format("Return Code from Registry: %d", response.statusCode()))
        }
        val parse = JsonPath.parse(response.body())
        return parse.read("$.repositories[*]")
    }

    @Throws(
        InvalidImageReferenceException::class,
        CacheDirectoryCreationException::class,
        IOException::class,
        ExecutionException::class,
        InterruptedException::class,
        RegistryException::class
    )
    fun uploadImage(image: String): String {
        val tag = String.format("%s/%s", address, image)
        Jib.from("hello-world")
            .containerize(
                Containerizer.to(RegistryImage.named(tag))
                    .setAllowInsecureRegistries(true)
            )
        return tag
    }

    fun withRegistry(method: Consumer<RegistryService>) {
        method.accept(this)
    }

    override fun toString(): String {
        return "Registry{" +
                "name='" + address + '\'' +
                '}'
    }
}