import spock.lang.Specification
import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Specification
import spock.lang.Unroll

class StoreApiSpec extends Specification {

    RESTClient restClient = new RESTClient(Constant.testURL)

    @Unroll("Cretae a store with #tag")
    def 'User should be able to create a Store'() {

        when:
        def requestBody =
                [
                        "name"    : name,
                        "type"    : type,
                        "address" : address,
                        "address2": "",
                        "city"    : city,
                        "state"   : state,
                        "zip"     : "55123",
                        "lat"     : 44.969658,
                        "lng"     : -93.449539,
                        "hours"   : "Mon: 10-9; Tue: 10-9; Wed: 10-9; Thurs: 10-9; Fri: 10-9; Sat: 10-9; Sun: 10-8"
                ]

        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {

            response = restClient.post(path: '/stores',
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

        cleanup:
        if (response.status in [201, 200]) {
            def testStoreId = response.responseData.id
            deleteTestStore(testStoreId)
        }

        where:
        tag                                  | name               | type         | address  | city     | state || responseStatus
        "valid data"                         | "Arif Books Store" | "Books Shop" | "Merul"  | "Dhaka"  | "BD"  || 201 // Create store with valid data
        "invalid data type (type)"           | "Mahmud TV"        | 111          | "Badda"  | "Ctg"    | "BD"  || 400 // Create store with invalid data type
        "mandatory data field absent (name)" | null               | "none"       | "banani" | "Khulna" | "BD"  || 400 // Create store without mandatory parameter

    }

    def 'User should be able to Get a Store with specific Store ID'() {
        setup:
        def testStoreId = createTestStore().responseData.id

        when:
        def response = restClient.get(path: '/stores/' + testStoreId)
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200

        and:
        response.responseData.id == testStoreId

        cleanup:
        deleteTestStore(testStoreId)

    }

    def 'User should be able to find a Store with in a specific State'() {
        when:
        def isStateSustain = 1
        def response = restClient.get(path: '/stores', query: ['state': 'MN'])
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        def responseStateList = response.responseData.data.state

        for (int i = 0; i < responseStateList.size(); i++) {
            if (!(responseStateList[i].toString().equalsIgnoreCase("MN"))) {
                isStateSustain = 0
                break
            }
        }

        then:
        response.status == 200

        and:
        isStateSustain == 1
    }

    @Unroll("Update Store with #tag.")
    def 'User should be able to Update a Store'() {
        setup:
        def testStoreId = createTestStore().responseData.id

        when:
        def requestBody =
                [
                        "name"   : name,
                        "type"   : type,
                        "address": address,
                        "city"   : city,
                        "state"  : state
                ]

        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {

            response = restClient.patch(path: '/stores/' + testStoreId,
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
        response.responseData.name == name
        response.responseData.city == city
        response.responseData.type == type
        response.responseData.address == address
        response.responseData.state == state


        cleanup:
        if (response.status in [201, 200]) {
            deleteTestStore(testStoreId)
        }

        where:
        tag                            | name         | type         | address | city    | state || responseStatus
        "name/type/address/city/state" | "Arif Books" | "Books Shop" | "Merul" | "Dhaka" | "BD"  || 200
    }

    @Unroll
    def 'User should be able to perform Delete Store'() {
        setup:
        def testStoreId = createTestStore().responseData.id

        when:
        def response = restClient.delete(path: '/stores/' + testStoreId)

        then:
        response.status == 200
    }


    def createTestStore() {
        def requestBody =
                [
                        "name"    : "New Store",
                        "type"    : "BigBox",
                        "address" : "123 Fake St",
                        "address2": "",
                        "city"    : "Springfield",
                        "state"   : "MN",
                        "zip"     : "55123",
                        "lat"     : 44.969658,
                        "lng"     : -93.449539,
                        "hours"   : "Mon: 10-9; Tue: 10-9; Wed: 10-9; Thurs: 10-9; Fri: 10-9; Sat: 10-9; Sun: 10-8"
                ]

        return restClient.post(path: '/stores',
                body: requestBody,
                requestContentType: 'application/json')

    }

    def deleteTestStore(def storeId) {
        return restClient.delete(path: '/stores/' + storeId)
    }

}
