package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount

interface Mountable {
    fun toMount(): Mount
}
