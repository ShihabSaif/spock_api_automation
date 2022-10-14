import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Specification
import spock.lang.Unroll

class CategoryApiSpec extends Specification {
    Random random = new Random()
    RESTClient restClient = new RESTClient(Constant.testURL)

    @Unroll("Create a category with #tag")
    def 'User Should be able to Create a Category'() {

        when:
        def requestBody =
                [
                        "name": name,
                        "id"  : id,
                ]

        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {

            response = restClient.post(path: '/categories',
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
            def testCategoryId = response.responseData.id
            deleteTestCategory(testCategoryId)
        }

        where:
        tag                            | name                                                                                                                                 | id    || responseStatus
        "valid data"                   | "Food"                                                                                                                               | "234" || 400 // Create category with valid data
        "invalid data length (name)"   | "A  category  with  name field data length more than 100, as data length is more than 100, this category creation should be failed." | "111" || 400 // Create category with invalid data length
        "mandatory data absent (name)" | null                                                                                                                                 | "333" || 400 // Create category without mandatory parameter

    }


    def 'User Should not be able to Create a Category with duplicate category ID'() {
        when:
        def testCategoryId = createTestCategory().responseData.id
        def requestBody =
                [
                        "name": "Arif Category",
                        "id"  : testCategoryId,
                ]

        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {

            response = restClient.post(path: '/categories',
                    body: requestBody,
                    requestContentType: 'application/json')
        } catch (HttpResponseException ex) {
            // default failure handler throws an exception:
            println "error response: ${ex.statusCode}"
            response = ex.response
        }

        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 400

        cleanup:
        deleteTestCategory(testCategoryId)
    }

    def 'User should be able to Get all Category'() {

        when:
        def response = restClient.get(path: '/categories')
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200
    }

    def 'User should be able to Get Category with specific ID'() {

        setup:
        def testCategoryId = createTestCategory().responseData.id

        when:
        def response = restClient.get(path: '/categories/' + testCategoryId)
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == 200

        and:
        testCategoryId == response.responseData.id
    }

    def 'User should be able to Get Category with TV names'() {

        when:
        def limit = 3
        def isCategoryNameContainTV = 1
        def response = restClient.get(path: '/categories', query: ['name[$like]': '*TV*'])
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        def categoryNameList = response.responseData.data.name
        println categoryNameList

        for (int i = 0; i < categoryNameList.size(); i++) {
                if (categoryNameList[i].toString().contains("TV")) {
                    println(categoryNameList[i].toString())
                } else {
                isCategoryNameContainTV = 0
                break
            }
        }
        then:
        response.status == 200

        and:
        isCategoryNameContainTV == 1
    }


    @Unroll("Update category name #tag")
    def 'User should be able to perform Update Category Name'() {
        setup:
        def testCategoryId = createTestCategory().responseData.id

        when:
        def requestBody =
                [
                        "name": name,
                        "id"  : testCategoryId
                ]


        println "request: " + new JsonBuilder(requestBody).toPrettyString()

        def response
        try {
            response = restClient.put(path: '/categories/' + testCategoryId, body: requestBody, requestContentType: 'application/json')
        } catch (HttpResponseException ex) {
            // default failure handler throws an exception:
            println "error response: ${ex.statusCode}"
            response = ex.response
        }

        then:
        response.status == responseStatus

        and:
        if (response.status in [201, 200]) {
            response.responseData.name == name
        }

        cleanup:
        if (response.status in [201, 200]) {
            testCategoryId = response.responseData.id
            deleteTestCategory(testCategoryId)
        }

        where:
        tag                     | name           || responseStatus
        "valid data for name"   | "New Category" || 200 // Update category name with valid data
        "invalid data for name" | null           || 400 // Update category with mandatory parameter null
    }


    @Unroll
    def 'User should be able to perform Delete Category'() {
        setup:
        def testCategoryId = createTestCategory().responseData.id

        when:
        def response = restClient.delete(path: '/categories/' + testCategoryId)

        then:
        response.status == 200
    }

    def createTestCategory() {
        def randomCategoryId = random.nextInt(10**3)
        def requestBody =
                [
                        "name": "string",
                        "id"  : randomCategoryId.toString(),
                ]

        return restClient.post(path: '/categories',
                body: requestBody,
                requestContentType: 'application/json')

    }

    def deleteTestCategory(def categoryId) {
        return restClient.delete(path: '/categories/' + categoryId)
    }

}
