package wolkenschloss.gradle.testbed.pool

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class BuildDataSourceImage : Exec() {
    init {
        commandLine("cloud-localds")
    }

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val networkConfig: Property<File>

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val userData: Property<File>

    @get:OutputFile
    abstract val dataSourceImage: RegularFileProperty

    @TaskAction
    public override fun exec() {
        args(
            "--network-config",
            networkConfig.get(),
            dataSourceImage.get(),
            userData.get()
        )

        super.exec()
    }
}