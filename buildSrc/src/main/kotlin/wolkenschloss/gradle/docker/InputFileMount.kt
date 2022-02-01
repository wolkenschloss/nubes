package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import com.github.dockerjava.api.model.MountType
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import javax.inject.Inject

abstract class InputFileMount : Mountable {
    @get:Input
    abstract val target: Property<String?>

    @get:InputFile
    abstract val source: RegularFileProperty

    @get:Inject
    abstract val providerFactory: ProviderFactory

    override fun toMount(): Provider<Mount> {
        return providerFactory.provider {
            Mount()
                .withSource(source.get().asFile.path)
                .withTarget(target.get())
                .withType(MountType.BIND)
                .withReadOnly(true)
        }
    }
}