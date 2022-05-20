package family.haschka.wolkenschloss.gradle.testbed

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskProvider
import family.haschka.wolkenschloss.gradle.testbed.domain.DomainExtension
import family.haschka.wolkenschloss.gradle.testbed.transformation.TransformationExtension
import javax.inject.Inject
import family.haschka.wolkenschloss.gradle.ca.TrustAnchor

@Suppress("CdiInjectionPointsInspection")
abstract class TestbedExtension @Inject constructor(private val layout: ProjectLayout) {
    fun configure(ca: TaskProvider<TrustAnchor>): TestbedExtension {
        val buildDirectory = layout.buildDirectory
        runDirectory.convention(buildDirectory.dir("run"))
        transformation.initialize()
        domain.initialize(runDirectory, ca)

        return this
    }

     @get:Nested
    abstract val domain: DomainExtension
    fun domain(action: Action<in DomainExtension>) {
        action.execute(domain)
    }

    @get:Nested
    abstract val transformation: TransformationExtension

    @Suppress("unused")
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
            "hostname" to domain.name,
            "fqdn" to project.provider { domain.testbedVmFqdn },
            "locale" to domain.locale,
            "hosts" to domain.hosts
                .map {hosts: List<String> -> hosts.map { host:String  -> "$host.${domain.domainSuffix.get()}" }}
                .map { it.joinToString(" ") },
            "dns" to domain.dns.map { it.joinToString(" ") }, //OK
            "cert_manager_version" to domain.certManagerVersion,
            "certificate" to domain.certificate.map { it.asFile.readText() },
            "private_key" to domain.privateKey.map { it.asFile.readText() }
        )
    }

    abstract val runDirectory: DirectoryProperty

    val registry: Provider<String> get() = domain.domainSuffix.map { "registry.$it" }
}