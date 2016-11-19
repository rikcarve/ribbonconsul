package carve.ribbonconsul;

import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.netflix.ribbon.ClientOptions;
import com.netflix.ribbon.Ribbon;
import com.netflix.ribbon.http.HttpRequestTemplate;
import com.netflix.ribbon.http.HttpResourceGroup;

import io.netty.buffer.ByteBuf;

@Path("/ribbon")
public class RibbonConsulResource {

    // static HelloService helloProxy = Ribbon.from(HelloService.class);

    static {
        System.setProperty("ribbon.NIWSServerListClassName", "carve.ribbonconsul.ConsulServerList");
    }

    static HttpResourceGroup httpResourceGroup = Ribbon.createHttpResourceGroup("hello",
            ClientOptions.create()
                    .withMaxAutoRetriesNextServer(2)
                    .withConnectTimeout(10000)
                    .withReadTimeout(5000)
                    .withMaxConnectionsPerHost(2)
                    .withMaxTotalConnections(12));

    @SuppressWarnings("unchecked")
    static HttpRequestTemplate<ByteBuf> helloTemplate = httpResourceGroup.newTemplateBuilder("helloTemplate", ByteBuf.class)
            .withMethod("GET")
            .withUriTemplate("/hello/v1/hello/")
            .withFallbackProvider(new HelloFallback())
            .build();

    @SuppressWarnings("unchecked")
    static HttpRequestTemplate<ByteBuf> worldTemplate = httpResourceGroup.newTemplateBuilder("worldTemplate", ByteBuf.class)
            .withMethod("GET")
            .withUriTemplate("/hello/v1/hello/world")
            .build();

    @PostConstruct
    public void init() {
        // helloProxy = Ribbon.from(HelloService.class);
    }

    @GET
    @Path("hello")
    @Produces("text/plain")
    public String hello() {
        ByteBuf resp = helloTemplate.requestBuilder().build().execute();
        // ByteBuf resp = helloProxy.sayWorld().execute();
        return resp.toString(StandardCharsets.ISO_8859_1);
    }

    @GET
    @Path("world")
    @Produces("text/plain")
    public String world() {
        ByteBuf resp = worldTemplate.requestBuilder().build().execute();

        // ByteBuf resp = helloProxy.sayWorld().execute();
        return resp.toString(StandardCharsets.ISO_8859_1);
    }

}
