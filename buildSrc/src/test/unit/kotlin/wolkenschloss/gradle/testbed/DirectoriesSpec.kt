package wolkenschloss.gradle.testbed

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.file.shouldHavePath

class DirectoriesSpec : FunSpec({
    val temp = tempdir()
    context("XDG_DATA_HOME Umgebungsvariable") {
        withEnvironment("XDG_DATA_HOME" to temp.absolutePath) {
            val xdgDataHome = temp.absoluteFile

            test("Has user application directory") {
                Directories.applicationHome.toFile()  shouldHavePath "$xdgDataHome/wolkenschloss"
            }

            test("Has XdgDataHome directory") {
                Directories.xdgDataHome.toFile() shouldHavePath xdgDataHome.absolutePath
            }

            test("Has testbed home directory") {
                Directories.testbedHome.toFile() shouldHavePath "$xdgDataHome/wolkenschloss/testbed"
            }

            test("has certificate authority home directory") {
                Directories.certificateAuthorityHome.toFile() shouldHavePath "$xdgDataHome/wolkenschloss/ca"
            }
        }
    }

    context("Ohne Umgebungsvariable") {
        val xdgDataHome = "${System.getProperty("user.home")}/.local/share"

        test("Has user application directory") {
            Directories.applicationHome.toFile() shouldHavePath "$xdgDataHome/wolkenschloss"
        }

        test("Has XdgDataHome directory") {
            Directories.xdgDataHome.toFile() shouldHavePath xdgDataHome
        }

        test("Has testbed home directory") {
            Directories.testbedHome.toFile() shouldHavePath "$xdgDataHome/wolkenschloss/testbed"
        }

        test("has certificate authority home directory") {
            Directories.certificateAuthorityHome.toFile() shouldHavePath "$xdgDataHome/wolkenschloss/ca"
        }
    }
})
