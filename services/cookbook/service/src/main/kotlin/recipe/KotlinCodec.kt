package family.haschka.wolkenschloss.cookbook.recipe

import org.bson.BsonReader
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import kotlin.reflect.KClass

class KotlinCodec<T : Any>(private val clazz: KClass<T>) : CollectibleCodec<T> {
    override fun encode(writer: BsonWriter?, value: T, encoderContext: EncoderContext?) {
        TODO("Not yet implemented")
    }

    override fun getEncoderClass(): Class<T> {
        TODO("Not yet implemented")
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): T {

        clazz.constructors.forEach {constructor ->
            constructor.parameters.forEach {parameter ->
                parameter.
            }
        }

        TODO("Not yet implemented")
    }

    override fun getDocumentId(document: T): BsonValue {
        TODO("Not yet implemented")
    }

    override fun documentHasId(document: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun generateIdIfAbsentFromDocument(document: T): T {
        TODO("Not yet implemented")
    }


}