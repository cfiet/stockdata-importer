package net.cfiet.stockdata.importer

import com.budjb.httprequests.HttpClient
import com.budjb.httprequests.HttpResponse
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class ScrapeServiceSpec extends Specification implements ServiceUnitTest<ScrapeService>{
    ScrapeService testedService

    def setup() {
        testedService = new ScrapeService()
    }

    def cleanup() {
    }

    void "Scrapes yearly files"() {
        given: "Page with a yearly file"
        setupPage("""
            <body>
                <a href="2010.zip">Yearly file</a>
            </body>
        """)

        expect: "Result contain yearly file"
        testedService.scrapePage("http://example.org/".toURI())
                .singleOrError()
                .blockingGet() == "http://example.org/2010.zip".toURI()
    }

    void "Scrapes monthly files"() {
        given: "Page with a yearly file"
        setupPage("""
            <body>
                <a href="01-2010.zip">Yearly file</a>
            </body>
        """)

        expect: "Result contain yearly file"
        testedService.scrapePage("http://example.org/".toURI())
                .singleOrError()
                .blockingGet() == "http://example.org/01-2010.zip".toURI()
    }

    void "Scrapes daily files"() {
        given: "Page with a yearly file"
        setupPage("""
            <body>
                <a href="20100101.prn">Yearly file</a>
            </body>
        """)

        expect: "Result contain yearly file"
        testedService.scrapePage("http://example.org/".toURI())
                .singleOrError()
                .blockingGet() == "http://example.org/20100101.prn".toURI()
    }

    void "Ignores other links"() {
        given: "Page with a yearly file"
        setupPage("""
            <body>
                <a href="ignoreme.html">This should be ignored</a>
            </body>
        """)

        expect: "Result contain yearly file"
        testedService.scrapePage("http://example.org/".toURI())
                .isEmpty()
                .blockingGet()
    }

    private void setupPage(String content) {
        if (!content || content.isEmpty()) throw new IllegalArgumentException("Content is required")
        def entity = new ByteArrayInputStream(content.getBytes("UTF-8"))

        def response = GroovyStub(HttpResponse, {
            it.status >> 200
            it.entity >> entity
        })

        testedService.httpClient = GroovyStub(HttpClient, {
            it.get(_) >> response
        })
    }
}
