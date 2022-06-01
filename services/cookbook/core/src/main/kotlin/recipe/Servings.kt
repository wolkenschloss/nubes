package family.haschka.wolkenschloss.cookbook.recipe

data class Servings(val amount: Int) {
    init {
        if (amount < MIN || amount > MAX) {
            throw IllegalArgumentException()
        }
    }

    override fun toString(): String {
        return amount.toString()
    }

    companion object {
        const val MIN = 1
        const val MAX = 100
    }
}