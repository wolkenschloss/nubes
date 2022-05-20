package wolkenschloss.testing

import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Vorlage für Testprojekte, die in Funktionstests und Integrationstests benutzt
 * werden.
 *
 * Die Vorlagen befinden sich im `/buildSrc/fixtures` Verzeichnis, auf das die
 * Systemeigenschaft `project.fixture.directory` verweisen muss. Die Vorlagen
 * können instanziiert, d.h. in ein temporäres Verzeichnis kopiert werden. Die
 * Tests verwenden diese Instanzen.
 *
 * Die Instanzen werden nach Beendigung des Tests automatisch gelöscht.
 *
 * Das übliche Schema für die Verwendung von Vorlagen ist:
 *
 * ```kotlin
 * class ExampleSpec : FunSpec({
 *   context("example build convention") {
 *     autoClose(Template("example")).withClone {
 *     test("example build test") {
  *        val result = build("build", "-i")
 *         result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
 *         workingDirectory.resolve("build/myFile.txt").shouldExist()
 *       }
 *     }
 *   }
 * })
 *```
 *
 */
class Template(private val path: String) : AutoCloseable {

    /**
     * Führt einen Code Block mit einer Instanz der Testdaten aus.
     *
     * Die Testdaten werden in ein temporäres Verzeichnis kopiert, auf das die
     * Testdaten Instanz verweist.
     */
    fun withClone(block: suspend Instance.() -> Unit) = runBlocking {
        val instance = Instance.from(resolve(path))
        instances.add(instance)
        block(instance)
    }

    private val instances = arrayListOf<Instance>()

    /**
     * Löscht alle mit [withClone] erstellten Kopien der Fixture.
     */
    override fun close() {
        instances.reversed().forEach { instance ->
            instance.workingDirectory.walkBottomUp().forEach { file -> file.delete() }
        }

        instances.clear()
    }

    companion object {
        /**
         * Löst den Pfad [path] relativ zum Basisverzeichnis der Vorlagen auf.
         * Das Basisverzeichnis der Vorlagen wird durch die Systemeigenschaft
         * `project.fixture.directory` bestimmt.
         */
        fun resolve(path: String): File {
            return File(System.getProperty("project.fixture.directory"))
                .resolve(path)
        }
    }
}
