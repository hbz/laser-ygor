package ygor

// ignore strange errors ..
import grails.util.Environment
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.POST

import org.apache.commons.io.IOUtils
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.MultipartFormEntity
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.entity.mime.content.FileBody

import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.export.structure.Pod
import de.hbznrw.ygor.iet.export.structure.PackageStruct


class EnrichmentController {

    static scope = "session"

    def documents = [:]

    def index = { redirect(action:'process')   }
    
    def process = {
        render(
            view:'process',
            model:[documents:documents, currentView:'process']
            )
    }
    
    def json = {
        render(
            view:'json',
            model:[documents:documents, currentView:'json']
            )
    }
    
    def howto = {
        render(
            view:'howto',
            model:[currentView:'howto']
            )
    }

    def about = {
        render(
            view:'about',
            model:[currentView:'about']
            )
    }

    def contact = {
        render(
            view:'contact',
            model:[currentView:'contact']
            )
    }
    
    def uploadFile = {

        def file = request.getFile('uploadFile')
        if (file.empty) {
            flash.info    = null
            flash.warning = null
            flash.error   = 'Sie müssen eine gültige Datei auswählen.'
            render(view:'process', model:[
                documents:documents,
                currentView: 'process'
                ]
            )
            return
        }

        def en = new Enrichment(getSessionFolder(), file.originalFilename)
        en.setStatus(Enrichment.ProcessingState.PREPARE)
        documents << ["${en.originHash}":en]
        
        file.transferTo(new File(en.originPathName))

        redirect(action:'process')
    }

    def prepareFile = {
        
        def pm = request.parameterMap
        def ph = enrichment.dataContainer.pkg.packageHeader
        
        if(!pm['ignorePkgData']) {
            
            ph.v.name.v = new Pod(pm['pkgTitle'][0])
            
            def preset = PackageStruct.getPackageHeaderNominalPlatformPreset()
            def pkgNominal = preset.find{it.key == pm['pkgNominal'][0]}
            if(pkgNominal){
                ph.v.nominalPlatform.v = pkgNominal.value
                ph.v.nominalProvider.v = pkgNominal.key
            }
            
            def vn =  PackageStruct.getNewPackageHeaderVariantName()
            vn.variantName.v = pm['pkgVariantName'][0]
            ph.v.variantNames << vn 
        }
        
        enrichment.setStatus(Enrichment.ProcessingState.UNTOUCHED)
        redirect(action:'process')
    }
    
    def processFile = {
        
        def pmIndex     = request.parameterMap['processIndex'][0]
        def pmIndexType = request.parameterMap['processIndexType'][0]
        def pmOptions   = request.parameterMap['processOption']

        println request.parameterMap['processOption']
        
        if(!pmIndex) {
            flash.info    = null
            flash.warning = 'Geben Sie einen gültigen Index an.'
            flash.error   = null
        }
        else if(!pmOptions) {
            flash.info    = null
            flash.warning = 'Wählen Sie mindestens eine Anreicherungsoption.'
            flash.error   = null
        }
        else {
            def en = getEnrichment()
            def options = [
                'indexOfKey':   pmIndex.toInteger() - 1,
                'typeOfKey':    pmIndexType,
                'options':      pmOptions,
                'ygorVersion':  grailsApplication.config.ygor.version,
                'ygorType':     grailsApplication.config.ygor.type
                ]
            
            if(en.status != Enrichment.ProcessingState.WORKING) {
                flash.info    = 'Bearbeitung gestartet.'
                flash.warning = null
                flash.error   = null

                en.process(options)
            }
        }
        render(
                view:'process',
                model:[
                    documents:   documents, 
                    currentView: 'process',
                    pIndex:      pmIndex,
                    pIndexType:  pmIndexType,
                    pOptions:    pmOptions,
                    ]
                )
    }
    def stopProcessingFile = {
        
        def en = getEnrichment()
        en.thread.isRunning = false
        
        deleteFile()
    }
    
    def deleteFile = {

        def en = getEnrichment()
        if(en) {
            def origin = en.getFile(Enrichment.FileType.ORIGIN)
            if(origin)
                origin.delete()
            documents.remove("${en.originHash}")
        }
        render(
                view:'process',
                model:[documents:documents, currentView:'process']
                )
    }
    
    def downloadPackageFile() {

        def en     = getEnrichment()
        def result = en.getFile(Enrichment.FileType.JSON_PACKAGE)
        render(
                file:result,
                fileName:"${en.resultName}.package.json"
                )
    }
    
    def downloadTitlesFile() {
        
        def en     = getEnrichment()
        def result = en.getFile(Enrichment.FileType.JSON_TITLES)
        render(
                file:result,
                fileName:"${en.resultName}.titles.json"
                )
    }
    
    def downloadDebugFile() {
        
        def en     = getEnrichment()
        def result = en.getFile(Enrichment.FileType.JSON_DEBUG)
        render(
                file:result,
                fileName:"${en.resultName}.debug.json"
                )
    }
    
    def downloadRawFile() {
        
        def en     = getEnrichment()
        def result = en.getFile(Enrichment.FileType.JSON_RAW)
        render(
                file:result,
                fileName:"${en.resultName}.raw.json"
                )
    }
    
    def exportFile() {
        
        // TODO split file
        //return only json.package
        //return only json.titles

        def en      = getEnrichment()
        def rawFile = en.getFile(Enrichment.FileType.JSON)
        def result  = Mapper.clearUp(rawFile)
        def http    = new HTTPBuilder(grailsApplication.config.gokbApi.xrTitleUri)
        
        http.auth.basic grailsApplication.config.gokbApi.user, grailsApplication.config.gokbApi.pwd

        println "EC.exportFile(" + en.resultHash + ") -> " + grailsApplication.config.gokbApi.xrTitleUri
        
        http.request(POST) { req ->
            headers.'User-Agent' = 'ygor'
            req.getParams().setParameter("http.socket.timeout", new Integer(5000))
            
            MultipartEntity entity = new MultipartEntity()
            entity.addPart("file", new FileBody(result))
            entity.addPart("info", new StringBody("greetings from ygor"))
            req.setEntity(entity)
            
            response.success = { resp, html ->
                println "server response: ${resp.statusLine}"
                println "server:          ${resp.headers.'Server'}"
                println "content length:  ${resp.headers.'Content-Length'}"
                if(resp.status < 400){
                    flash.warning = html
                }
                else {
                    flash.info = html
                }
            }
            response.failure = { resp ->
                println "server response: ${resp.statusLine}"
                flash.error = resp.statusLine
            }
        }

        // TODO ...
        
        process()
    }
    
    def ajaxGetStatus() {
        
        def en = getEnrichment()
        
        render '{"status":"' + en.getStatus() + '", "progress":' + en.getProgress().round() + '}'
    }

    Enrichment getEnrichment() {
        
        def hash = (String) request.parameterMap['originHash'][0]
        documents.get("${hash}")
    }

    /**
     * Return session depending directory for file upload.
     * Creates if not existing.
     */

    File getSessionFolder() {
        
        def path = grailsApplication.config.ygor.uploadLocation + File.separator + session.id
        def sessionFolder = new File(path)
        if(!sessionFolder.exists()) {
            sessionFolder.mkdirs()
        }
        sessionFolder
    }
}
