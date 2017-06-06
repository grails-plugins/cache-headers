package cacheheaders

import com.grailsrocks.cacheheaders.TestController
import grails.plugins.cacheheaders.CacheHeadersService
import spock.lang.Specification
import grails.util.GrailsWebMockUtil
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import grails.test.mixin.integration.Integration
import spock.lang.Subject

import java.text.SimpleDateFormat

@Integration
class CacheMethodsSpec extends Specification {
    private static final String RFC1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz" // Always GMT

    @Subject
    @Shared TestController testController = new TestController()

    @Autowired
    CacheHeadersService cacheHeadersService

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest()
        testController.cacheHeadersService = cacheHeadersService
    }

    def "preset can turn caching off"() {
        given:
        grails.util.Holders.config.cache.headers.presets.presetDeny = false

        when:
        testController.presetTest1()

        then:
        testController.response.getHeader('Cache-Control') == 'no-cache, no-store'
        testController.response.getHeader('Expires') != null
        testController.response.getHeader('Pragma') == 'no-cache'
    }

    def "validUnit sets expires header"() {
        when:
        testController.validUntilTest1()

        then:
        testController.response.getHeader('Expires') == dateToHTTPDate(testController.request.getAttribute('test_validUntil'))
    }

    def "validFor sets max-hage"() {
        when:
        testController.validForTest1()
        def cc = testController.response.getHeader('Cache-Control').tokenize(',')*.trim()
        def ma = cc.find { it.startsWith('max-age=') }

        then: 'max-age exists'
        ma

        and:
        (ma-'max-age=').toInteger() == testController.request.getAttribute('test_validFor')
    }

    def "validFor negative"() {
        when:
        testController.validForTestNeg()
        def cc = testController.response.getHeader('Cache-Control').tokenize(',')*.trim()
        def ma = cc.find { it.startsWith('max-age=') }

        then: 'max-age is set'
        ma

        and:
        (ma-'max-age=').toInteger() == 0
    }

    def "validUntil negative"() {
        when:
        testController.validForTestNeg()
        def cc = testController.response.getHeader('Cache-Control').tokenize(',')*.trim()
        def ma = cc.find { it.startsWith('max-age=') }

        then:
        ma

        and:
        (ma-'max-age=').toInteger() == 0
    }

    def "testCombinedStoreAndShareDefault" () {
        when:
        // If we set store false, it should also default to share: private, specifying both
        testController.combinedStoreAndShareDefaultTest()
        def ccParts = testController.response.getHeader('Cache-Control').tokenize(',')*.trim()

        then:
        ccParts.contains('private')
        ccParts.contains('no-store')
    }

    private static String dateToHTTPDate(date) {
        def v = new SimpleDateFormat(RFC1123_DATE_FORMAT, Locale.ENGLISH)
        v.timeZone = TimeZone.getTimeZone('GMT')
        return v.format(date)
    }
}
