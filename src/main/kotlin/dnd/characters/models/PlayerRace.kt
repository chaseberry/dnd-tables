package dnd.characters.models

class PlayerRace(val race: DndRace,
                 enabled: Boolean = true) : PlayerBase(enabled) {

    override fun toString() = race.name

    override fun match(regex: Regex): Boolean {
        return regex.containsMatchIn(race.name)
    }

    override val type = "Race"

    override val name = race.name

}