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

    val fixture: File get() = File(System.getProperty("project.fixture.directory"))
        .resolve(path)

    /**
     * Führt einen Code Block mit einer Kopie der Testdaten aus.
     *
     * Die Test-fixture wird in ein temporäres Verzeichnis kopiert.
     * Dem Codeblock [block] wird ein [File] Objekt übergeben, sodass
     * innerhalb des Codeblocks auf die Kopie zugegriffen werden
     * kann. Die Kopie wird durch mit [close] gelöscht.
     */
    fun withClone(block: suspend Instance.() -> Unit) = runBlocking {
        val instance =  Instance.from(fixture)
        temporaryDirectories.add(instance.target)
        block(instance)
    }

    fun overlay(instance: File) {
        val overlay = File(System.getProperty("project.fixture.directory")).resolve(path)
        overlay.copyRecursively(target = instance, overwrite = false)
    }

    fun removeOverlay(instance: File) {
        println("Remove overlay this: ${this.fixture.absolutePath} overlay: ${instance.absolutePath}")
        println("this: ${this.fixture.absolutePath}")
        println("overlay: ${instance.absolutePath}")

        val overlay = instance

        overlay.walkBottomUp().forEach {
            val relative = it.relativeTo(overlay)
            val inFixture = this.fixture.resolve(relative)
            println("deleting file ${relative.path} in ${inFixture.absolutePath}")

            if (inFixture.isFile) {
                inFixture.delete()
            }
        }
    }

    private val temporaryDirectories = arrayListOf<File>()

    /**
     * Löscht alle mit [withClone] erstellten Kopien der Fixture.
     */
    override fun close() {
        temporaryDirectories.reversed().forEach {directory ->
            directory.walkBottomUp().forEach { file -> file.delete() }
        }

        temporaryDirectories.clear()
    }
}
