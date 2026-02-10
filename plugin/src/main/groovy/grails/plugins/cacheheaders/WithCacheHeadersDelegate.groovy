package grails.plugins.cacheheaders

import groovy.transform.CompileStatic

@CompileStatic
class WithCacheHeadersDelegate {
    Closure etagDSL
    Closure lastModDSL
    Closure generateDSL

    void etag(Closure c) {
        etagDSL = c
    }

    void lastModified(Closure c) {
        lastModDSL = c
    }

    void generate(Closure c) {
        generateDSL = c
    }
}
