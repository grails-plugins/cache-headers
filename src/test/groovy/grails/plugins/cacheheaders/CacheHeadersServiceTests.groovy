package grails.plugins.cacheheaders

import junit.framework.TestCase

import java.text.SimpleDateFormat

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

import grails.test.mixin.*
import static org.junit.Assert.*

@TestFor(CacheHeadersService)
class CacheHeadersServiceTests extends TestCase  {

	private MockHttpServletRequest req = new MockHttpServletRequest()
	private MockHttpServletResponse resp = new MockHttpServletResponse()
	private CacheHeadersService svc = new CacheHeadersService()
	private Expando context = new Expando(
		request: req,
		response: resp,
		render: { String s -> resp.outputStream << s.bytes })


	private static final String RFC1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz" // Always GMT

	void testWithCacheHeadersCachingDisabled() {
		svc.enabled = false

		req.addHeader('If-None-Match', "1234567Z")

		def res = svc.withCacheHeaders(context) {
			etag {
				"1234567Z"
			}
			generate {
				render "Hello!"
			}
		}

		assertEquals 200, resp.status
		assertEquals "Hello!", resp.contentAsString
		assertNull resp.getHeader('Last-Modified')
		assertNull resp.getHeader('ETag')
	}

	void testWithCacheHeadersETagMatch() {
		req.addHeader('If-None-Match', "1234567Z")

		def res = svc.withCacheHeaders(context) {
			etag {
				"1234567Z"
			}
		}

		assertEquals 304, resp.status
		assertNull resp.getHeader('Last-Modified')
		assertNull resp.getHeader('ETag')
	}

	void testWithCacheHeadersETagNoMatchLastModUnchanged() {
		def lastMod = new Date() - 100

		req.addHeader('If-None-Match', "dsfdsfdsfdsfsd")
		// This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
		req.addHeader('If-Modified-Since', lastMod)

		def res = svc.withCacheHeaders(context) {
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

		assertEquals 200, resp.status
		assertEquals "Derelict Herds", resp.contentAsString
		assertEquals lastMod.time.toString(), resp.getHeader('Last-Modified')
		assertEquals "1234567Z", resp.getHeader('ETag')
	}

	void testWithCacheHeadersETagMatchLastModChanged() {
		def lastMod = new Date() - 100

		req.addHeader('If-None-Match', "bingo")
		// This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
		req.addHeader('If-Modified-Since', lastMod-1)

		def res = svc.withCacheHeaders(context) {
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

		assertEquals 200, resp.status
		assertEquals "Derelict Herds", resp.contentAsString
		assertEquals lastMod.time.toString(), resp.getHeader('Last-Modified')
		assertEquals "bingo", resp.getHeader('ETag')
	}

	void testWithCacheHeadersETagMatchLastModUnchanged() {
		def lastMod = new Date() - 100

		req.addHeader('If-None-Match', "bingo")
		// This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
		req.addHeader('If-Modified-Since', lastMod)

		def res = svc.withCacheHeaders(context) {
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

		assertEquals 304, resp.status
	}

	void testWithCacheHeadersETagNoMatchLastModChanged() {
		def lastMod = new Date() - 100

		req.addHeader('If-None-Match', "dsfdsfdsfdsfsd")
		// This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
		req.addHeader('If-Modified-Since', lastMod-1)

		def res = svc.withCacheHeaders(context) {
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

		assertEquals 200, resp.status
		assertEquals "Derelict Herds", resp.contentAsString
		assertEquals lastMod.time.toString(), resp.getHeader('Last-Modified')
		assertEquals "1234567Z", resp.getHeader('ETag')
	}

	void testWithCacheHeadersLastModChanged() {
		// This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
		req.addHeader('If-Modified-Since', new Date() - 102)

		def lastMod = new Date() - 100

		def res = svc.withCacheHeaders(context) {
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

		assertEquals 200, resp.status
		assertEquals "Porcelain Heart", resp.contentAsString
		assertEquals lastMod.time.toString(), resp.getHeader('Last-Modified')
		assertEquals "OU812", resp.getHeader('ETag')
	}

	void testWithCacheHeadersLastModNotNewer() {
		def d = new Date() - 100
		// This is an AWFUL hack because spring mock http request/response does not do string <-> date coercion
		req.addHeader('If-Modified-Since', d)

		def lastMod = d

		def res = svc.withCacheHeaders(context) {
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

		assertEquals 304, resp.status
		assertNull resp.getHeader('Last-Modified')
		assertNull resp.getHeader('ETag')
	}

	private String dateToHTTPDate(date) {
		def v = new SimpleDateFormat(RFC1123_DATE_FORMAT, Locale.ENGLISH)
		v.timeZone = TimeZone.getTimeZone('GMT')
		return v.format(date)
	}
}
