package net.cfiet.stockdata.importer

import grails.gorm.transactions.Rollback
import grails.testing.gorm.DomainUnitTest
import org.grails.orm.hibernate.HibernateDatastore
import org.junit.Test
import org.springframework.transaction.PlatformTransactionManager
import spock.lang.Specification

import java.time.ZonedDateTime


class FileSpec extends Specification implements DomainUnitTest<DataFile> {

    static HibernateDatastore hibernateDatastore
    PlatformTransactionManager transactionManager

    void setupSpec() {
        hibernateDatastore = new HibernateDatastore(DataFile)
    }

    void cleanupSpec() {
        hibernateDatastore.close()
    }

    void setup() {
        transactionManager = hibernateDatastore.getTransactionManager()
    }

    @Rollback
    @Test
    void "Should have a correct location"() {
        given:
        def location = "file://./2000.zip".toURI()
        def testedFile = new DataFile(location)

        expect:
        testedFile.location == location
    }

    @Rollback
    @Test
    void "Should have data ranges correctly calculated for yearly files"() {
        given:
        def testedFile = new DataFile("file://./2000.zip".toURI())

        expect:
        testedFile.dataStartsAt == ZonedDateTime.parse("2000-01-01T00:00:00Z")
        testedFile.dataEndsAt == ZonedDateTime.parse("2000-12-31T23:59:59Z")
    }

    @Rollback
    @Test
    void "Should have data ranges correctly calculated for monthly files"() {
        given:
        def testedFile = new DataFile("file://./10-2000.zip".toURI())

        expect:
        testedFile.dataStartsAt == ZonedDateTime.parse("2000-10-01T00:00:00Z")
        testedFile.dataEndsAt == ZonedDateTime.parse("2000-10-31T23:59:59Z")
    }

    @Rollback
    @Test
    void "Should have data ranges correctly calculated for daily files"() {
        given:
        def testedFile = new DataFile("file://./20001013.zip".toURI())

        expect:
        testedFile.dataStartsAt == ZonedDateTime.parse("2000-10-13T00:00:00Z")
        testedFile.dataEndsAt == ZonedDateTime.parse("2000-10-13T23:59:59Z")
    }

    @Rollback
    @Test
    void "Should have seen dates initialized"() {
        given:
        def testedFile = new DataFile("file://./2000.zip".toURI())

        expect:
        testedFile.firstSeenAt != null && testedFile.firstSeenAt <= ZonedDateTime.now()
        testedFile.lastSeenAt != null && testedFile.lastSeenAt <= ZonedDateTime.now()
    }

    @Rollback
    @Test
    void "Should correct file type when it's a local file"() {
        given:
        def testedFile = new DataFile("file://./2000.zip".toURI())

        expect:
        testedFile.fileType == FileType.Cache
    }

    @Rollback
    @Test
    void "Should be marked as active by default"() {
        given:
        def testedFile = new DataFile("file://./2000.zip".toURI())

        expect:
        testedFile.sourceStatus == SourceFileStatus.Active
    }

    @Rollback
    @Test
    void "Should be defining relations correctly"() {
        given:
        def remoteFile = new DataFile("https://example.org/2000.zip".toURI()).save(failOnError: true)
        def cachedFile = new DataFile("file://./2000.zip".toURI(),
                Optional.of(remoteFile)).save(failOnError: true)
        def dataFile = new DataFile("file://./20000101.prn".toURI(),
                Optional.of(cachedFile)).save(failOnError: true)

        expect:
        dataFile.parentFile.location == cachedFile.location &&
                dataFile.parentFile.parentFile.location == remoteFile.location &&
                dataFile.parentFile.parentFile.parentFile == null
    }
}
