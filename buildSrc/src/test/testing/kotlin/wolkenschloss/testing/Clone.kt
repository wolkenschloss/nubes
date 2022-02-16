package wolkenschloss.testing

import java.io.File
import java.nio.file.Paths

class Clone(public val target: File) {
    companion object {
        fun from(fixture: File): Clone {
            val target = temporaryBuildDirectory().resolve(System.currentTimeMillis().toString())
            fixture.copyRecursively(target)
            return Clone(target)
        }

        private fun userDirectory(): File = Paths.get(System.getProperty("user.dir")).toFile()

        private fun temporaryBuildDirectory(): File = userDirectory()
            .resolve(Paths.get("build", "tmp", "fixture").toFile())
    }
}