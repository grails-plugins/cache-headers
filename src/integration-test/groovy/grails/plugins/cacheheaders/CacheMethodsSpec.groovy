package grails.plugins.cacheheaders

import grails.test.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.text.SimpleDateFormat

import com.grailsrocks.cacheheaders.TestController
import static org.junit.Assert.*
import grails.util.*

@Integration
class CacheMethodsSpec extends Specification {

	private static final String RFC1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz" // Always GMT


	private TestController con = new TestController()

	@Autowired
	CacheHeadersService cacheHeadersService

	void setup() {
		GrailsWebMockUtil.bindMockWebRequest()
		con.cacheHeadersService = cacheHeadersService 
	}

	void "testPresetCanTurnCachingOff"() {
		given:
		grails.util.Holders.config.cache.headers.presets.presetDeny = false
		con.presetTest1()

		expect:
		assertEquals 'no-cache, no-store', con.response.getHeader('Cache-Control')
		assertNotNull con.response.getHeader('Expires')
		assertEquals 'no-cache', con.response.getHeader('Pragma')
	}

	void "testValidUntil"() {
		given:
		con.validUntilTest1()
		def d = con.request.getAttribute('test_validUntil')

		expect:
		assertEquals d.time.toString(), con.response.getHeader('Expires')
	}

	void "testValidFor"() {
		given:
		con.validForTest1()
		def cc = con.response.getHeader('Cache-Control').tokenize(',')*.trim()
		def ma = cc.find { it.startsWith('max-age=') }

		expect:
		assertNotNull "Did not have max-age", ma
		assertEquals con.request.getAttribute('test_validFor'), (ma-'max-age=').toInteger()
	}

	void "testValidForNegative"() {
		given:
		con.validForTestNeg()
		def cc = con.response.getHeader('Cache-Control').tokenize(',')*.trim()
		def ma = cc.find { it.startsWith('max-age=') }

		expect:
		assertNotNull "Did not have max-age", ma
		assertEquals 0, (ma-'max-age=').toInteger()
	}

	void "testValidUntilNegative"() {
		given:
		con.validUntilTestNeg()
		def cc = con.response.getHeader('Cache-Control').tokenize(',')*.trim()
		def ma = cc.find { it.startsWith('max-age=') }

		expect:
		assertNotNull "Did not have max-age", ma
		assertEquals 0, (ma-'max-age=').toInteger()
	}

	void "testCombinedStoreAndShareDefault"() {
		given:
		// If we set store false, it should also default to share: private, specifying both
		con.combinedStoreAndShareDefaultTest()
		def ccParts = con.response.getHeader('Cache-Control').tokenize(',')*.trim()

		expect:
		assertTrue "Did not contain 'private'", ccParts.contains('private')
		assertTrue "Did not contain 'no-store'", ccParts.contains('no-store')
	}

	void "testWithCacheHeadersHasRequestAndResponse"() {
		given:
		con.withCacheHeadersHasRequestAndResponseTest()

		expect:
		assertNotNull con.response
		assertNotNull con.request
	}

	private String dateToHTTPDate(date) {
		def v = new SimpleDateFormat(RFC1123_DATE_FORMAT, Locale.ENGLISH)
		v.timeZone = TimeZone.getTimeZone('GMT')
		return v.format(date)
	}
}
