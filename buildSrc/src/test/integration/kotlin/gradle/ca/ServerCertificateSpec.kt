package family.haschka.wolkenschloss.gradle.ca

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.file.shouldStartWithPath
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.testfixtures.ProjectBuilder
import family.haschka.wolkenschloss.gradle.testbed.Directories

class ServerCertificateSpec : FunSpec({
    context("CA Gradle Plugin applied to a project") {
        val xdgDataHome = tempdir()
        withEnvironment(mapOf("XDG_DATA_HOME" to xdgDataHome.absolutePath)) {

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
                localhost.get().certificate.get().asFile shouldStartWithPath Directories.certificateAuthorityHome.resolve("localhost/crt.pem")
            }

            test("should have default private key file name") {
                val example by project.tasks.registering(ServerCertificate::class)
                example.get().privateKey.get().asFile shouldStartWithPath Directories.certificateAuthorityHome.resolve("example/key.pem")
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        const val PROJECT_NAME = "certificate"
    }
}