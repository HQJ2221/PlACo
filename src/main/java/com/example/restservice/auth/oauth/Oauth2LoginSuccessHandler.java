package com.example.restservice.auth.oauth;

import com.example.restservice.model.OauthUser;
import com.example.restservice.model.Role;
import com.example.restservice.model.User;
import com.example.restservice.repository.OauthUserRepository;
import com.example.restservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Component
public class Oauth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final UserRepository userRepository;

    private final OauthUserRepository oauthUserRepository;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public Oauth2LoginSuccessHandler(UserRepository userRepository, OauthUserRepository oauthUserRepository) {
        this.userRepository = userRepository;
        this.oauthUserRepository = oauthUserRepository;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        final DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        final OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        final String provider = authenticationToken.getAuthorizedClientRegistrationId();
        final String subject = oidcUser.getName();

        // Find existing and connected account
        Optional<OauthUser> oauthUser = oauthUserRepository.findByProviderAndSubject(provider, subject);
        if (oauthUser.isPresent()) {
            final User user = oauthUser.get().getUser();
            authenticate(user, response, request);
            return;
        }

        // Create and connect new account
        User newUser = createUserFromOidc(oidcUser, provider);
        User savedUser = userRepository.save(newUser);

        // Create OauthUser to link the user to the provider
        OauthUser newOauthUser = new OauthUser();
        newOauthUser.setProvider(provider);
        newOauthUser.setSubject(subject);
        newOauthUser.setUser(savedUser);
        oauthUserRepository.save(newOauthUser);

        // Authenticate the new user
        authenticate(savedUser, response, request);
    }

    private User createUserFromOidc(DefaultOidcUser oidcUser, String provider) {
        User user = new User();
        user.setEmail(oidcUser.getEmail() != null ? oidcUser.getEmail() : oidcUser.getName());
        user.setUsername(oidcUser.getEmail() != null ? oidcUser.getEmail() : oidcUser.getName());
        user.setPassword("no_password");
        user.setRole(Role.USER);
        return user;
    }

    private void authenticate(User user, HttpServletResponse response, HttpServletRequest request) throws IOException {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        response.sendRedirect(frontendUrl + "/dashboard");
    }
}