package com.citihub.configr.namespace;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/configuration")
public class NamespaceController {

  private NamespaceService configurationService;

  public NamespaceController(@Autowired NamespaceService configurationService) {
    this.configurationService = configurationService;
  }

  @PreAuthorize("@authorizer.canRead()")
  @GetMapping(path = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Map<String, Object> getData(HttpServletRequest request,
      HttpServletResponse response) {
    return configurationService.getNamespaceValue(getTrimmedPath(request));
  }

  @PreAuthorize("@authorizer.canWrite()")
  @PostMapping(consumes = {"application/json", "application/yaml", "application/yml"},
      value = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Namespace postData(@RequestBody Map<String, Object> json,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.info("You asked for me to POST: " + json + " to the namespace " + request.getRequestURI());

    return configurationService.storeNamespaceValue(json, getTrimmedPath(request), false, false);
  }

  @PreAuthorize("@authorizer.canWrite()")
  @PutMapping(consumes = {"application/json", "application/yaml", "application/yml"}, value = "/**",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Namespace putData(@RequestBody Map<String, Object> json,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.info("You asked for me to PUT: " + json + " to the namespace " + request.getRequestURI());

    return configurationService.storeNamespaceValue(json, getTrimmedPath(request), false, true);
  }

  @PreAuthorize("@authorizer.canWrite()")
  @PatchMapping(consumes = {"application/json", "application/yaml", "application/yml"},
      value = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Namespace patchWithData(@RequestBody Map<String, Object> json,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.info("You asked for me to PATCH: " + json + " to the namespace " + request.getRequestURI());

    return configurationService.storeNamespaceValue(json, getTrimmedPath(request), true, true);
  }

  @PreAuthorize("@authorizer.canDelete()")
  @DeleteMapping(value = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Namespace delete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    log.info("You asked for me to DELETE the namespace " + request.getRequestURI());

    return configurationService.deleteNamespace(getTrimmedPath(request));
  }


  String getTrimmedPath(HttpServletRequest request) {
    return request.getRequestURI().replace("/configuration", "");
  }

}
