package dnd.characters.models

class PlayerClass(val dndClass: DndClass,
                  enabled: Boolean = true): PlayerBase(enabled) {

    override fun toString() = dndClass.name

    val subclasses = dndClass.subclasses.map {
        PlayerSubclass(it, true)
    }

    override fun match(regex: Regex): Boolean {
        return regex.containsMatchIn(dndClass.name)
    }

    override val type = "Class"

    override val name = dndClass.name

}