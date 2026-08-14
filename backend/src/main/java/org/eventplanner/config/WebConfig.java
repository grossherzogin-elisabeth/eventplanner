package org.eventplanner.config;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // resolves the home endpoint, e.g. http://localhost:8080
        registry.addResourceHandler("")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolverWithIndexHTMLFallback());

        // resolves all other endpoints, for static resources or other views
        registry.addResourceHandler("*")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolverWithIndexHTMLFallback());
    }

    /**
     * Our frontend is a Single Page Application, so there only is one single index.html. For other views than the
     * root view to be resolved, we have to add a fallback to the index.html.
     */
    private static class PathResourceResolverWithIndexHTMLFallback extends PathResourceResolver {
        @Override
        protected @NonNull Resource getResource(@NonNull String resourcePath, @NonNull Resource location)
        throws IOException {
            // `..` and `%2e%2e` are 2nd layer of defense, as Spring should already reject these
            if (resourcePath.contains("..")
                || resourcePath.contains("%2e%2e")
                || resourcePath.contains("/.env")
                || resourcePath.contains("/.git")
                || resourcePath.contains("/application.yml")
                || resourcePath.contains("/application-local.yml")
                || resourcePath.contains("/application-secrets.yml")) {
                throw new NoSuchElementException("Resource not found");
            }
            if (resourcePath.startsWith("/api/")) {
                // this branch only triggers for routes that are not mapped to any endpoint -> 404
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                    throw new UnauthorizedException("Authentication required");
                } else {
                    throw new NoSuchElementException("Resource not found");
                }
            }
            // return frontend resources
            Resource requestedResource = super.getResource(resourcePath, location);
            if (requestedResource != null) {
                return requestedResource;
            }
            if (resourcePath.contains(".") && !resourcePath.contains(".html")) {
                throw new NoSuchElementException("Resource not found");
            }
            return new ClassPathResource("/static/index.html");
        }
    }
}