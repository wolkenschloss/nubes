package wolkenschloss.testing

import java.io.File
import java.nio.file.Paths

class Instance(public val target: File) {
    fun overlay(other: String, function: () -> Unit) {
        Fixtures(other).overlay(target)
        try {
            function()
        } finally {
            Fixtures(target.path).removeOverlay(Fixtures(other).fixture)
        }
    }

    companion object {
        fun from(fixture: File): Instance {
            val target = temporaryBuildDirectory().resolve(System.currentTimeMillis().toString())
            fixture.copyRecursively(target)
            return Instance(target)
        }

        private fun userDirectory(): File = Paths.get(System.getProperty("user.dir")).toFile()

        private fun temporaryBuildDirectory(): File = userDirectory()
            .resolve(Paths.get("build", "tmp", "fixture").toFile())
    }
}