package wolkenschloss.gradle.ca

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.engine.spec.tempdir
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.kotlin.dsl.*
import io.kotest.matchers.shouldBe
import wolkenschloss.gradle.testbed.Directories
import io.kotest.matchers.file.shouldStartWithPath

class ServerCertificateSpec : FunSpec({
    context("CA Gradle Plugin applied to a project") {
        withEnvironment(mapOf("XDG_DATA_HOME" to tempdir().path)) {

            val projectDir = tempdir()
            val project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName(PROJECT_NAME)
                .build()

            project.pluginManager.apply(CaPlugin::class.java)

            test("should register server certificate task") {
                val localhost by project.tasks.registering(ServerCertificate::class)
                localhost.isPresent shouldBe true
            }

            test("should have default root ca certificate") {
                val localhost by project.tasks.registering(ServerCertificate::class)
                localhost.get().caCertificate.get().asFile shouldStartWithPath Directories.certificateAuthorityHome.resolve(CaPlugin.CA_CERTIFICATE_FILE_NAME)
            }

            test("should have default root ca key") {
                val localhost by project.tasks.registering(ServerCertificate::class)
                localhost.get().caPrivateKey.get().asFile shouldStartWithPath Directories.certificateAuthorityHome.resolve(CaPlugin.CA_PRIVATE_KEY_FILE_NAME)
            }

            test("should have default certificate file name") {
                val localhost by project.tasks.registering(ServerCertificate::class)
                localhost.get().certificate.get().asFile shouldStartWithPath Directories.certificateAuthorityHome.resolve("localhost.pem")
            }

            test("should have default private key file name") {
                val example by project.tasks.registering(ServerCertificate::class)
                example.get().privateKey.get().asFile shouldStartWithPath Directories.certificateAuthorityHome.resolve("example-key.pem")
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        const val PROJECT_NAME = "certificate"
    }
}