package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.Pod

class Tipp {
    
    Pod accessEnd   = new Pod()
    Pod accessStart = new Pod()
    
    Pod medium      = new Pod("")
    Pod status      = new Pod("")  
    Pod url         = new Pod("")
    
    Pod title       = new Pod(PackageStruct.getNewTippTitle())
    Pod platform    = new Pod(PackageStruct.getNewTippPlatform())
    Pod coverage    = new Pod([]) // list
}
