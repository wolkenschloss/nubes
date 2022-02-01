package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory

import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import javax.inject.Inject

interface ContainerMounts {

    @Inject
    fun getObjectFactory(): ObjectFactory

    @Nested
    fun getInputs(): DomainObjectSet<InputFileMount>

    @get:Nested
    val mounts: Provider<List<Mount>>

    fun input(action: Action<in InputFileMount?>) {
        val mount: InputFileMount = getObjectFactory().newInstance(InputFileMount::class.java)
        action.execute(mount)
        getInputs().add(mount)
    }
}
