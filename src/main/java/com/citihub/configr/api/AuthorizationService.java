package com.citihub.configr.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;

@Component("authorizer")
public class AuthorizationService {

  @Value("${authorization.enabled}")
  private boolean enabled;

  @Value("${authorization.roles.read}")
  private String readAllowedRoles;

  @Value("${authorization.roles.write}")
  private String writeAllowedRoles;

  public boolean canRead() {
    if (enabled) {
      BearerTokenAuthentication token =
          (BearerTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
      return token.getAuthorities().stream()
          .anyMatch(p -> readAllowedRoles.matches(".*?" + p.getAuthority() + ".*?"));
    } else
      return true;
  }

  public boolean canWrite() {
    if (enabled) {
      BearerTokenAuthentication token =
          (BearerTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
      return token.getAuthorities().stream()
          .anyMatch(p -> writeAllowedRoles.matches(".*?" + p.getAuthority() + ".*?"));
    } else
      return true;
  }

}
