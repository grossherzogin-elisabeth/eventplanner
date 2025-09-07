package org.eventplanner.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

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
            Resource requestedResource = location.createRelative(resourcePath);
            return requestedResource.exists() && requestedResource.isReadable()
                ? requestedResource
                : new ClassPathResource("/static/index.html");
        }
    }
}