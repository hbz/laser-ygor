package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class PackageStruct {
    
    // Package
    
    static Tipp getNewTipp() {
        return new Tipp()
    }

    // PackageHeader
    
    static PackageHeaderVariantName getNewPackageHeaderVariantName() {
        return new PackageHeaderVariantName()
    }
    
    static PackageHeaderCuratoryGroup getNewPackageHeaderCuratoryGroup() {
        return new PackageHeaderCuratoryGroup()
    }
    
    static PackageHeaderSource getNewPackageHeaderSource() {
        return new PackageHeaderSource()
    }
    
    // Tipps
    
    static TippCoverage getNewTippCoverage() {
        return new TippCoverage()
    }
    
    static TippPlatform getNewTippPlatform() {
        return new TippPlatform()
    }
    
    static TippTitle getNewTippTitle() {
        return new TippTitle()
    }
    
    // TippTitle
    
    static Identifier getNewTippTitleIdentifier() {
        return new Identifier()
    }
    
    // --- Presets / GOKb(phaeton.hbz-nrw.de) 2017.01.20 ---
    
    static Map getPackageHeaderNominalPlatformPreset() {
    
        return [
            "ACM Digital Library" : "http://dl.acm.org/",
            "ACS Publications" : "http://pubs.acs.org/",
            "AIP Scitation" : "http://scitation.aip.org/",
            "American Chemical Society" : "",
            "American Institute of Physics" : "http://scitation.aip.org/admin/reporting/kbart/list.action",
            "American Mathematical Society" : "",
            "American Medical Association" : "",
            "American Physical Society" : "http://www.the-aps.org/mm/Publications",
            "American Psychological Association" : "http://supp.apa.org/kbart/journals/",
            "American Society for Microbiology" : "",
            "American Society of Civil Engineers" : "",
            "American Society of Mechanical Engineers" : "",
            "Annual Reviews" : "http://www.annualreviews.org/",
            "BioOne" : "http://www.bioone.org/",
            "BMJ Publications" : "",
            "Brepols Publishers" : "",
            "Brill" : "http://www.brill.com/",
            "Budrich Journals" : "http://budrich-journals.de",
            "BWV Digitale Bibliothek" : "http://bwv.verlag-online.eu/digibib/bwv/",
            "Cambridge University Press" : "http://www.cambridge.org/",
            "Content Select" : "https://content-select.com",
            "CUFTS Open Knowledgebase" : "",
            "Cultura - Hombre - Sociedad" : "",
            "DeGruyter Online" : "http://www.degruyter.com/",
            "Directory of Open Access Journals" : "",
            "Duke University Press" : "https://www.dukeupress.edu/",
            "Duncker & Humblot eJournals" : "http://ejournals.duncker-humblot.de/",
            "East View Information Services" : "",
            "Edinburgh University Press" : "http://www.euppublishing.com/action/showPublications?display=bySubject&pubType=journal",
            "Elsevier ScienceDirect" : "http://www.sciencedirect.com/",
            "EMBO Press" : "http://embopress.org/",
            "Emerald Insight" : "http://www.emeraldinsight.com/",
            "ESVCampus" : "www.esvcampus.de",
            "Gale" : "",
            "GeoScienceWorld" : "http://www.geoscienceworld.org/",
            "GSA Publications" : "http://www.gsapubs.org/",
            "Hanser eLibrary" : "",
            "HeinOnline" : "",
            "HighWire" : "http://cufts2.lib.sfu.ca/knowledgebase/",
            "Hogrefe eContent" : "http://econtent.hogrefe.com",
            "IEEE Xplore" : "http://ieeexplore.ieee.org",
            "ingentaconnect" : "www.ingentaconnect.com",
            "IOPscience" : "http://iopscience.iop.org",
            "Iowa Research Online" : "",
            "Journals.ASM.org" : "http://journals.asm.org/",
            "JSTOR" : "",
            "Karger" : "http://www.karger.com",
            "Lab Animal" : "http://www.labanimal.com",
            "Laboratory Investigation" : "www.laboratoryinvestigation.org",
            "link.springer.com" : "link.springer.com",
            "Loeb Classical Library" : "http://www.loebclassics.com/",
            "Maney Publishing" : "http://maneypublishing.com/",
            "Mary Ann Liebert, Inc. Publishers" : "http://www.liebertpub.com/",
            "Microscopy Today" : "http://microscopy-today.com/",
            "nature.com" : "http://www.nature.com/",
            "Nomos eLibrary" : "http://www.nomos-elibrary.de/",
            "OECD" : "",
            "OhioLINK" : "",
            "Open Journal System" : "",
            "Ovid" : "",
            "Oxford University Press Journals" : "http://oxfordjournals.org",
            "Project Euclid" : "http://projecteuclid.org/",
            "Project Muse" : "http://muse.jhu.edu/",
            "Revista Iberoamericana de Viticultura, Agroindustria y Ruralidad" : "http://revistarivar.cl/",
            "Revista iZQuierdas" : "http://www.izquierdas.cl",
            "Revistas y Publicaciones Ediciones Electrónicas" : "http://www.revistas.usach.cl",
            "Royal Society of Chemistry" : "",
            "R&W Online" : "http://www.ruw.de/",
            "Sage" : "http://www.sagepub.com",
            "Schulz-Kirchner" : "http://www.schulz-kirchner.de/",
            "Science" : "http://www.sciencemag.org/",
            "SpringerLink" : "http://link.springer.com",
            "Taylor & Francis Online" : "http://www.tandfonline.com",
            "Thieme Connect" : "https://www.thieme-connect.de/products/all/home.html",
            "UNC Greensboro" : "http://libjournal.uncg.edu/",
            "Vahlen eLibrary" : "http://elibrary.vahlen.de/",
            "V&R eLibrary" : "http://www.vr-elibrary.de/",
            "Wiley Online Library" : "http://onlinelibrary.wiley.com/",
            "WorldSciNet" : "",
            "World Textile Information Network" : ""
        ] 
    }
    
}

