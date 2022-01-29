package wolkenschloss.testing

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.*


fun File.build(vararg args: String): BuildResult = GradleRunner.create()
    .withProjectDir(this)
    .withArguments(*args)
    .withPluginClasspath()
    .build()

fun File.properties(path: String): Properties {
    val properties = Properties()
    val file = this.resolve(path)
    file.inputStream().use {
        properties.load(it)
    }
    return properties
}