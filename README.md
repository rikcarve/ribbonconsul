# ribbonconsul
Client side loadbalancing through ribbon and consul

## Client
```java
    static {
        System.setProperty("ribbon.NIWSServerListClassName", "carve.ribbonconsul.ConsulServerList");
    }

    static HttpResourceGroup httpResourceGroup = Ribbon.createHttpResourceGroup("hello",
            ClientOptions.create()
                    .withMaxAutoRetriesNextServer(2)
                    .withConnectTimeout(1000)
                    .withReadTimeout(5000)
                    .withLoadBalancerEnabled(true)
                    .withMaxConnectionsPerHost(10)
                    .withMaxTotalConnections(20));

    @SuppressWarnings("unchecked")
    HttpRequestTemplate<ByteBuf> worldTemplate = httpResourceGroup
            .newTemplateBuilder("worldTemplate", ByteBuf.class)
            .withMethod("GET")
            .withUriTemplate("/hello/v1/hello/world")
            .withFallbackProvider(new HelloFallback())
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
```

## Maven config

		<dependency>
			<groupId>com.orbitz.consul</groupId>
			<artifactId>consul-client</artifactId>
			<version>0.12.8</version>
		</dependency>		
        
        <dependency>
            <groupId>com.netflix.hystrix</groupId>
            <artifactId>hystrix-metrics-event-stream</artifactId>
            <version>${hystrix.version}</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.hystrix</groupId>
            <artifactId>hystrix-core</artifactId>
            <version>${hystrix.version}</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.archaius</groupId>
            <artifactId>archaius-core</artifactId>
            <version>0.7.4</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.ribbon</groupId>
            <artifactId>ribbon</artifactId>
            <version>${ribbon.version}</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.ribbon</groupId>
            <artifactId>ribbon-core</artifactId>
            <version>${ribbon.version}</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.ribbon</groupId>
            <artifactId>ribbon-loadbalancer</artifactId>
            <version>${ribbon.version}</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.ribbon</groupId>
            <artifactId>ribbon-transport</artifactId>
            <version>${ribbon.version}</version>
        </dependency>
        <dependency>
        	<groupId>io.netty</groupId>
        	<artifactId>netty-buffer</artifactId>
        	<version>4.0.27.Final</version>
        </dependency>
        <dependency>
            <groupId>io.reactivex</groupId>
            <artifactId>rxjava</artifactId>
            <version>1.0.10</version>
        </dependency>
        <dependency>
            <groupId>io.reactivex</groupId>
            <artifactId>rxnetty</artifactId>
            <version>0.4.9</version>
        </dependency>
        <dependency>
        	<groupId>com.fasterxml.jackson.core</groupId>
        	<artifactId>jackson-core</artifactId>
        	<version>${jackson.version}</version>
        </dependency>
        <dependency>
        	<groupId>com.fasterxml.jackson.core</groupId>
        	<artifactId>jackson-databind</artifactId>
        	<version>${jackson.version}</version>
        </dependency>
