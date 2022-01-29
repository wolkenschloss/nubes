package wolkenschloss.testing

import java.io.File

class Fixtures(private val path: String) {

    fun clone(target: File): File {
        val fixtures = File(System.getProperty("project.fixture.directory")).resolve(path)
        fixtures.copyRecursively(target)
        return target
    }
}