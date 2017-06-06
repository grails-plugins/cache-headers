package grails.plugins.cacheheaders

import grails.plugins.Plugin
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class CacheHeadersGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.2.10 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "**/TestController**",
            "src/docs/**",
    ]

    def profiles = ['web']
    def author = 'Graeme Rocher'
    def authorEmail = 'graeme.rocher@gmail.com'
    def title = 'Caching Headers Plugin'
    def description = 'Improve your application performance with browser caching, with easy ways to set caching headers in controller responses'
    def developers = [[name: "Marc Palmer", email: "marc@grailsrocks.com"], [name: "Graeme Rocher", email: "graeme.rocher@gmail.com"], [name: 'Sergio del Amo', email: 'sergio.delamo@softamo.com']]
    def issueManagement = [system: "Github", url: "http://github.com/grails-plugins/grails-cache-headers/issues"]
    def scm = [url: 'http://github.com/grails-plugins/grails-cache-headers']
    def license = "APACHE"
    def documentation = "http://grails.org/plugin/cache-headers"

    void doWithApplicationContext() {
        CacheHeadersService cacheHeadersService = applicationContext.getBean('cacheHeadersService', CacheHeadersService)
        cacheHeadersService.enabled = config.getProperty('cache.headers.enabled', Boolean, true)
        cacheHeadersService.presets = getPresets(config)
        log.info "Caching enabled in Config: ${cacheHeadersService.enabled}"
        log.debug "Caching presets declared: ${cacheHeadersService.presets}"
    }

    @CompileDynamic
    static Map getPresets(config) {
        config.cache.headers.presets
    }

    void onConfigChange(Map<String, Object> event) {
        // Config change might mean that the caching has been turned on/off
        doWithApplicationContext()
    }
}