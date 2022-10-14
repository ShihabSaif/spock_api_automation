import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import net.sf.json.JSONObject
import net.sf.json.util.JSONBuilder
import spock.lang.Specification
import groovy.sql.Sql

class testApiSpec extends Specification{
    static String baseURL = "https://stgqa.tallykhata.com"

    RESTClient restClient = new RESTClient(baseURL)

    def 'send payment link sms by v3/sms'(){
        def dbUrl      = "jdbc:postgresql://localhost:5432/postgres"
        def dbUser     = "test"
        def dbPassword = "test"
        def dbDriver   = "org.postgresql.Driver"

        def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)
        given:
        def requestBody = ["account_type": 2,
                           "business_address": "",
                           "business_name": "Airtel store",
                           "collection": "0.00",
                           "customer_account_id": 25,
                           "customer_mobile_no": "01621215877",
                           "device_uuid": "2350227c-70a6-42c8-8db6-03d5cbf97e2c",
                           "merchant_mobile_no": "01621215877",
                           "previous_credit": "700.00",
                           "sale": "500.00",
                           "total_credit": "1,200.00",
                           "version_code": 110]

        when:
        restClient.headers['Authorization'] = "Basic cHJvZ290aV9xYTpwcjBnMHQxQDIwMnR3bw=="
        restClient.headers['x-auth-token'] = "05pmN9hrpwf6QDYhC2NSa0QrcPqtwS2TYjQYKJhV"
        def response = restClient.post(path:'/api/notification/v3/sms', body: requestBody, requestContentType: 'application/json')

        then:
        response.status == 200
    }

    def 'payment link sms'() {

        given:
        def requestBody =
                [
                           "account_type": account_type,
                           "business_address": business_address,
                           "business_name": business_name,
                           "collection": collection,
                           "customer_account_id": customer_account_id,
                           "customer_mobile_no": customer_mobile_no,
                           "device_uuid": device_uuid,
                           "merchant_mobile_no": merchant_mobile_no,
                           "previous_credit": previous_credit,
                           "sale": sale,
                           "total_credit": total_credit,
                           "version_code": version_code
                ]

        when:
        restClient.headers['Authorization'] = "Basic cHJvZ290aV9xYTpwcjBnMHQxQDIwMnR3bw=="
        restClient.headers['x-auth-token'] = "05pmN9hrpwf6QDYhC2NSa0QrcPqtwS2TYjQYKJhV"
        def response
        try {
            response = restClient.post(path:'/api/notification/v3/sms', body: requestBody, requestContentType: 'application/json')
        } catch (HttpResponseException ex)
        {
            response = ex.response
        }
        println "response: " + new JsonBuilder(response.responseData).toPrettyString()

        then:
        response.status == responseStatus


        where:
        account_type | business_address  | business_name  | collection | customer_account_id | customer_mobile_no | device_uuid                            | merchant_mobile_no | previous_credit | sale | total_credit | version_code || responseStatus
        2            | ""                | "Airtel store" | "0.00"     | 25                  | "01621215877"      | "d867025b-d813-4f51-947b-a0a8ea179d68" | "01621215877"      |  "70"           | "45" | "115"        | 107          || 200               // successful sms
        1            | ""                | "Airtel store" | "0.00"     | 25                  | "01621215877"      | "d867025b-d813-4f51-947b-a0a8ea179d68" | "01621215877"      |  "70"           | "45" | "115"        | 107          || 400               // 1 is not a valid account type
        2            | ""                | "Airtel store" | "0.00"     | 16                  | "01621215877"      | "d867025b-d813-4f51-947b-a0a8ea179d68" | "01621215877"      |  "70"           | "45" | "115"        | 107          || 404               // customer not found (wrong customer_account_id)
        2            | ""                | "Airtel store" | "0.00"     | 25                  | "01621215876"      | "d867025b-d813-4f51-947b-a0a8ea179d68" | "01621215877"      |  "70"           | "45" | "115"        | 107          || 404               // customer not found (wrong customer_mobile_no)
        2            | ""                | "Airtel store" | "0.00"     | 25                  | "01621215877"      | "1ab24185-b570-47c2-9681-706a5d0f560d" | "01621215877"      |  "70"           | "45" | "115"        | 107          || 403               // inactive device (wrong device_uuid)
        2            | ""                | "Airtel store" | "0.00"     | 25                  | "01621215877"      | "d867025b-d813-4f51-947b-a0a8ea179d68" | "01621215876"      |  "70"           | "45" | "115"        | 107          || 400               // no device found (wrong merchant_number)

    }

    def 'payment link sms response body'() {

        given:
        def requestBody =
                [
                        "account_type": account_type,
                        "business_address": business_address,
                        "business_name": business_name,
                        "collection": collection,
                        "customer_account_id": customer_account_id,
                        "customer_mobile_no": customer_mobile_no,
                        "device_uuid": device_uuid,
                        "merchant_mobile_no": merchant_mobile_no,
                        "previous_credit": previous_credit,
                        "sale": sale,
                        "total_credit": total_credit,
                        "version_code": version_code
                ]

        when:
        restClient.headers['Authorization'] = "Basic cHJvZ290aV9xYTpwcjBnMHQxQDIwMnR3bw=="
        restClient.headers['x-auth-token'] = "05pmN9hrpwf6QDYhC2NSa0QrcPqtwS2TYjQYKJhV"
        def response
        try {
            response = restClient.post(path:'/api/notification/v3/sms', body: requestBody, requestContentType: 'application/json')
        } catch (HttpResponseException ex)
        {
            response = ex.response
        }
        response = new JsonBuilder(response.responseData).toPrettyString()

        then:
        def finding = response.indexOf("https")
        println("response " + finding)
        if(finding > 0)
        {
            println("passed")
        }
        else
        {
            println("failed")
        }
//        response.status == responseStatus


        where:
        account_type | business_address  | business_name  | collection | customer_account_id | customer_mobile_no | device_uuid                            | merchant_mobile_no | previous_credit | sale | total_credit | version_code || responseStatus
        2            | ""                | "Airtel store" | "0.00"     | 25                  | "01621215877"      | "b43ccdf1-3691-401a-9fcb-5d46600d1f18" | "01621215877"      |  "70"           | "45" | "115"        | 107          || 200               // successful sms
        1            | ""                | "Airtel store" | "0.00"     | 25                  | "01621215877"      | "b43ccdf1-3691-401a-9fcb-5d46600d1f18" | "01621215877"      |  "70"           | "45" | "115"        | 107          || 200               // successful sms

    }


}
