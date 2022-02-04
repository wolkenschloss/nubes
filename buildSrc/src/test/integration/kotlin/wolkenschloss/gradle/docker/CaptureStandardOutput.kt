package wolkenschloss.gradle.docker

import org.gradle.api.logging.StandardOutputListener
import java.lang.StringBuilder

class CaptureStandardOutput: StandardOutputListener {
    private val outputs = StringBuilder()

    val output: String
    get() = outputs.toString()

    override fun onOutput(output: CharSequence?) {
        outputs.append(output)
    }
}