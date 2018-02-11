package net.cfiet.stockdata.importer

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import io.reactivex.Flowable
import spock.lang.Specification


class DataFileServiceSpec extends Specification implements ServiceUnitTest<DataFileService>, DataTest {
    DataFileService testedService

    def setup() {
        testedService = new DataFileService()
    }

    def cleanup() {
    }

    void "has default uriMatcher"() {
        expect:
        testedService.uriMatcher != null && !testedService.uriMatcher.empty
    }

    void "matches yearly files"() {
        given: "yearly file URI"
        setupLinks("https://example.org/2010.zip")

        expect: "yearly file entity"
        with(testedService.scrapePage("http://example.org".toURI())
            .singleOrError()
            .blockingGet()) {
            location == "https://example.org/2010.zip".toURI()
        }
    }

    void "matches monthly files"() {
        given: "yearly file URI"
        setupLinks("https://example.org/01-2010.zip")

        expect: "yearly file entity"
        with(testedService.scrapePage("http://example.org".toURI())
                .singleOrError()
                .blockingGet()) {
            location == "https://example.org/01-2010.zip".toURI()
        }
    }

    void "matches daily files"() {
        given: "yearly file URI"
        setupLinks("https://example.org/20100101.prn")

        expect: "yearly file entity"
        with(testedService.scrapePage("http://example.org".toURI())
                .singleOrError()
                .blockingGet()) {
            location == "https://example.org/20100101.prn".toURI()
        }
    }

    void "ignores other common files"() {
        given: "ignored files"
        setupLinks(
                "https://example.org/readme.txt",
                "https://example.org/index.html",
        )

        expect: "result is empty"
        testedService.scrapePage("http://example.org".toURI())
            .toList()
            .blockingGet() == []
    }

    private void setupLinks(String... uris) {
        testedService.scraperService = Stub(ScrapeService, {
            scrapePage(_) >> Flowable.fromArray(uris).map({ URI.create(it) })
        })
    }
}
