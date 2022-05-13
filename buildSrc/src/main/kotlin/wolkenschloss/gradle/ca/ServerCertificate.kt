package wolkenschloss.gradle.ca

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile

abstract class ServerCertificate : DefaultTask() {

    @get:InputFile
    abstract val caCertificate: RegularFileProperty

    @get:InputFile
    abstract val caPrivateKey: RegularFileProperty

    @get:OutputFile
    abstract val certificate: RegularFileProperty

    @get:OutputFile
    abstract val privateKey: RegularFileProperty
}