package carve.ribbonconsul;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.ribbon.ClientOptions;
import com.netflix.ribbon.ResponseValidator;
import com.netflix.ribbon.Ribbon;
import com.netflix.ribbon.ServerError;
import com.netflix.ribbon.UnsuccessfulResponseException;
import com.netflix.ribbon.http.HttpRequestBuilder;
import com.netflix.ribbon.http.HttpRequestTemplate;
import com.netflix.ribbon.http.HttpResourceGroup;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;

@Path("/ribbon")
public class RibbonConsulResource {

    private ObjectMapper mapper = new ObjectMapper();

    static {
        System.setProperty("ribbon.NIWSServerListClassName", "carve.ribbonconsul.ConsulServerList");
    }

    static HttpResourceGroup httpResourceGroup = Ribbon.createHttpResourceGroup("hello",
            ClientOptions.create()
                    .withMaxAutoRetriesNextServer(2)
                    .withConnectTimeout(10000)
                    .withReadTimeout(5000)
                    .withLoadBalancerEnabled(true)
                    .withMaxConnectionsPerHost(10)
                    .withMaxTotalConnections(20));

    @SuppressWarnings("unchecked")
    HttpRequestTemplate<ByteBuf> helloTemplate = httpResourceGroup
            .newTemplateBuilder("helloTemplate", ByteBuf.class)
            .withMethod("GET")
            .withUriTemplate("/hello/v1/hello/")
            .withFallbackProvider(new HelloFallback())
            .build();

    @SuppressWarnings("unchecked")
    HttpRequestTemplate<ByteBuf> worldTemplate = httpResourceGroup
            .newTemplateBuilder("worldTemplate", ByteBuf.class)
            .withMethod("GET")
            .withUriTemplate("/hello/v1/hello/world")
            .withResponseValidator(new ResponseValidator<HttpClientResponse<ByteBuf>>() {
                @Override
                public void validate(HttpClientResponse<ByteBuf> response)
                        throws UnsuccessfulResponseException, ServerError {
                    if (response.getStatus().code() >= 500) {
                        throw new ServerError(response.getStatus().reasonPhrase());
                    }
                }
            })
            .build();

    @GET
    @Path("hello")
    @Produces("text/plain")
    public String hello() {
        ByteBuf resp = helloTemplate.requestBuilder().build().execute();
        return resp.toString(StandardCharsets.ISO_8859_1);
    }

    @GET
    @Path("world")
    @Produces("text/plain")
    public String world() throws JsonParseException, JsonMappingException, IOException {
        MDC.put("REQUEST-ID", "12345678");
        ByteBuf resp = requestBuilderWithHeaders(worldTemplate).build().execute();
        return mapper.readValue(resp.toString(StandardCharsets.ISO_8859_1), World.class).getHelloWorld();
    }

    public static HttpRequestBuilder<ByteBuf> requestBuilderWithHeaders(HttpRequestTemplate<ByteBuf> template) {
        HttpRequestBuilder<ByteBuf> builder = template.requestBuilder();
        MDC.getCopyOfContextMap().forEach((k, v) -> builder.withHeader("X-MDC-" + k, v));
        return builder;
    }
}
