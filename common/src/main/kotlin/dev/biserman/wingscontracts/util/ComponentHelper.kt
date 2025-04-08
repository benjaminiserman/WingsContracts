package dev.biserman.wingscontracts.util

object ComponentHelper {
    fun (String).trimBrackets() = this.trim { it == '[' || it == ']' }
}