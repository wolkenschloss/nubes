package wolkenschloss.testing

import java.io.File
import java.nio.file.Paths

/**
 * Verwaltet Testprojekte für Funktionstests und Integrationstests.
 *
 * Die Testprojekte befinden sich im [/buildSrc/fixtures](/buildSrc/fixtures) Verzeichnis.
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

    @Deprecated(
        "Useless for fixtures that want to run files in temporary directories.",
        replaceWith = ReplaceWith("withClone"))
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

    /**
     * Führt einen Code Block mit einer Kopie der Testdaten aus.
     *
     * Die Test-fixture wird in ein temporäres Verzeichnis kopiert.
     * Dem Codeblock [block] wird ein [File] Objekt übergeben, sodass
     * innerhalb des Codeblocks auf die Kopie zugegriffen werden
     * kann. Die Kopie wird durch mit [close] gelöscht.
     */
    fun withClone(block: File.() -> Unit) {
        val clone = clone()
        block(clone)
    }

    private fun defaultFixturePath() = userDirectory().resolve("fixtures")

    private fun overlay(fixture: File) {
        val overlay = File(System.getProperty("project.fixture.directory")).resolve(path)
        overlay.copyRecursively(target = fixture, overwrite = false)
    }

    @Deprecated("Funktioniert nicht mir withClone")
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
        private fun userDirectory(): File = Paths.get(System.getProperty("user.dir")).toFile()

        // It is not possible to use the temporary directories provided by
        // Kotest or Java for cloned fixtures.
        //
        // The node_modules directory contains executable files that are
        // required for the test. No files may be executed in ordinary temporary
        // directories. This leads to an error in the test.
        // val fixture = Fixtures.temporaryBuildDirectory()
        private fun temporaryBuildDirectory(): File = userDirectory()
            .resolve(Paths.get("build", "tmp", "fixture").toFile())
    }

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
