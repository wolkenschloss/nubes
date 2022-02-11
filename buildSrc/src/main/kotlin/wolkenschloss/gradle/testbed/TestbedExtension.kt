package wolkenschloss.gradle.testbed

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import wolkenschloss.gradle.testbed.domain.DomainExtension
import wolkenschloss.gradle.testbed.domain.RegistryService
import wolkenschloss.gradle.testbed.domain.SecureShellService
import wolkenschloss.gradle.testbed.download.BaseImageExtension
import wolkenschloss.gradle.testbed.pool.PoolExtension
import wolkenschloss.gradle.testbed.transformation.TransformationExtension

abstract class TestbedExtension : ExtensionAware {
    fun configure(project: Project): TestbedExtension {
        // Set build directories
        val layout = project.layout
        val buildDirectory = layout.buildDirectory
        val sharedServices = project.gradle.sharedServices
        runDirectory.convention(buildDirectory.dir("run"))
        failOnError.convention(true)
        transformation.initialize(layout)
        user.initialize()
        host.initialize()
        pool.initialize(sharedServices, buildDirectory, runDirectory)
        baseImage.initialize(project)
        domain.initialize(
            sharedServices,
            runDirectory.file(DEFAULT_KNOWN_HOSTS_FILE_NAME),
            runDirectory.file(DEFAULT_HOSTS_FILE_NAME),
            runDirectory.file(DEFAULT_KUBE_CONFIG_FILE_NAME),
            runDirectory.file(DEFAULT_DOCKER_CONFIG_FILE_NAME)
        )
        return this
    }

    @get:Nested
    abstract val host: HostExtension
    fun host(action: Action<in HostExtension>) {
        action.execute(host)
    }

    @get:Nested
    abstract val user: UserExtension
    fun user(action: Action<in UserExtension>) {
        action.execute(user)
    }

    @get:Nested
    abstract val domain: DomainExtension
    fun domain(action: Action<in DomainExtension>) {
        action.execute(domain)
    }

    @get:Nested
    abstract val pool: PoolExtension
    fun pool(action: Action<in PoolExtension>) {
        action.execute(pool)
    }

    @get:Nested
    abstract val baseImage: BaseImageExtension
    fun base(action: Action<in BaseImageExtension>) {
        action.execute(baseImage)
    }

    @get:Nested
    abstract val transformation: TransformationExtension
    fun transformation(action: Action<in TransformationExtension>) {
        action.execute(transformation)
    }

    /**
     * Liefert die Beschreibung des Prüfstandes als Map
     * @param objects Tja
     * @return Eine Karte mit den Eigenschaften des Prüfstandes
     */
    fun asPropertyMap(project: Project): Map<String, Provider<*>> {
        return mapOf(
            "user" to user.name,
            "sshKey" to user.sshKey,
            "hostname" to domain.name,
            "fqdn" to project.provider { domain.testbedVmFqdn },
            "locale" to domain.locale,
            "callback_ip" to host.hostAddress,
            "callback_port" to host.callbackPort,
            "disks_root" to pool.rootImageName,
            "disks_cidata" to pool.cidataImageName,
            "pool_name" to pool.name,
            "pool_directory" to pool.poolDirectory

        )
    }

    abstract val runDirectory: DirectoryProperty
    abstract val failOnError: Property<Boolean?>
    abstract val secureShellService: Property<SecureShellService>
    abstract val registryService: Property<RegistryService>

    companion object {
        const val DEFAULT_KNOWN_HOSTS_FILE_NAME = "known_hosts"
        const val DEFAULT_HOSTS_FILE_NAME = "hosts"
        const val DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig"
        const val DEFAULT_DOCKER_CONFIG_FILE_NAME = "docker/config.json"
    }
}