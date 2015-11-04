package org.rorschach.complex

import java.util.*

data class FieldDepend(val target: String) {
    val depend : MutableSet<String> = HashSet()
}