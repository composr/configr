package com.citihub.configr.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.citihub.configr.authorization.AclValidator;

@Component("authorizer")
public class AuthorizationService {

  @Value("${authorization.enabled}")
  private boolean enabled;

  @Value("${authorization.roles.read}")
  private String readAllowedRoles;

  @Value("${authorization.roles.write}")
  private String writeAllowedRoles;

  @Value("${authorization.roles.delete}")
  private String deleteAllowedRoles;

  private AclValidator aclValidator;

  public AuthorizationService(@Autowired AclValidator aclValidator) {
    this.aclValidator = aclValidator;
  }

  public boolean canRead() {
    if (enabled) {
      return this.aclValidator.validateAcls(AclValidator.Action.READ, readAllowedRoles);
    } else
      return true;
  }

  public boolean canWrite() {
    if (enabled) {
      return this.aclValidator.validateAcls(AclValidator.Action.WRITE, writeAllowedRoles);
    } else
      return true;
  }

  public boolean canDelete() {
    if (enabled) {
      return this.aclValidator.validateAcls(AclValidator.Action.DELETE, deleteAllowedRoles);
    } else
      return true;
  }

}
