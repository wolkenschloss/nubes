package wolkenschloss.gradle.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.gradle.api.invocation.Gradle
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Internal
import java.time.Duration

@Suppress("UnstableApiUsage")
abstract class DockerService : BuildService<BuildServiceParameters.None>, AutoCloseable {

    private val config = DefaultDockerClientConfig
        .createDefaultConfigBuilder()
        .build()

    private val httpClient: ZerodepDockerHttpClient = ZerodepDockerHttpClient.Builder()
        .dockerHost(config.dockerHost)
        .sslConfig(config.sslConfig)
        .maxConnections(100)
        .connectionTimeout(Duration.ofSeconds(30))
        .responseTimeout(Duration.ofSeconds(30))
        .build()

    @Internal
    val client: DockerClient = DockerClientImpl.getInstance(config, httpClient)


    override fun close() {
         client.close()
    }

    fun listImages(): List<Image> = client.listImagesCmd()
        .withShowAll(true)
        .exec()

    companion object {
        private const val NAME = "docker"

        fun getInstance(gradle: Gradle): DockerService {
            return gradle.sharedServices
                .registerIfAbsent(
                    NAME,
                    DockerService::class.java
                ) {}.get()
        }
    }
}