//package family.haschka.wolkenschloss.cookbook.recipe
//
//import family.haschka.wolkenschloss.cookbook.Blueprint
//import family.haschka.wolkenschloss.cookbook.Entity
//
//@Entity
//@Blueprint(["Entity"])
//data class Image(val _id: String, val contentType: String, val byteArray: ByteArray) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Image
//
//        if (_id != other._id) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        return _id.hashCode()
//    }
//
//    override fun toString(): String {
//        return "Image(_id='$_id', contentType='$contentType')"
//    }
//}