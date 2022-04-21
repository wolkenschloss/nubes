package wolkenschloss.gradle.ca

class InvalidArgumentException(val value: String) : Throwable() {
    constructor(tagNo: Int) : this(tagNo.toString())
}
