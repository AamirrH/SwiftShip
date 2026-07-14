package com.code.prodapp.apigateway.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "platform.health")
public class PlatformHealthProperties {

    private List<HttpTarget> httpTargets = defaultHttpTargets();
    private List<TcpTarget> tcpTargets = defaultTcpTargets();
    private Integer timeoutMillis = 3000;

    public List<HttpTarget> getHttpTargets() {
        return httpTargets;
    }

    public void setHttpTargets(List<HttpTarget> httpTargets) {
        this.httpTargets = httpTargets;
    }

    public List<TcpTarget> getTcpTargets() {
        return tcpTargets;
    }

    public void setTcpTargets(List<TcpTarget> tcpTargets) {
        this.tcpTargets = tcpTargets;
    }

    public Integer getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(Integer timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public static class HttpTarget {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class TcpTarget {
        private String name;
        private String host;
        private Integer port;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }

    private static List<HttpTarget> defaultHttpTargets() {
        List<HttpTarget> targets = new ArrayList<>();
        targets.add(httpTarget("Discovery Service", "http://localhost:8761"));
        targets.add(httpTarget("Config Server", "http://localhost:9080/actuator/health"));
        targets.add(httpTarget("API Gateway", "http://localhost:9090/actuator/health"));
        targets.add(httpTarget("Auth Service", "http://localhost:9040/auth/login"));
        targets.add(httpTarget("Inventory Service", "http://localhost:9010/actuator/health"));
        targets.add(httpTarget("Order Service", "http://localhost:9020/actuator/health"));
        targets.add(httpTarget("Notification Service", "http://localhost:9050/actuator/health"));
        targets.add(httpTarget("Warehouse Service", "http://localhost:9060/actuator/health"));
        targets.add(httpTarget("Routing Service", "http://localhost:9070/actuator/health"));
        targets.add(httpTarget("Tracking Service", "http://localhost:9099/actuator/health"));
        return targets;
    }

    private static List<TcpTarget> defaultTcpTargets() {
        List<TcpTarget> targets = new ArrayList<>();
        targets.add(tcpTarget("Kafka Server", "localhost", 9092));
        targets.add(tcpTarget("Redis", "localhost", 6379));
        targets.add(tcpTarget("PostgreSQL", "localhost", 5433));
        return targets;
    }

    private static HttpTarget httpTarget(String name, String url) {
        HttpTarget target = new HttpTarget();
        target.setName(name);
        target.setUrl(url);
        return target;
    }

    private static TcpTarget tcpTarget(String name, String host, Integer port) {
        TcpTarget target = new TcpTarget();
        target.setName(name);
        target.setHost(host);
        target.setPort(port);
        return target;
    }
}
