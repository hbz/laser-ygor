package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.Pod

class TippTitle {
    
    Pod name        = new Pod("")
    Pod type        = new Pod("Serial", Status.HARDCODED)
    Pod identifiers = new Pod([]) // list
}