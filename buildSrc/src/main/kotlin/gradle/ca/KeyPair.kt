package family.haschka.wolkenschloss.gradle.ca

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom

val random: SecureRandom by lazy {
    val rnd = SecureRandom()
    rnd.setSeed(SecureRandom.getSeed(20))
    rnd
}

fun KeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(KEYPAIR_GENERATOR_ALGORITHM)
    keyPairGenerator.initialize(3072, random)
    return keyPairGenerator.generateKeyPair()
}

private const val KEYPAIR_GENERATOR_ALGORITHM = "RSA"