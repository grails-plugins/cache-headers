package grails.plugins.cacheheaders

import grails.web.api.*
import groovy.transform.*

@CompileStatic
trait CacheHeadersTrait extends ServletAttributes {

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