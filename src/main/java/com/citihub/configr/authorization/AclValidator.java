package com.citihub.configr.authorization;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;
import com.citihub.configr.metadata.ACL;
import com.citihub.configr.metadata.Metadata;
import com.citihub.configr.metadata.MetadataService;

@Component
public class AclValidator {

  public enum Action {
    READ, WRITE, DELETE
  }

  private HttpServletRequest request;
  private MetadataService metadataService;

  public AclValidator(@Autowired HttpServletRequest request,
      @Autowired MetadataService metadataService) {
    this.request = request;
    this.metadataService = metadataService;
  }

  public boolean validateAcls(Action action, String allowedRoles) {
    BearerTokenAuthentication token =
        (BearerTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();

    String uri = getTrimmedPath(request.getRequestURI());
    Optional<Metadata> metadata = metadataService.getMetadataForNamespace(uri);
    if (metadata.isPresent()) {
      return validateRoles(action, metadata.get().getAcls(), token.getAuthorities());
    }

    return token.getAuthorities().stream()
        .anyMatch(p -> allowedRoles.matches(".*?" + p.getAuthority() + ".*?"));

  }

  private boolean validateRoles(Action action, Set<ACL> acls,
      Collection<GrantedAuthority> authorities) {
    for (ACL acl : acls) {
      if (aclMatchesAction(action, acl) && roleInToken(acl.getRole(), authorities)) {
        return true;
      }
    }
    return false;
  }

  private boolean aclMatchesAction(Action action, ACL acl) {
    if (action == Action.READ && acl.isRead()) {
      return true;
    } else if (action == Action.WRITE && acl.isWrite()) {
      return true;
    } else if (action == Action.DELETE && acl.isDelete()) {
      return true;
    }
    return false;
  }

  private boolean roleInToken(String role, Collection<GrantedAuthority> authorities) {
    return authorities.stream().anyMatch(
        grantedAuthority -> grantedAuthority.getAuthority().matches(".*?" + role + ".*?"));
  }

  String getTrimmedPath(String uri) {
    if (uri != null && uri.length() > 1 && uri.indexOf("/", 1) > 0)
      return uri.substring(uri.indexOf("/", 1));
    else
      return uri;
  }
}
