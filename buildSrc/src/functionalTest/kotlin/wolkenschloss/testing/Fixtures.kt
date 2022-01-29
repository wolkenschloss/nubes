package wolkenschloss.testing

import java.io.File

class Fixtures(private val path: String) {

    fun clone(target: File): File {
        val fixtures = File(System.getProperty("project.fixture.directory")).resolve(path)
        fixtures.copyRecursively(target)
        return target
    }

    private fun overlay(fixture: File) {
        println("Using overlay $path")
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
                println("remove ${inFixture.path}")
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