package wolkenschloss.testing

import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Verwaltet Testprojekte für Funktionstests und Integrationstests.
 *
 * Die Testprojekte befinden sich im `/buildSrc/fixtures` Verzeichnis.
 * Für jeden Test wird eine Kopie (Klon) angelegt, damit der Test
 * nicht auf Erzeugnisse eines vorangegangenen Tests stößt, womit
 * Testergebnisse verfälscht würden.
 *
 * Das übliche Schema für die Verwendung von Fixtures ist:
 *
 * ```kotlin
 * class ExampleSpec : FunSpec({
 *   context("example build convention") {
 *     val fixture = autoClose(Fixture("example"))
 *     test("example build test") {
 *       fixture.withClone {
 *         val result = build("build", "-i")
 *         result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
 *       }
 *     }
 *   }
 * })
 *```
 *
 */
class Fixtures(private val path: String) : AutoCloseable {

    /**
     * Führt einen Code Block mit einer Instanz der Testdaten aus.
     *
     * Die Testdaten werden in ein temporäres Verzeichnis kopiert, auf das die
     * Testdaten Instanz verweist.
     */
    fun withClone(block: suspend Instance.() -> Unit) = runBlocking {
        val instance =  Instance.from(resolve(path))
        instances.add(instance)
        block(instance)
    }

    private val instances = arrayListOf<Instance>()

    /**
     * Löscht alle mit [withClone] erstellten Kopien der Fixture.
     */
    override fun close() {
        instances.reversed().forEach { instance ->
            instance.target.walkBottomUp().forEach { file -> file.delete() }
        }

        instances.clear()
    }

    companion object {
        fun resolve(path: String): File {
            return File(System.getProperty("project.fixture.directory"))
                .resolve(path)
        }
    }
}
