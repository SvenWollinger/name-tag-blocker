package io.wollinger.mc.nametagblocker

object BlockedDatabase {
    private val list: ArrayList<String> = ArrayList()

    fun add(name: String) {
        if(list.contains(name)) return
        list.add(name)
    }

    fun remove(name: String) {
        list.remove(name)
    }

    fun getAll() = list.toList()
}