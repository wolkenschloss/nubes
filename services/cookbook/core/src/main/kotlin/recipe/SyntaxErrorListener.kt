package family.haschka.wolkenschloss.cookbook.recipe

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

class SyntaxErrorListener : BaseErrorListener() {

    private var _errors = emptyList<String>()

    var errors: List<String>
        get() = _errors
        private set(value) {
            _errors = value
        }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e)
        errors = errors + listOf(msg)
    }
}