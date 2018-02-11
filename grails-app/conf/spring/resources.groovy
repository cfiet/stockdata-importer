import com.budjb.httprequests.jersey2.JerseyHttpClientFactory

// Place your Spring DSL code here
beans = {
    httpClientFactory(JerseyHttpClientFactory)
    httpClient(httpClientFactory: "createHttpClient")
}
