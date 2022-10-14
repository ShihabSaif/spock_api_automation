import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j

@Slf4j
class Util {
    static def logRequest(request, response) {
        log.info("Reuqest: {} \n Response: {}",
                new JsonBuilder(request).toPrettyString(),
                new JsonBuilder(response).toPrettyString())

    }
}
