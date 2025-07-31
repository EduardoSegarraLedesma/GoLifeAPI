package org.GoLifeAPI.mixed.security;

import jakarta.servlet.FilterChain;
import org.GoLifeAPI.security.RateLimitingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        // 30 Tokens Max per Minute per User
        filter = new RateLimitingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenUnauthenticated_200() throws Exception {
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void whenAuthenticatedUnderLimit_200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", null, List.of())
        );
        for (int i = 0; i < 30; i++) {
            response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertThat(response.getStatus()).isEqualTo(200);
        }
        verify(chain, times(30)).doFilter(any(), any());
    }

    @Test
    void whenAuthPresentButNotAuthenticated_treatedAsUnauthenticated() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(auth.getName()).thenReturn("ignored");
        SecurityContextHolder.getContext().setAuthentication(auth);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void whenAuthNameIsNull_treatedAsUnauthenticated() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void whenAuthenticatedOverLimit_429() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", null, List.of())
        );

        for (int i = 0; i < 30; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain, times(30)).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).isEqualTo("text/plain");
        assertThat(response.getContentAsString())
                .isEqualTo("Demasiadas peticiones. Espera un momento por favor.");
    }

    @Test
    void differentUsers_haveIndependentBuckets() throws Exception {
        // userA
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("userA", null, List.of())
        );
        for (int i = 0; i < 30; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse respA = new MockHttpServletResponse();
        filter.doFilter(request, respA, chain);
        assertThat(respA.getStatus()).isEqualTo(429);
        assertThat(respA.getContentType()).isEqualTo("text/plain");
        assertThat(respA.getContentAsString())
                .isEqualTo("Demasiadas peticiones. Espera un momento por favor.");

        // userB
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("userB", null, List.of())
        );
        MockHttpServletResponse respB = new MockHttpServletResponse();
        filter.doFilter(request, respB, chain);
        assertThat(respB.getStatus()).isEqualTo(200);
    }
}