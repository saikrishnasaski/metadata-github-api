package com.csk.services.metadata.github.api.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class GithubMetadataController {

    private final WebClient webClient;
    private final TokenManager tokenManager;

    @GetMapping("/create")
    public Mono<Repositories> createRepo(HttpServletRequest httpServletRequest) {

        var accessToken = "Bearer " + tokenManager.getAccessToken(httpServletRequest);

        CreateRepo createRepo = new CreateRepo("sample-repo", "testing", "https://www.github.com/saikrishnasaski", false);

        return webClient.post()
                .uri("/user/repos")
                .headers(httpHeaders -> {
                    httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    httpHeaders.add("Authorization", accessToken);
                })
                .accept(MediaType.valueOf("application/vnd.github+json"))
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("github"))
                .body(Mono.just(createRepo), CreateRepo.class)
                .retrieve()
                .bodyToMono(Repositories.class);

    }

    @GetMapping
    public String hello() {

        var securityContext = SecurityContextHolder.getContext();

        securityContext.getAuthentication().getCredentials();

        var oAuth2User = (DefaultOAuth2User) securityContext.getAuthentication().getPrincipal();

        var userName = oAuth2User.getAttributes().get("name");


        return "hello " + userName;
    }



    @GetMapping("/repositories")
    public Flux<Repositories> getRepositories() {
        log.info("Fetching Repositories");

        var securityContext = SecurityContextHolder.getContext();

        var oAuth2User = (DefaultOAuth2User) securityContext.getAuthentication().getPrincipal();

        var userName = (String) oAuth2User.getAttributes().get("login");

        return getRepositories(userName);
    }

    @GetMapping("/repositories/private")
    public Flux<Repositories> getPrivateRepositories(HttpServletRequest request) {

        var accessToken = "Bearer " + tokenManager.getAccessToken(request);
        log.info("Fetching Private Repositories");

        var uriVariables = new HashMap<String, String>();
        uriVariables.put("visibility", "private");

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/repos")
                        .queryParam("visibility", "private")
                        .build())
                .headers(httpHeaders -> {
                    httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    httpHeaders.add("Authorization", accessToken);
                })
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("github"))
                .retrieve()
                .bodyToFlux(Repositories.class);
    }

    private Flux<Repositories> getRepositories(String userName) {

        return webClient.get()
                .uri("/users/{username}/repos", userName)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("github"))
                .retrieve()
                .bodyToFlux(Repositories.class);

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Repositories {

        private String id;
        private String node_id;
        private String name;
        private String full_name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class CreateRepo {

        private String name;
        private String description;
        private String homepage;
        private Boolean is_template;
    }
}
