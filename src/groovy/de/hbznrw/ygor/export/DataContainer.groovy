package de.hbznrw.ygor.export

import de.hbznrw.ygor.export.structure.Meta
import de.hbznrw.ygor.export.structure.Package

class DataContainer {

    Meta info
    Package pkg
    HashMap titles
    
    DataContainer() {

        info = new Meta(
            file:   "TODO",
            type:   "TODO",
            ygor:   "TODO",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
            api:    [],
            stats:  [:],
            stash:  [:]
        )
        pkg      = new Package()
        titles   = [:]
    }
}