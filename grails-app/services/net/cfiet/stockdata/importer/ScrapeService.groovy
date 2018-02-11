package net.cfiet.stockdata.importer

import com.budjb.httprequests.HttpClient
import com.budjb.httprequests.HttpResponse
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class ScrapeService {
    HttpClient httpClient

    Flowable<URI> scrapePage(URI source, Collection<String> selectors = ["a[href\$=.zip], a[href\$=.prn]"]) {
        return Flowable.using(
                { -> httpClient.get {
                    uri = source
                    followRedirects = true
                }},
                { HttpResponse response ->
                    if (response == null) return Flowable.error(new NullPointerException("Response is null"))
                    if (response.status >= 400) return Flowable.error(new Exception(
                            "Get request to $source returned status: $response.status"))

                    return Flowable.create({ FlowableEmitter<URI> emitter ->
                        try {
                            def document = Jsoup.parse(response.entity, "UTF-8", source.toString())
                            def links = document.select(selectors.join(", "))
                            for(Element link in links) {
                                if (emitter.requested() <= 0) {
                                    break
                                }
                                def href = link.attr("href")
                                def target = source.resolve(href)
                                emitter.onNext(target)
                            }
                        } catch (Exception e) {
                            emitter.onError(e)
                        } finally {
                            emitter.onComplete()
                        }
                    }, BackpressureStrategy.BUFFER)
                },
                { HttpResponse response -> response.close() }
        )
    }
}
