package com.citihub.configr.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;

@Component
public class PreAuthorizer {

  @Value("${authorization.roles.read}")
  private String readAllowedRoles;

  @Value("${authorization.roles.write}")
  private String writeAllowedRoles;

  public boolean canRead() {
    BearerTokenAuthentication token =
        (BearerTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
    return token.getAuthorities().stream()
        .anyMatch(p -> readAllowedRoles.matches(".*?" + p.getAuthority() + ".*?"));
  }

  public boolean canWrite() {
    BearerTokenAuthentication token =
        (BearerTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
    return token.getAuthorities().stream()
        .anyMatch(p -> writeAllowedRoles.matches(".*?" + p.getAuthority() + ".*?"));
  }

}
