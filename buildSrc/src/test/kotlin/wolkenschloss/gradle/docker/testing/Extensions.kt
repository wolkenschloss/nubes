package wolkenschloss.gradle.docker.testing

import com.github.dockerjava.api.model.Image
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.tasks.TaskProvider
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import wolkenschloss.gradle.docker.BuildImageTask
import java.io.File

val Image.shortId: String
    get() = id.removePrefix("sha256:").take(12)

fun TaskProvider<BuildImageTask>.forceRemoveImage() {
    val task = get()
    val file = task.imageId.get().asFile
    if (file.exists()) {
        val imageId = file.readText()
        task.dockerService.get()
            .client.removeImageCmd(imageId)
            .withForce(true)
            .exec()
    }
}

operator fun <T : FileSystemLocation> FileSystemLocationProperty<T>.minus(projectDir: File): String {
    return get().asFile.relativeToOrNull(projectDir).toString()
}

fun File.build(vararg args: String): BuildResult = GradleRunner.create()
    .withProjectDir(this)
    .withArguments(*args)
    .withPluginClasspath()
    .build()