package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import com.github.dockerjava.api.model.MountType
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory

abstract class InputDirectoryMount : Mountable {
    @get:Input
    abstract val target: Property<String?>

    @get:InputDirectory
    abstract val source: DirectoryProperty

    override fun toMount(): Mount = Mount()
        .withSource(source.get().asFile.path)
        .withTarget(target.get())
        .withType(MountType.BIND)
        .withReadOnly(true)
}
