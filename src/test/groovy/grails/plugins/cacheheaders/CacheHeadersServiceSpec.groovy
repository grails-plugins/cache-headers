package grails.plugins.cacheheaders

import grails.test.mixin.TestFor
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

import java.text.SimpleDateFormat

@TestFor(CacheHeadersService)
class CacheHeadersServiceSpec extends Specification {

    MockHttpServletRequest req
    MockHttpServletResponse resp
    Expando context

    private static final String RFC1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz" // Always GMT

    void setup() {
        req = new MockHttpServletRequest()
        resp = new MockHttpServletResponse()
        context = new Expando(
                request: req,
                response: resp,
                render: { String s -> resp.outputStream << s.bytes })
    }

    def "withCacheHeaders does not set Last-Modified or ETag if caching is disabled"() {
        given:
        service.enabled = false

        when:
        req.addHeader('If-None-Match', "1234567Z")

        def res = service.withCacheHeaders(context) {
            etag {
                "1234567Z"
            }
            generate {
                render "Hello!"
            }
        }

        then:
        resp.status == 200
        resp.contentAsString == 'Hello!'
        resp.getHeader('Last-Modified') == null
        resp.getHeader('ETag') == null
    }

    def "request where Header If-None-Match matches ETag"() {
        when:
        req.addHeader('If-None-Match', "1234567Z")

        def res = service.withCacheHeaders(context) {
            etag {
                "1234567Z"
            }
        }

        then:
        resp.status == 304 // Not Modified
        resp.getHeader('Last-Modified') == null
        resp.getHeader('ETag') == null
    }

    def "withCacheHeaders ETag does not match last modfied unchanged"() {

        given:
        def lastMod = new Date() - 100

        when:
        req.addHeader('If-None-Match', "dsfdsfdsfdsfsd")
        // This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
        req.addHeader('If-Modified-Since', lastMod)

        def res = service.withCacheHeaders(context) {
            etag {
                "1234567Z"
            }

            lastModified {
                lastMod
            }

            generate {
                render "Derelict Herds"
            }
        }

        then:
        resp.status == 200
        resp.contentAsString == 'Derelict Herds'
        resp.getHeader('ETag') == '1234567Z'
        resp.getHeader('Last-Modified') == dateToHTTPDate(lastMod)

    }

    void "withCacheHeaders ETag match Last Modified Changed"() {
        given:
        def lastMod = new Date() - 100

        when:
        req.addHeader('If-None-Match', "bingo")
        // This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
        req.addHeader('If-Modified-Since', lastMod-1)

        def res = service.withCacheHeaders(context) {
            etag {
                "bingo"
            }

            lastModified {
                lastMod
            }

            generate {
                render "Derelict Herds"
            }
        }

        then:
        resp.status == 200
        resp.contentAsString == 'Derelict Herds'
        resp.getHeader('ETag') == 'bingo'
        resp.getHeader('Last-Modified') == dateToHTTPDate(lastMod)
    }

    void "withCacheHeaders ETag match, last Modified unchanged"() {
        given:
        def lastMod = new Date() - 100

        when:
        req.addHeader('If-None-Match', "bingo")
        // This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
        req.addHeader('If-Modified-Since', lastMod)

        def res = service.withCacheHeaders(context) {
            etag {
                "bingo"
            }

            lastModified {
                lastMod
            }

            generate {
                render "Derelict Herds"
            }
        }

        then:
        resp.status == 304 // Not Modified
    }


    def "withCacheHeaders ETag No Match, LastModChanged"() {
        given:
        def lastMod = new Date() - 100

        when:
        req.addHeader('If-None-Match', "dsfdsfdsfdsfsd")
        // This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
        req.addHeader('If-Modified-Since', lastMod-1)

        def res = service.withCacheHeaders(context) {
            etag {
                "1234567Z"
            }

            lastModified {
                lastMod
            }

            generate {
                render "Derelict Herds"
            }
        }

        then:
        resp.status == 200
        resp.contentAsString == 'Derelict Herds'
        resp.getHeader('ETag') == '1234567Z'
        resp.getHeader('Last-Modified') == dateToHTTPDate(lastMod)
    }

    def "testWithCacheHeadersLastModChanged"() {
        when:
        // This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
        req.addHeader('If-Modified-Since', new Date() - 102)

        def lastMod = new Date() - 100

        def res = service.withCacheHeaders(context) {
            etag {
                "OU812"
            }
            lastModified {
                lastMod
            }

            generate {
                render "Porcelain Heart"
            }
        }

        then:
        resp.status == 200
        resp.contentAsString == 'Porcelain Heart'
        resp.getHeader('ETag') == 'OU812'
        resp.getHeader('Last-Modified') == dateToHTTPDate(lastMod)
    }

    def "testWithCacheHeadersLastModNotNewer"() {
        given:
        def d = new Date() - 100

        when:
        // This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
        req.addHeader('If-Modified-Since', d)
        def lastMod = d
        def res = service.withCacheHeaders(context) {
            etag {
                "5150"
            }
            lastModified {
                lastMod
            }

            generate {
                render "Hessian Peel"
            }
        }

        then:
        resp.status == 304 // Not Modified
        resp.getHeader('Last-Modified') == null
        resp.getHeader('ETag') == null
    }

    private static String dateToHTTPDate(date) {
        def v = new SimpleDateFormat(RFC1123_DATE_FORMAT, Locale.ENGLISH)
        v.timeZone = TimeZone.getTimeZone('GMT')
        return v.format(date)
    }
}
