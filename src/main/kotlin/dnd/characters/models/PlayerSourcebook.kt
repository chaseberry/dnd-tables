package dnd.characters.models

class PlayerSourcebook(val book: Sourcebook,
                       enabled: Boolean = true) : PlayerBase(enabled) {

    override fun match(regex: Regex): Boolean {
        return regex.containsMatchIn(book.name) || regex.containsMatchIn(book.code)
    }

    override val type = "Sourcebook"

    override val name = book.name

}