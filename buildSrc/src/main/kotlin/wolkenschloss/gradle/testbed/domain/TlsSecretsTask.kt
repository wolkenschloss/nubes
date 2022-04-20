package wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class TlsSecretsTask : DefaultTask() {

    @get:Internal
    abstract val domainName: Property<String>

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun printSecrets() {
        val domainOperations = DomainOperations(execOperations, domainName)
        domainOperations.readAllTlsSecrets()
            .forEach {
                logger.lifecycle("${it.namespace}/${it.name}")
                logger.lifecycle("Type: ${it.type}")
                logger.lifecycle("Subject: ${it.certificate.subject}")
                logger.lifecycle("SAN: ${it.certificate.subjectAlternativeNames().joinToString(", ")}")
                logger.lifecycle("Issuer: ${it.certificate.issuer}")
                logger.lifecycle("not before: ${it.certificate.notBefore}")
                logger.lifecycle("not after: ${it.certificate.notAfter}")
                logger.lifecycle("")
            }
    }
}