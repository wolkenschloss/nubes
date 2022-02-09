package wolkenschloss.gradle.docker

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.tasks.TaskProvider
import java.io.File

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
