package wolkenschloss.testing

import java.io.File
import java.nio.file.Paths

class Fixtures(private val path: String) {

    fun clone(target: File): File {
        val fixtures = File(
            System.getProperty("project.fixture.directory",
                defaultFixturePath()
            ))
            .resolve(path)

        fixtures.copyRecursively(target)
        return target
    }

    private fun defaultFixturePath() = Paths.get(System.getProperty("user.dir"), "fixtures").toAbsolutePath().toString()

    private fun overlay(fixture: File) {
        val overlay = File(System.getProperty("project.fixture.directory")).resolve(path)
        overlay.copyRecursively(target = fixture, overwrite = false)
    }

    private fun remove(fixture: File) {
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

    fun useOverlay(fixture: File, function: () -> Unit) {
        Fixtures(path).overlay(fixture)
        try {
            function()
        } finally {
            Fixtures(path).remove(fixture)
        }
    }
}