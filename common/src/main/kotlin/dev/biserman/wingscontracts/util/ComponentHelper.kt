package dev.biserman.wingscontracts.util

object ComponentHelper {
    @JvmStatic
    fun (String).trimBrackets() = this.trim { it == '[' || it == ']' }
}