package org.nature.gateway;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocatorConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        routes.route("http", r -> r.path("/http/**")
                .filters(f -> f.rewritePath("/http/(?<segment>.*)", "/$\\{segment}"))
                .uri("lb://http"));
        routes.route("web-socket", r -> r.path("/web-socket/**")
                .filters(f -> f.rewritePath("/web-socket/(?<segment>.*)", "/$\\{segment}"))
                .uri("lb://web-socket"));
        return routes.build();
    }
}
