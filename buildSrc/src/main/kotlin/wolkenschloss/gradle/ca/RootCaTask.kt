package wolkenschloss.gradle.ca

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.security.KeyPairGenerator

abstract class RootCaTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun execute() {
        outputDir.get().asFile.mkdirs()
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        val keyPair = keyPairGenerator.genKeyPair()

        val caPrivateKeyFile = outputDir.file("ca.key").get().asFile
        caPrivateKeyFile.createNewFile()
        caPrivateKeyFile.writeBytes(keyPair.private.encoded)

        val caPublicKeyFile = outputDir.file("ca.pub").get().asFile
        caPublicKeyFile.createNewFile()
        caPublicKeyFile.writeBytes(keyPair.public.encoded)
    }
}