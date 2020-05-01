package dnd.characters.models

class PlayerRace(val race: DndRace,
                 var enabled: Boolean = true) {

    override fun toString() = race.name

}