import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Specification
import spock.lang.Unroll

class ServiceApiSpec extends Specification {

    RESTClient restClient = new RESTClient(Constant.testURL)

    @Unroll("Create a service with #tag")
    def 'User Should be able to Create a Service'() {

        when:
        def requestBody =
                [
                        "name": name
                ]

        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {

            response = restClient.post(path: '/services',
                    body: requestBody,
                    requestContentType: 'application/json')
        } catch (HttpResponseException ex) {
            // default failure handler throws an exception:
            println "error response: ${ex.statusCode}"
            response = ex.response
        }

        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == responseStatus

        and:
        if (response.status in [201, 200]) {
            response.responseData.name == name
        }

        cleanup:
        if (response.status in [201, 200]) {
            def testServiceId = response.responseData.id
            deleteTestService(testServiceId)
        }

        where:
        tag                            | name                                                                                                                       || responseStatus
        "valid data (name)"            | "Food Service"                                                                                                             || 201 // Create service with valid data
        "invalid data length (name)"   | "A service with long name and the name field length should be not more than 100, so the service creation should be failed" || 400 // Create category with invalid data length
        "mandatory data absent (name)" | null                                                                                                                       || 400 // Create service without mandatory parameter

    }

    @Unroll
    def 'User should be able to Get a Service with specific Service ID'() {
        setup:
        def testServiceId = createTestService().responseData.id

        when:
        def response = restClient.get(path: '/services/' + testServiceId)
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200

        cleanup:
        deleteTestService(testServiceId)

    }


    @Unroll("Update a service with #tag")
    def 'User should be able to perform Update Service Request'() {
        setup:
        def testServiceId = createTestService().responseData.id

        when:
        def requestBody =
                [
                        "name": name
                ]


        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {
            response = restClient.put(path: '/services/' + testServiceId, body: requestBody, requestContentType: 'application/json')
        } catch (HttpResponseException ex) {
            // default failure handler throws an exception:
            println "error response: ${ex.statusCode}"
            response = ex.response
        }
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == responseStatus

        and:
        if (response.status in [201, 200]) {
            response.responseData.name == name
        }

        cleanup:
        if (response.status in [201, 200]) {
            testServiceId = response.responseData.id
            deleteTestService(testServiceId)
        }

        where:
        tag                            | name          || responseStatus
        "valid data (name)"            | "Best Servie" || 200 // Update service name with valid data
        "mandatory data absent (name)" | null          || 400 // Update service name without mandatory parameter null
    }

    @Unroll
    def 'User should be able to perform Delete Service Request'() {
        setup:
        def testServiceId = createTestService().responseData.id

        when:
        def response = restClient.delete(path: '/services/' + testServiceId)

        then:
        response.status == 200
    }


    def createTestService() {
        def requestBody =
                [
                        "name": "string"
                ]

        return restClient.post(path: '/services',
                body: requestBody,
                requestContentType: 'application/json')

    }

    def deleteTestService(def serviceId) {
        return restClient.delete(path: '/services/' + serviceId)
    }
}
