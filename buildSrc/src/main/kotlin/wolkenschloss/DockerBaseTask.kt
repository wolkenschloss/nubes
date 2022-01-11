package wolkenschloss

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import java.time.Duration

abstract class DockerBaseTask : DefaultTask() {

    private val config : DefaultDockerClientConfig = DefaultDockerClientConfig
        .createDefaultConfigBuilder()
        .build()

    private val httpClient : ZerodepDockerHttpClient = ZerodepDockerHttpClient.Builder()
        .dockerHost(config.dockerHost)
        .sslConfig(config.sslConfig)
        .maxConnections(100)
        .connectionTimeout(Duration.ofSeconds(30))
        .responseTimeout(Duration.ofSeconds(30))
        .build()

    @Internal
    val docker: DockerClient = DockerClientImpl.getInstance(config, httpClient)
}