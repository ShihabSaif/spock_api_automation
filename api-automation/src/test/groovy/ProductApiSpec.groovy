/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class ProductApiSpec extends Specification {


    RESTClient restClient = new RESTClient(Constant.testURL)


    @Unroll("Create Product with #tag.")
    def 'Create Product with all valid data/with invalid data type/without mandatory data field)'() {

        when:
        def requestBody =
                [
                        "name"        : name,
                        "type"        : type,
                        "price"       : price,
                        "shipping"    : 0,
                        "upc"         : "string",
                        "description" : "string",
                        "manufacturer": "string",
                        "model"       : "string",
                        "url"         : "string",
                        "image"       : "string"
                ]

        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {

            response = restClient.post(path: '/products',
                    body: requestBody,
                    requestContentType: 'application/json')
        } catch (HttpResponseException ex) {
            response = ex.response
        }

        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == responseStatus

        cleanup:
        if (response.status in [201, 200]) {
            def testProductId = response.responseData.id
            deleteTestProduct(testProductId)
        }

        where:
        tag                                   | name                                                                                                                            | price | type    || responseStatus
        "valid data (name)"                   | "Oil"                                                                                                                           | 123   | "good"  || 201 // with mandatory parameter
        "without mandatory data field (type)" | "Rice"                                                                                                                          | 123   | null    || 400 // without mandatory parameter
        "with invalid data type (name)"       | 1111                                                                                                                            | 120   | "best"  || 400 // with illegal data type
        "invalid data length (name)"          | "A New Product with a name of more than 100 chars and which price is zero and free shipping when product should not be created" | 200   | "worst" || 400 // with invalid data length
    }

    //TODO: Unroll with contextual variables
    @Unroll
    def 'User should be able to Get a Product with specific Product ID'() {
        setup:
        def testProductId = createTestProduct().responseData.id

        when:
        def response = restClient.get(path: '/products/' + testProductId)
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200

        and:
        response.responseData.id
        response.responseData.type
        response.responseData.price == 1235


        cleanup:
        deleteTestProduct(testProductId)

    }

    // TODO: default limit is 10
    // TODO: specified limit works
    // TODO: check total value
    // TODO: ascending descending

    def 'User should be able to get products in sorted order'() {


        setup:
        def limit = 12
        def testProducIdList = []

        limit.times { testProducIdList << createTestProduct().responseData.id }

        when:
        def response = restClient.get(
                path: '/products',
                query: [
                        '$sort[price]': '-1',
                        '$limit'      : limit,
                ]
        )

        def priceList = []
        response.responseData.data.each { it -> priceList << it.price }

        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200

        and:
        response.responseData.data
        response.responseData.limit == limit
        response.responseData.data.size == limit
        priceList.size == limit
        priceList == priceList.sort(true)

        cleanup:
        testProducIdList.each {
            deleteTestProduct(it)
        }

    }

    def 'Number of total product should work properly'() {


        setup:
        def limit = 12

        def response = restClient.get(path: '/products')
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()
        def currentTotalProduct = response.responseData.total

        //creating some test products to chek whether the total value is increased or not
        def testProducIdList = []
        limit.times { testProducIdList << createTestProduct().responseData.id }

        when:
        response = restClient.get(path: '/products')
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        def totalProductAfterCreation = response.responseData.total

        testProducIdList.each {
            deleteTestProduct(it)
        }

        response = restClient.get(path: '/products')
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        def totalProductAfterDeletion = response.responseData.total

        then:
        response.status == 200

        and:
        totalProductAfterDeletion == currentTotalProduct
        currentTotalProduct + limit == totalProductAfterCreation
    }


    def 'User should be able to get a limit number of products'() {


        setup:
        def limit = 5

        when:
        def response = restClient.get(
                path: '/products',
                query: [
                        '$limit': limit
                ]
        )

        //println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200

        and:
        response.responseData.data
        response.responseData.limit == limit
        response.responseData.data.size == limit
    }

    def 'User should be able to get products with free shipping'() {

        setup:
        def limit = 5
        def isShippingZero = 1

        when:
        def response = restClient.get(
                path: '/products',
                query: [
                        'shipping[$eq]': 0,
                        '$limit'       : limit,
                ]
        )

        def shippingPriceList = []
        response.responseData.data.each { it -> shippingPriceList << it.shipping }

        for (int i = 0; i < shippingPriceList.size(); i++) {
            if (shippingPriceList[i] != 0) {
                isShippingZero = 0
                break
            }
        }

        then:
        response.status == 200

        and:
        response.responseData.data
        response.responseData.limit == limit
        response.responseData.data.size == limit
        shippingPriceList.size == limit
        isShippingZero == 1

    }

    def 'User should be able to get products within a specific price range'() {

        setup:
        def limit = 5
        def lowestPrice = 500
        def highestPrice = 1000
        def isPriceRangeSustain = 1


        when:
        def response = restClient.get(
                path: '/products',
                query: [
                        'price[$gt]': 0,
                        'price[$lt]': 800,
                        '$limit'    : limit,
                ]
        )

        def priceList = []
        response.responseData.data.each { it -> priceList << it.price }

        for (int i = 0; i < priceList.size(); i++) {
            if (priceList[i] >= lowestPrice && priceList[i] <= highestPrice) {
                isPriceRangeSustain = 0
                break
            }
        }

        println priceList

        //println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200

        and:
        response.responseData.data
        response.responseData.limit == limit
        response.responseData.data.size == limit
        priceList.size == limit
        isPriceRangeSustain == 1

    }


    @Unroll("Update a product with #tag")
    def 'User should be able to perform Update Product Request'() {
        setup:
        def testProductId = createTestProduct().responseData.id

        when:
        def requestBody =
                [
                        "name"        : "string2",
                        "type"        : type,
                        "price"       : price,
                        "shipping"    : 0,
                        "upc"         : "string",
                        "description" : "string",
                        "manufacturer": "string",
                        "model"       : "string",
                        "url"         : "string",
                        "image"       : "string"
                ]


        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {
            response = restClient.put(path: '/products/' + testProductId, body: requestBody, requestContentType: 'application/json')
        } catch (HttpResponseException ex) {
            // default failure handler throws an exception:
            response = ex.response
        }
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == responseStatus

        and:
        if (response.status in [201, 200]) {
            response.responseData.price == price
        }

        cleanup:
        if (response.status in [201, 200]) {
            testProductId = response.responseData.id
            deleteTestProduct(testProductId)
        }

        where:
        tag                             | price | type   || responseStatus
        "valid data for type and price" | 200   | "good" || 200 // Update product type and price with valid data
        "invalid data for type"         | 420   | null   || 400 // Update product type and price without mandatory parameter null
    }

    def 'User should be able to perform Delete Request'() {
        setup:
        def testProductId = createTestProduct().responseData.id

        when:
        def response = restClient.delete(path: '/products/' + testProductId)

        then:
        response.status == 200
    }


    def createTestProduct() {
        def requestBody =
                [
                        "name"        : "string",
                        "type"        : "string",
                        "price"       : 1235,
                        "shipping"    : 0,
                        "upc"         : "string",
                        "description" : "string",
                        "manufacturer": "string",
                        "model"       : "string",
                        "url"         : "string",
                        "image"       : "string"
                ]

        return restClient.post(path: '/products',
                body: requestBody,
                requestContentType: 'application/json')

    }

    def deleteTestProduct(def productId) {
        return restClient.delete(path: '/products/' + productId)
    }

}