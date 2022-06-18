package family.haschka.wolkenschloss.cookbook.recipe

import family.haschka.wolkenschloss.cookbook.Blueprint
import family.haschka.wolkenschloss.cookbook.Entity


@Entity
//@MongoEntity
@Blueprint(["Entity"])
data class Image2(
    var id: String,
    var contentType: String,
    var byteArray: ByteArray)

//data class Image2 @BsonCreator constructor(
//    @BsonId @BsonProperty("_id") var _id: String,
//    @BsonProperty("contentType") var contentType: String,
//    @BsonProperty("byteArray") var byteArray: ByteArray)
{

//    val _id: String = _id
//    get() {
//        println("getting _id: $field")
//        return field
//    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image2

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Image(_id='$id', contentType='$contentType')"
    }
}