package grails.plugins.cacheheaders

import org.springframework.beans.factory.annotation.*
import grails.web.api.*
import groovy.transform.*

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
		cacheHeadersService.withCacheHeaders([ response: response, request: request ], c)
	}

	void lastModified( dateOrLong ){ 
		cacheHeadersService.lastModified(response, dateOrLong) 
	}	
}