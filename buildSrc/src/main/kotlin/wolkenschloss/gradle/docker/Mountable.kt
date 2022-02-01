package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import org.gradle.api.provider.Provider

interface Mountable {
    fun toMount(): Provider<Mount>
}
