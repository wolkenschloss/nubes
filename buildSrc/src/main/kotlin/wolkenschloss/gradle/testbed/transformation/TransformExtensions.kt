package family.haschka.wolkenschloss.gradle.testbed.transformation

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider

val TaskProvider<Copy>.userData get() = file(TransformationTasks.USER_DATA_FILE_NAME)
private fun TaskProvider<Copy>.file(path: String) = this.map { it.destinationDir.resolve(path) }