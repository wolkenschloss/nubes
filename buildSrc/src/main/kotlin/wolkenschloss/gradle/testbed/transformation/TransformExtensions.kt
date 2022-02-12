package wolkenschloss.gradle.testbed.transformation

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider

val TaskProvider<Copy>.networkConfig get() = file(TransformationTasks.NETWORK_CONFIG_FILE_NAME)
val TaskProvider<Copy>.userData get() = file(TransformationTasks.USER_DATA_FILE_NAME)
val TaskProvider<Copy>.poolDescription get() = file(TransformationTasks.POOL_DESCRIPTION_FILE_NAME)
val TaskProvider<Copy>.domainDescription get() = file(TransformationTasks.DOMAIN_DESCRIPTION_FILE_NAME)
private fun TaskProvider<Copy>.file(path: String) = this.map { it.destinationDir.resolve(path) }