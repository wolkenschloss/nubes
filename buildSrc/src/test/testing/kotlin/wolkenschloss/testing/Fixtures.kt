package wolkenschloss.testing

import java.io.File
import java.nio.file.Paths

class Fixtures(private val path: String) : AutoCloseable {

    fun clone(target: File): File {
        val fixtures = File(
            System.getProperty(
                "project.fixture.directory",
                defaultFixturePath().absolutePath))
            .resolve(path)

        fixtures.copyRecursively(target)
        return target
    }

    private val temporaryDirectories = arrayListOf<File>()

    private fun clone(): File {
        val target = temporaryBuildDirectory().resolve(System.currentTimeMillis().toString())
        temporaryDirectories.add(target)
        return clone(target)
    }

    fun withClone(block: File.() -> Unit) {
        val clone = clone()
        block(clone)
    }

    private fun defaultFixturePath() = userDirectory().resolve("fixtures")

    private fun overlay(fixture: File) {
        val overlay = File(System.getProperty("project.fixture.directory")).resolve(path)
        overlay.copyRecursively(target = fixture, overwrite = false)
    }

    fun useOverlay(directory: File, function: () -> Unit) {
        Fixtures(path).overlay(directory)
        try {
            function()
        } finally {
            Fixtures(path).removeOverlay(directory)
        }
    }

    private fun removeOverlay(fixture: File) {
        val fixtures = File(System.getProperty("project.fixture.directory"))
        val overlay = fixtures.resolve(path)

        overlay.walkBottomUp().forEach {
            val relative = it.relativeTo(overlay)
            val inFixture = fixture.resolve(relative)
            if (inFixture.isFile) {
                inFixture.delete()
            }
        }
    }

    companion object {
        fun userDirectory(): File = Paths.get(System.getProperty("user.dir")).toFile()

        // It is not possible to use the temporary directories provided by
        // Kotest or Java for cloned fixtures.
        //
        // The node_modules directory contains executable files that are
        // required for the test. No files may be executed in proper temporary
        // directories. This leads to an error in the test.
//        val fixture = Fixtures.temporaryBuildDirectory()
        fun temporaryBuildDirectory(): File = userDirectory().resolve(Paths.get("build", "tmp", "fixture").toFile())
    }

    override fun close() {
        temporaryDirectories.reversed().forEach {directory ->
            directory.walkBottomUp().forEach { file -> file.delete() }
        }

        temporaryDirectories.clear()
    }
}
