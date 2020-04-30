package dnd.characters

import java.io.File
import java.io.InputStream
import java.nio.file.Paths

object Resources {

    private val s: String = File.separator

    fun load(name: String): InputStream {
        return try {
            javaClass.classLoader.getResourceAsStream(name)!!
        } catch (e: Exception) {
            val f = goUp(File(javaClass.getResource(s).path))

            Paths.get(f.path, "src${s}main${s}resources$s", name).toUri().toURL().openStream()
        }
    }

    fun find(name: String): String? {
        return try {
            javaClass.classLoader.getResource(name)?.toString()!!
        } catch (e: Exception) {
            val f = goUp(File(javaClass.getResource(s).path))

            Paths.get(f.path, "src${s}main${s}resources$s", name).toUri().toString()
        }
    }


    private fun goUp(file: File): File {
        if (file.isDirectory && file.listFiles().any { it.name == "src" }) {
            return file
        }

        return goUp(file.parentFile)
    }

}