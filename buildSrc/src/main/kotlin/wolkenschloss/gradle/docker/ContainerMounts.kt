package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import javax.inject.Inject

abstract class ContainerMounts {

    @get:Inject
    abstract val providerFactory: ProviderFactory

    @get:Inject
    abstract val objectFactory: ObjectFactory

    @get:Nested
    abstract val inputs: DomainObjectSet<InputFileMount>

    @get:Nested
    abstract val outputs: DomainObjectSet<OutputDirectoryMount>

    @get:Internal
    val mounts: Provider<List<Mount>>
        get() = providerFactory.provider {
            inputs.map(InputFileMount::toMount) +
                    outputs.map(OutputDirectoryMount::toMount)
        }


    fun input(block: InputFileMount.() -> Unit) {
        inputs.add(objectFactory.newInstance(InputFileMount::class.java).apply(block))
    }

    fun directory(block: InputDirectoryMount.() -> Unit) {

    }

    fun output(block: OutputDirectoryMount.() -> Unit) {
        outputs.add(objectFactory.newInstance(OutputDirectoryMount::class.java).apply(block))
    }
}
