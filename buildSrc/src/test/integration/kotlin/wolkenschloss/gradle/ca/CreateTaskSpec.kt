package wolkenschloss.gradle.ca

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.date.shouldHaveSameYearAs
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.time.Duration
import java.time.Period
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CreateTaskSpec : FunSpec({
    context("A project with create task") {
        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .withName(PROJECT_NAME)
            .build()

        project.pluginManager.apply(CaPlugin::class.java)

        test("certificate file defaults to xdg_home_dir/wolkenschloss/ca") {
            val create = project.tasks.create("create", CreateTask::class.java)
//            create.execute()

            create.certificate.get().toFile() shouldEndWithPath ".local/share/wolkenschloss/ca/ca.crt"
            create.privateKey.get().toFile() shouldEndWithPath ".local/share/wolkenschloss/ca/ca.key"

            create.notBefore.get().shouldBeWithin(Duration.ofSeconds(5), ZonedDateTime.now())
            create.notAfter.get().shouldBeWithin(Duration.ofSeconds(5), ZonedDateTime.now()
                .plus(CreateTask.DEFAULT_VALIDITY_PERIOD))
            println(create.certificate.get().toFile().absolutePath)
        }
    }
}) {
    companion object {
        const val PROJECT_NAME = "ca"
    }
}

private fun endWithPath(suffix: String) = object : Matcher<File> {
    override fun test(value: File) = MatcherResult(
        value.absolutePath.endsWith(suffix),
        "Path $value should end with $suffix",
        "Path $value should not end with $suffix"
    )
}

infix fun File.shouldEndWithPath(suffix: String) = this should endWithPath(suffix)
