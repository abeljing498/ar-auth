package com.lyentech.bdc.md.auth.config.authorization;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author :yan
 * @Date :Create in 2022/6/28
 * @Description :
 */

//@Configuration
public class RedirectResolverConfiguration extends DefaultRedirectResolver {

    private Collection<String> redirectGrantTypes = Arrays.asList("implicit", "authorization_code");

    @Override
    public String resolveRedirect(String requestedRedirect, ClientDetails client) throws OAuth2Exception {

        Set<String> authorizedGrantTypes = client.getAuthorizedGrantTypes();
        if (authorizedGrantTypes.isEmpty()) {
            throw new InvalidGrantException("A client must have at least one authorized grant type.");
        }
        if (!containsRedirectGrantType(authorizedGrantTypes)) {
            throw new InvalidGrantException(
                    "A redirect_uri can only be used by implicit or authorization_code grant types.");
        }

        Set<String> registeredRedirectUris = client.getRegisteredRedirectUri();
        if (registeredRedirectUris == null || registeredRedirectUris.isEmpty()) {
            throw new InvalidRequestException("At least one redirect_uri must be registered with the client.");
        }
        return obtainMatchingRedirect(registeredRedirectUris, requestedRedirect);
    }

    private boolean containsRedirectGrantType(Set<String> grantTypes) {
        for (String type : grantTypes) {
            if (redirectGrantTypes.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private String obtainMatchingRedirect(Set<String> redirectUris, String requestedRedirect) {
        Assert.notEmpty(redirectUris, "Redirect URIs cannot be empty");
        if (redirectUris.size() == 1 && requestedRedirect == null) {
            return (String) redirectUris.iterator().next();
        } else {
            Iterator var3 = redirectUris.iterator();

            String redirectUri;
            do {
                if (!var3.hasNext()) {
                    throw new RedirectMismatchException("Invalid redirect uri does not match one of the registered values.");
                }

                redirectUri = (String) var3.next();
            } while (requestedRedirect == null || !this.redirectMatches(requestedRedirect, redirectUri));

            return requestedRedirect;
        }
    }
}
