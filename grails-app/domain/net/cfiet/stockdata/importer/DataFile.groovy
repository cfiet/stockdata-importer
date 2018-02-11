package net.cfiet.stockdata.importer

import grails.compiler.GrailsCompileStatic
import grails.rest.Resource
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

@Resource(readOnly = false, formats = ['json', 'xml'])
@GrailsCompileStatic
@ToString(includes = ["id", "location"])
class DataFile implements GormEntity<DataFile> {
    DataFile(
            URI location,
            Optional<DataFile> parentFile = Optional.empty(),
            Optional<ZonedDateTime> firstSeenAt = Optional.empty(),
            Optional<ZonedDateTime> lastSeenAt = Optional.empty(),
            SourceFileStatus sourceFileStatus = SourceFileStatus.Active
    ) {
        if (location == null) throw new IllegalArgumentException("Location is required")

        this.location = location
        //this.parentUri = parentUri.map{ URI u -> u.toString() }.orElse(null)
        this.parentFile = parentFile.orElse(null)

        this.firstSeenAt = firstSeenAt.orElse(ZonedDateTime.now(ZoneOffset.UTC))
        this.lastSeenAt = lastSeenAt.orElse(ZonedDateTime.now(ZoneOffset.UTC))

        def fileRange = getFileRange(location)
        this.dataStartsAt = fileRange.map { Tuple2<ZonedDateTime, ZonedDateTime> o -> o.getFirst() }.orElse(null)
        this.dataEndsAt = fileRange.map { Tuple2<ZonedDateTime, ZonedDateTime> o -> o.getSecond() }.orElse(null)

        this.fileType = getDataFileType(location)
        this.sourceStatus = sourceFileStatus
    }

    Long id

    URI location
    ZonedDateTime firstSeenAt
    ZonedDateTime lastSeenAt

    ZonedDateTime dataStartsAt
    ZonedDateTime dataEndsAt

    FileType fileType
    SourceFileStatus sourceStatus

    DataFile parentFile

    static belongsTo = [parentFile: DataFile]

    static constraints = {
        location(nullable: false, blank: false, unique: true)
        parentFile(nullable: true)
    }

    static mapping = {
    }

    static FileType getDataFileType(URI location) {
        if (location.scheme?.startsWith("http")) {
            return FileType.Remote
        }

        if (location.path?.endsWith(".zip")) {
            return FileType.Cache
        }

        if (location.path?.endsWith(".prn")) {
            return FileType.Data
        }

        return FileType.Unknown
    }

    static Optional<Tuple2<Date, Date>> getFileRange(URI location) {

        def fileName = location.toString().split("/")?.last()
        def yearMatch = (fileName =~ /([0-9]{4})\.\w+$/)
        if (yearMatch.matches()) {
            return Optional.of(new Tuple2(
                ZonedDateTime.parse("${yearMatch.group(1)}-01-01T00:00:00Z"),
                ZonedDateTime.parse("${yearMatch.group(1)}-12-31T23:59:59Z")
            ))
        }
        def yearMonthMatch = (fileName =~ /([0-9]{2})-([0-9]{4})\.\w+$/)
        if (yearMonthMatch.matches()) {
            def startDate = ZonedDateTime.parse("${yearMonthMatch.group(2)}-${yearMonthMatch.group(1)}-01T00:00:00Z")
            return Optional.of(new Tuple2(
                startDate,
                startDate.with(TemporalAdjusters.lastDayOfMonth())
                        .withHour(23)
                        .withMinute(59)
                        .withSecond(59)
            ))
        }

        def yearMonthDay = (fileName =~ /([0-9]{4})([0-9]{2})([0-9]{2})\.\w+/)
        if (yearMonthDay.matches()) {
            return Optional.of(new Tuple2(
                ZonedDateTime.parse("${yearMonthDay.group(1)}-${yearMonthDay.group(2)}-${yearMonthDay.group(3)}T00:00:00Z"),
                ZonedDateTime.parse("${yearMonthDay.group(1)}-${yearMonthDay.group(2)}-${yearMonthDay.group(3)}T23:59:59Z")
            ))
        }

        return Optional.empty()
    }
}
