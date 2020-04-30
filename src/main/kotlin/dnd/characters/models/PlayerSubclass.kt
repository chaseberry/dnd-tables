package dnd.characters.models

class PlayerSubclass(val dndSubClass: DndSubClass,
                     var enabled: Boolean) {

    override fun toString() = dndSubClass.name

}