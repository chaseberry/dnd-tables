package dnd.characters.models

abstract class PlayerBase(var enabled: Boolean) {

    abstract fun match(regex: Regex): Boolean

    abstract val type: String

    abstract val name: String

}