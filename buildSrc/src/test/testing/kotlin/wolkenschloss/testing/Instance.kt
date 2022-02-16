package wolkenschloss.testing

import java.io.File
import java.nio.file.Paths

class Instance(public val target: File) {
    fun overlay(other: String, function: () -> Unit) {
        overlay(Fixtures(other), target)
        try {
            function()
        } finally {
            removeOverlay(Fixtures(target.path), Fixtures(other).fixture)
        }
    }

    fun overlay(fixture: Fixtures, instance: File) {
        val overlay = File(System.getProperty("project.fixture.directory")).resolve(fixture.path)
        overlay.copyRecursively(target = instance, overwrite = false)
    }

    fun removeOverlay(fixture: Fixtures, instance: File) {
        println("Remove overlay this: ${fixture.fixture.absolutePath} overlay: ${instance.absolutePath}")
        println("this: ${fixture.fixture.absolutePath}")
        println("overlay: ${instance.absolutePath}")

        instance.walkBottomUp().forEach {
            val relative = it.relativeTo(instance)
            val inFixture = fixture.fixture.resolve(relative)
            println("deleting file ${relative.path} in ${inFixture.absolutePath}")

            if (inFixture.isFile) {
                inFixture.delete()
            }
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