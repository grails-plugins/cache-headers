package test

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@Rollback
@Integration
class BookControllerSpec extends Specification {

    def setup() {
        new Book(name: 'The Definitive Guide to Grails 2').save()
    }

    def cleanup() {
        Book.where { name == 'The Definitive Guide to Grails 2' }.deleteAll()
    }

    def "book show"() {
        given:
        RestTemplate rest = new RestTemplate()

        expect:
        Book.read(1)
        
        when:
        def resp = rest.getForEntity("http://localhost:${serverPort}/book/1", String)

        then:
        resp.statusCode.value() == 200
        resp.headers.keySet().contains('ETag')
        resp.headers.keySet().contains('Last-Modified')
        resp.headers.get('ETag') as String == '[1:0]'
        resp.headers.get('Last-Modified') != null
    }
}
