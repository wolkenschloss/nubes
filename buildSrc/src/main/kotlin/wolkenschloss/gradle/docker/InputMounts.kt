package wolkenschloss.gradle.docker

import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory

class  InputMounts(
    private val inputs: DomainObjectSet<InputFileMount>,
    private val inputDirs: DomainObjectSet<InputDirectoryMount>,
    private val objectFactory: ObjectFactory
) {
    fun file(block: InputFileMount.() -> Unit) {
        inputs.add(objectFactory.newInstance(InputFileMount::class.java).apply(block))
    }

    fun directory(block: InputDirectoryMount.() -> Unit) {
        inputDirs.add(objectFactory.newInstance(InputDirectoryMount::class.java).also(block))
    }
}
