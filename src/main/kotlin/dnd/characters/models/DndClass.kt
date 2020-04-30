package dnd.characters.models

data class DndClass(val name: String,
               val source: String,
               val subclasses: List<DndSubClass>) {

    fun removeSubclasses(subs: List<String>) = DndClass(
        name = name,
        source = source,
        subclasses = subclasses.filter { it.name !in subs }
    )

}