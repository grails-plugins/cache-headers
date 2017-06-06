package grails.plugins.cacheheaders

import grails.artefact.Enhances
import grails.web.api.ServletAttributes
import groovy.transform.CompileStatic
import org.grails.core.artefact.ControllerArtefactHandler
import org.springframework.beans.factory.annotation.Autowired

@Enhances(ControllerArtefactHandler.TYPE)
@CompileStatic
trait CacheHeadersTrait extends ServletAttributes {

    @Autowired
    CacheHeadersService cacheHeadersService


    void cache( boolean allow ) {
        cacheHeadersService.cache(response, allow)
    }

    void cache( String preset ){
        cacheHeadersService.cache(response, preset)
    }

    void cache( Map args ) {
        cacheHeadersService.cache(response, args)
    }
    void withCacheHeaders( Closure c) {
        cacheHeadersService.withCacheHeaders(response, c)
    }

    void lastModified( dateOrLong ){
        cacheHeadersService.lastModified(response, dateOrLong)
    }
}