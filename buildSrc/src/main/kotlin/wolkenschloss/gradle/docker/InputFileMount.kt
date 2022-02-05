package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import com.github.dockerjava.api.model.MountType
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile

abstract class InputFileMount : Mountable {
    @get:Input
    abstract val target: Property<String?>

    @get:InputFile
    abstract val source: RegularFileProperty

    override fun toMount(): Mount = Mount()
                .withSource(source.get().asFile.path)
                .withTarget(target.get())
                .withType(MountType.BIND)
                .withReadOnly(true)

}