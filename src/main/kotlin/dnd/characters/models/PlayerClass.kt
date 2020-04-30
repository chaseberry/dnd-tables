package dnd.characters.models

class PlayerClass(val dndClass: DndClass,
                  var enabled: Boolean = true) {

    override fun toString() = dndClass.name

    val subclasses = dndClass.subclasses.map {
        PlayerSubclass(it, true)
    }

}