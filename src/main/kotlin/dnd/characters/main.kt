package dnd.characters

import dnd.characters.models.*
import edu.csh.chase.kjson.Json

fun main() {

    val sources = loadSources()?.sortedBy { it.name } ?: return println("No sources")
    val classes = loadClasses()?.sortedBy { it.name } ?: return println("No classes")
    val races = loadRaces()?.sortedBy { it.name } ?: return println("No races")

    val c = classes.map { PlayerClass(it) }
    val s = sources.map { PlayerSourcebook(it) }
    val r = races.map { PlayerRace(it) }

    val die = cli(c, s, r)

    val allowedSourceCodes = s.filter { it.enabled }.map { it.book.code }

    r.forEach {
        if (it.race.source !in allowedSourceCodes) {
            it.enabled = false
        }
    }

    c.forEach {
        if (it.dndClass.source !in allowedSourceCodes) {
            it.enabled = false
            it.subclasses.forEach { it.enabled = false }
        }

        it.subclasses.forEach {
            if (it.dndSubClass.source !in allowedSourceCodes) {
                it.enabled = false
            }
        }
    }

    createTable(c, r, die)
}

fun cli(classes: List<PlayerClass>, sources: List<PlayerSourcebook>, races: List<PlayerRace>): Boolean {
    println("Note: Sourcebook filters are applied after specific class/subclass filters\n")

    when (get("Starting Source Books [all, none, default]: ")?.toLowerCase()) {
        "all" -> Unit
        "none" -> sources.forEach { it.enabled = false }
        else -> sources.forEach { it.enabled = it.book.code in listOf("DMG", "PHB", "XGTE") }
    }

    println("Sources: ${sources.books()}")

    if (get("Starting Classes [all, none]: ")?.toLowerCase() == "none") {
        classes.forEach { it.enabled = false }
    }

    println("Classes: ${classes.classes()}")

    loop@ while (true) {
        val input = get("> ")?.takeIf { it.isNotBlank() }?.trim() ?: continue
        val rn = input.toLowerCase().split(Regex("\\s+"))

        val arg = rn.getOrNull(1)

        when (rn.getOrNull(0)) {
            "?", "help" -> {
                println(
                    """
                    ?, help                show this message
                    q, quit, exit          finish and get tables
                    sources                list available source books
                    classes                list available classes
                    subs, subclasses       list subclasses, will prompt for class
                    +class, -class         add/remove class, will prompt for class
                    +source, -source       add/remove source book, will prompt for book
                    +race, -race           add/remove race, will prompt for race
                    +sub, -sub             fuck you not done yet
                """.trimIndent()
                )
            }
            "q", "quit", "exit" -> break@loop
            "sources" -> println("Sources: ${sources.books()}")
            "classes" -> println("Classes: ${classes.classes()}")
            "subs", "subclasses" -> (arg ?: get("Class: "))?.let { i ->
                val r = Regex(i, RegexOption.IGNORE_CASE)
                classes.find {
                    r.containsMatchIn(it.dndClass.name)
                }?.let {
                    println("Subclasses for ${it.dndClass.name}: [${it.subclasses.filter { it.enabled }.joinToString { it.dndSubClass.name }}]")
                } ?: println("No class '$i'")
            }
            "+source", "+book" -> (arg ?: get("Source book (will match as best as can): "))?.toLowerCase()?.let {
                val r = Regex(it, RegexOption.IGNORE_CASE)
                sources.find { r.containsMatchIn(it.book.name) || r.containsMatchIn(it.book.code) }?.let {
                    if (get("Add ${it.book.name}? [y/n]: ")?.first()?.toLowerCase() == 'y') {
                        it.enabled = true
                    }
                } ?: println("No book found for '$it'")
            }
            "-source", "-book" -> (arg ?: get("Source book (will match as best as can): "))?.toLowerCase()?.let {
                val r = Regex(it, RegexOption.IGNORE_CASE)
                sources.find { r.containsMatchIn(it.book.name) || r.containsMatchIn(it.book.code) }?.let {
                    if (get("Remove ${it.book.name}? [y/n]: ")?.first()?.toLowerCase() == 'y') {
                        it.enabled = false
                    }
                } ?: println("No book found for '$it'")
            }
            "+class" -> (arg ?: get("Class (will match as best as can): "))?.toLowerCase()?.let {
                val r = Regex(it, RegexOption.IGNORE_CASE)
                classes.find { r.containsMatchIn(it.dndClass.name) }?.let {
                    if (get("Add ${it.dndClass.name}? [y/n]: ")?.first()?.toLowerCase() == 'y') {
                        it.enabled = true
                    }
                } ?: println("No book found for '$it'")
            }
            "-class" -> (arg ?: get("Class (will match as best as can): "))?.toLowerCase()?.let {
                val r = Regex(it, RegexOption.IGNORE_CASE)
                classes.find { r.containsMatchIn(it.dndClass.name) }?.let {
                    if (get("Remove ${it.dndClass.name}? [y/n]: ")?.first()?.toLowerCase() == 'y') {
                        it.enabled = false
                    }
                } ?: println("No book found for '$it'")
            }
            "+race" -> (arg ?: get("Race (will match as best as can): "))?.toLowerCase()?.let {
                val r = Regex(it, RegexOption.IGNORE_CASE)
                races.find { r.containsMatchIn(it.race.name) }?.let {
                    if (get("Add ${it.race.name}? [y/n]: ")?.first()?.toLowerCase() == 'y') {
                        it.enabled = true
                    }
                } ?: println("No book found for '$it'")
            }
            "-race" -> (arg ?: get("Race (will match as best as can): "))?.toLowerCase()?.let {
                val r = Regex(it, RegexOption.IGNORE_CASE)
                races.find { r.containsMatchIn(it.race.name) }?.let {
                    if (get("Remove ${it.race.name}? [y/n]: ")?.first()?.toLowerCase() == 'y') {
                        it.enabled = false
                    }
                } ?: println("No book found for '$it'")
            }
            else -> {
                val c = input[0]
                if (c != '+' && c != '-') {
                    continue@loop
                }
                val add = c == '+'

                val match = Regex(input.drop(1), RegexOption.IGNORE_CASE)

                val text = "${if (add) "Add" else "Remove"} %s %s? [y/n]: "

                sources.find { match.containsMatchIn(it.book.name) || match.containsMatchIn(it.book.code) }?.let {
                    if (get(text.format("Sourcebook", it.book.name))?.first()?.toLowerCase() == 'y') {
                        it.enabled = add
                    }
                }

                classes.find { match.containsMatchIn(it.dndClass.name) }?.let {
                    if (get(text.format("Class", it.dndClass.name))?.first()?.toLowerCase() == 'y') {
                        it.enabled = add
                    }
                }

                races.find { match.containsMatchIn(it.race.name) }?.let {
                    if (get(text.format("Race", it.race.name))?.first()?.toLowerCase() == 'y') {
                        it.enabled = add
                    }
                }
            }
        }
    }

    return get("Use standard die values for tables? [y/n]: ")?.first()?.toLowerCase() == 'y'
}

fun List<PlayerSourcebook>.books() = filter { it.enabled }.joinToString(", ", "[", "]") { it.book.name }

fun List<PlayerClass>.classes() = filter { it.enabled }.joinToString(", ", "[", "]") { it.dndClass.name }

fun createTable(classes: List<PlayerClass>, races: List<PlayerRace>, useStandardDie: Boolean) {

    val dieList = listOf(20, 12, 10, 8, 6, 4, 2)

    val c = if (useStandardDie) {
        dieList.limit(classes.filter { it.enabled }.shuffled())
    } else {
        classes.filter { it.enabled }.shuffled()
    }.shuffled() //More random!

    println("## Classes")
    println("| Roll | Class")
    println("| --- | ---")
    c.forEachIndexed { i, it -> println("| ${i + 1} | $it") }

    println()

    c.sortedBy { it.dndClass.name }.forEach {
        val s = if (useStandardDie) {
            dieList.limit(it.subclasses.filter { it.enabled }.shuffled())
        } else {
            it.subclasses.filter { it.enabled }.shuffled()
        }.shuffled()

        println("## $it")
        println("| Roll | Subclass")
        println("| --- | ---")
        s.forEachIndexed { i, sc -> println("| ${i + 1} | $sc") }
        println()
    }

    val r = if (useStandardDie) {
        dieList.limit(races.filter { it.enabled }.shuffled())
    } else {
        races.filter { it.enabled }.shuffled()
    }.shuffled()

    println("## Races")
    println("| Roll | Race")
    println("| --- | ---")
    r.forEachIndexed { i, it -> println("| ${i + 1} | $it") }

}

fun <T> List<Int>.limit(other: List<T>): List<T> {
    val size = this.find { other.size >= it } ?: 0
    return other.take(size)
}

fun dumpData(classes: List<DndClass>, sources: List<Sourcebook>) {
    classes.groupBy { it.source }.forEach { src, cls ->
        println(sources.find(src))
        cls.forEach {
            println("  $it")
            it.subclasses.groupBy { it.source }.forEach { src, sub ->
                println("    ${sources.find(src)}")
                sub.forEach { println("      $it") }
            }
            println()
        }
        println()
    }
}

fun List<Sourcebook>.find(code: String) = find { it.code == code }?.name ?: code

fun loadClasses(): List<DndClass>? {
    val j = Json.parseToArray(String(Resources.load("classes.json").use { it.readBytes() })) ?: return null

    return j.getInternalArray().map {
        (it as Map<String, Any?>)
        DndClass(
            name = it["name"].toString(),
            source = it["source"].toString(),
            subclasses = (it["subclasses"] as List<*>).map {
                (it as Map<String, Any?>)
                DndSubClass(
                    name = it["name"].toString(),
                    source = it["source"].toString()
                )
            }.sortedBy { it.name }
        )
    }

}

fun loadSources(): List<Sourcebook>? {

    val j = Json.parseToArray(String(Resources.load("sources.json").use { it.readBytes() })) ?: return null

    return j.getInternalArray().map {
        (it as Map<String, Any?>)
        Sourcebook(
            name = it["name"].toString(),
            code = it["code"].toString()
        )
    }

}

fun loadRaces(): List<DndRace>? {
    val j = Json.parseToArray(String(Resources.load("races.json").use { it.readBytes() })) ?: return null

    return j.getInternalArray().map {
        (it as Map<String, Any?>)
        DndRace(
            name = it["name"].toString(),
            source = it["source"].toString()
        )
    }
}

fun get(message: String, default: String? = null): String? {
    print(message)
    return readLine() ?: default
}