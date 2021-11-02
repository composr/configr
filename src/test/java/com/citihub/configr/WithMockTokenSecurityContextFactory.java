package com.citihub.configr;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class WithMockTokenSecurityContextFactory
    implements WithSecurityContextFactory<WithMockToken> {

  @Override
  public SecurityContext createSecurityContext(WithMockToken withUser) {
    String username =
        StringUtils.hasLength(withUser.username()) ? withUser.username() : withUser.value();
    Assert.notNull(username,
        () -> withUser + " cannot have null username on both username and value properties");
    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    for (String authority : withUser.authorities()) {
      grantedAuthorities.add(new SimpleGrantedAuthority(authority));
    }
    if (grantedAuthorities.isEmpty()) {
      for (String role : withUser.roles()) {
        Assert.isTrue(!role.startsWith("ROLE_"), () -> "roles cannot start with ROLE_ Got " + role);
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
      }
    } else if (!(withUser.roles().length == 1 && "USER".equals(withUser.roles()[0]))) {
      throw new IllegalStateException(
          "You cannot define roles attribute " + Arrays.asList(withUser.roles())
              + " with authorities attribute " + Arrays.asList(withUser.authorities()));
    }
    Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("typ", "JWT");
    OAuth2AuthenticatedPrincipal p =
        new DefaultOAuth2AuthenticatedPrincipal(attributes, grantedAuthorities);
    OAuth2AccessToken token =
        new OAuth2AccessToken(TokenType.BEARER, username, Instant.now(), Instant.MAX);
    Authentication authentication = new BearerTokenAuthentication(p, token, grantedAuthorities);
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    return context;
  }
}
