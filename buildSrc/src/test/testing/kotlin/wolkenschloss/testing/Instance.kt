package wolkenschloss.testing

import java.io.File
import java.nio.file.Paths

class Instance(val target: File) {

    fun overlay(overlay: File, function: () -> Unit) {
        overlay.copyRecursively(target, false)
        try {
            function()
        } finally {
            removeOverlay(overlay)
        }
    }

    private fun removeOverlay(overlay: File) {
        overlay.walkBottomUp().forEach {
            val inFixture = target.resolve(it.relativeTo(overlay))

            if (inFixture.isFile) {
                inFixture.delete()
            }
        }
    }

    companion object {
        fun from(fixture: File): Instance {
            val target = temporaryBuildDirectory.resolve(System.currentTimeMillis().toString())
            fixture.copyRecursively(target)
            return Instance(target)
        }

        private val userDirectory
            get() = Paths.get(System.getProperty("user.dir")).toFile()

        private val temporaryBuildDirectory
            get() = userDirectory.resolve(Paths.get("build", "tmp", "fixture").toFile())
    }
}