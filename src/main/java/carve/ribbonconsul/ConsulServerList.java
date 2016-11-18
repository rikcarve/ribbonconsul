package carve.ribbonconsul;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HostAndPort;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.health.ServiceHealth;

public class ConsulServerList extends AbstractServerList<Server> {
    private static final Logger logger = LoggerFactory.getLogger(ConsulServerList.class);

    private static Consul consul = Consul.builder().withHostAndPort(HostAndPort.fromParts("192.168.99.100", 8500)).build();

    private String clientName;

    @Override
    public List<Server> getInitialListOfServers() {
        return null;
    }

    @Override
    public List<Server> getUpdatedListOfServers() {
        List<Server> result = new ArrayList<>();
        List<ServiceHealth> nodes = consul.healthClient().getHealthyServiceInstances(clientName).getResponse();
        logger.info("getUpdatedListOfServers: {}", nodes.toString());
        for (ServiceHealth node : nodes) {
            result.add(new Server(node.getService().getAddress(), node.getService().getPort()));
        }
        return result;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        clientName = clientConfig.getClientName();
    }

}
