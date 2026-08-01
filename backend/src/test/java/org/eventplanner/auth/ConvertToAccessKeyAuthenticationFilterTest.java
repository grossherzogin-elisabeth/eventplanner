package org.eventplanner.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ConvertToAccessKeyAuthenticationFilterTest {

    private AuthenticationMutexHolder authenticationMutexHolder;
    private ConvertToAccessKeyAuthenticationFilter testee;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        authenticationMutexHolder = mock();
        when(authenticationMutexHolder.getMutex(any())).thenReturn(new Object());
        testee = new ConvertToAccessKeyAuthenticationFilter(authenticationMutexHolder);
        request = mock();
        response = mock();
        filterChain = mock();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldConvertAnonymousAuthenticationToAccessKeyAuthentication() throws Exception {
        when(request.getParameter("accessKey")).thenReturn("access-1");
        SecurityContextHolder.setContext(new SecurityContextImpl(createAnonymousAuthentication()));

        testee.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(AccessKeyAuthentication.class);
        var authentication = (AccessKeyAuthentication) SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getCredentials().value()).isEqualTo("access-1");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReuseCachedSecurityContextForSameAccessKey() throws Exception {
        when(request.getParameter("accessKey")).thenReturn("access-1");

        SecurityContextHolder.setContext(new SecurityContextImpl(createAnonymousAuthentication()));
        testee.doFilterInternal(request, response, filterChain);
        var firstContext = SecurityContextHolder.getContext();

        SecurityContextHolder.setContext(new SecurityContextImpl(createAnonymousAuthentication()));
        testee.doFilterInternal(request, response, filterChain);
        var secondContext = SecurityContextHolder.getContext();

        assertThat(secondContext).isSameAs(firstContext);
    }

    @Test
    void shouldUseDifferentCachedContextsForDifferentAccessKeys() throws Exception {
        var request1 = mock(HttpServletRequest.class);
        var request2 = mock(HttpServletRequest.class);
        when(request1.getParameter("accessKey")).thenReturn("access-1");
        when(request2.getParameter("accessKey")).thenReturn("access-2");

        SecurityContextHolder.setContext(new SecurityContextImpl(createAnonymousAuthentication()));
        testee.doFilterInternal(request1, response, filterChain);
        var firstContext = SecurityContextHolder.getContext();

        SecurityContextHolder.setContext(new SecurityContextImpl(createAnonymousAuthentication()));
        testee.doFilterInternal(request2, response, filterChain);
        var secondContext = SecurityContextHolder.getContext();

        assertThat(secondContext).isNotSameAs(firstContext);
    }

    @Test
    void shouldNotConvertWhenAuthenticationIsNotAnonymous() throws Exception {
        when(request.getParameter("accessKey")).thenReturn("access-1");
        SecurityContextHolder.setContext(new SecurityContextImpl(createSignedInUser()));
        var previousAuthentication = SecurityContextHolder.getContext().getAuthentication();

        testee.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previousAuthentication);
        verifyNoInteractions(authenticationMutexHolder);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotConvertWhenAccessKeyIsMissing() throws Exception {
        when(request.getParameter("accessKey")).thenReturn(null);
        SecurityContextHolder.setContext(new SecurityContextImpl(createAnonymousAuthentication()));
        var previousAuthentication = SecurityContextHolder.getContext().getAuthentication();

        testee.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previousAuthentication);
        verifyNoInteractions(authenticationMutexHolder);
        verify(filterChain).doFilter(request, response);
    }

    private AnonymousAuthenticationToken createAnonymousAuthentication() {
        return new AnonymousAuthenticationToken(
            "test-key",
            "anonymous-user",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
    }
}
