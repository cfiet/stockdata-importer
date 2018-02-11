package net.cfiet.stockdata.importer

class UrlMappings {

    static mappings = {
        "/files"(resources: "datafile", includes: ['index', 'show'])
        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
