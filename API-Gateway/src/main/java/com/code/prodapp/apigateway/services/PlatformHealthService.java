package com.code.prodapp.apigateway.services;

import com.code.prodapp.apigateway.configs.PlatformHealthProperties;
import com.code.prodapp.apigateway.dtos.HealthCheckResult;
import com.code.prodapp.apigateway.dtos.PlatformHealthResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlatformHealthService {

    private final PlatformHealthProperties platformHealthProperties;
    private final WebClient.Builder webClientBuilder = WebClient.builder();

    public PlatformHealthService(PlatformHealthProperties platformHealthProperties) {
        this.platformHealthProperties = platformHealthProperties;
    }

    public Mono<PlatformHealthResponse> checkPlatformHealth() {
        Mono<List<HealthCheckResult>> httpChecks = Flux.fromIterable(platformHealthProperties.getHttpTargets())
                .flatMap(this::checkHttpTarget)
                .collectList();

        Mono<List<HealthCheckResult>> tcpChecks = Flux.fromIterable(platformHealthProperties.getTcpTargets())
                .flatMap(this::checkTcpTarget)
                .collectList();

        return Mono.zip(httpChecks, tcpChecks)
                .map(tuple -> {
                    List<HealthCheckResult> checks = new ArrayList<>();
                    checks.addAll(tuple.getT1());
                    checks.addAll(tuple.getT2());
                    boolean allUp = checks.stream().allMatch(check -> "UP".equals(check.status()));
                    return new PlatformHealthResponse(
                            Instant.now(),
                            allUp ? "UP" : "DEGRADED",
                            checks
                    );
                });
    }

    private Mono<HealthCheckResult> checkHttpTarget(PlatformHealthProperties.HttpTarget target) {
        long startedAt = System.currentTimeMillis();
        return webClientBuilder
                .build()
                .get()
                .uri(target.getUrl())
                .exchangeToMono(response -> Mono.just(httpResult(target, response.statusCode(), startedAt)))
                .timeout(timeout())
                .onErrorResume(exception -> Mono.just(new HealthCheckResult(
                        target.getName(),
                        "HTTP",
                        target.getUrl(),
                        "DOWN",
                        null,
                        elapsed(startedAt),
                        exception.getClass().getSimpleName() + ": " + exception.getMessage()
                )));
    }

    private Mono<HealthCheckResult> checkTcpTarget(PlatformHealthProperties.TcpTarget target) {
        long startedAt = System.currentTimeMillis();
        return Mono.fromCallable(() -> {
                    try (Socket socket = new Socket()) {
                        socket.connect(
                                new InetSocketAddress(target.getHost(), target.getPort()),
                                platformHealthProperties.getTimeoutMillis()
                        );
                        return new HealthCheckResult(
                                target.getName(),
                                "TCP",
                                target.getHost() + ":" + target.getPort(),
                                "UP",
                                null,
                                elapsed(startedAt),
                                "TCP connection accepted"
                        );
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(timeout())
                .onErrorResume(exception -> Mono.just(new HealthCheckResult(
                        target.getName(),
                        "TCP",
                        target.getHost() + ":" + target.getPort(),
                        "DOWN",
                        null,
                        elapsed(startedAt),
                        exception.getClass().getSimpleName() + ": " + exception.getMessage()
                )));
    }

    private HealthCheckResult httpResult(
            PlatformHealthProperties.HttpTarget target,
            HttpStatusCode statusCode,
            long startedAt
    ) {
        boolean reachable = statusCode.value() < 500;
        return new HealthCheckResult(
                target.getName(),
                "HTTP",
                target.getUrl(),
                reachable ? "UP" : "DOWN",
                statusCode.value(),
                elapsed(startedAt),
                reachable ? "HTTP response received" : "Service returned server error"
        );
    }

    private Duration timeout() {
        return Duration.ofMillis(platformHealthProperties.getTimeoutMillis());
    }

    private long elapsed(long startedAt) {
        return System.currentTimeMillis() - startedAt;
    }
}
