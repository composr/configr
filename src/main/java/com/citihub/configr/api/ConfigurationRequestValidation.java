package com.citihub.configr.api;

import com.google.common.base.Strings;

public class ConfigurationRequestValidation {

  public static boolean isRequestURIAValidNamespace(String requestURI) {
    if (Strings.isNullOrEmpty(requestURI) || requestURI.split("/").length < 3)
      return false;

    return true;
  }
}
