package net.cfiet.stockdata.importer

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.gorm.transactions.Transactional
import io.reactivex.Flowable

@Transactional
class DataFileService implements GrailsConfigurationAware {
    static String DefaultUriMatcher = '.*/((([0-9]{2}-)?[0-9]{4}\\.zip)|([0-9]{8}\\.prn))$'

    ScrapeService scraperService
    String uriMatcher = DefaultUriMatcher

    Flowable<DataFile> scrapePage(URI source) {
        return scraperService.scrapePage(source)
            .filter{
                it.toString().matches(uriMatcher)
            }
            .map{
                new DataFile(it)
            }
    }

    @Override
    void setConfiguration(Config co) {
        uriMatcher = co.getProperty("net.cfiet.stockdata.importer.uriMatcher", DefaultUriMatcher)
    }
}
