package com.citihub.configr.metadata;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/metadata")
public class MetadataController {

  private MetadataService metadataService;

  public MetadataController(@Autowired MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @PreAuthorize("@authorizer.canRead()")
  @GetMapping(path = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Metadata getData(HttpServletRequest request, HttpServletResponse response) {
    return metadataService.getMetadataForNamespace(getTrimmedPath(request));
  }

  @PreAuthorize("@authorizer.canWrite()")
  @PostMapping(consumes = {"application/json", "application/yaml", "application/yml"},
      value = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Metadata postData(@RequestBody Metadata metadata, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    log.info(
        "You asked for me to POST: " + metadata + " to the namespace " + request.getRequestURI());

    return metadataService.setMetadataForNamespace(metadata, getTrimmedPath(request));
  }

  @PreAuthorize("@authorizer.canWrite()")
  @PutMapping(consumes = {"application/json", "application/yaml", "application/yml"}, value = "/**",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Metadata putData(@RequestBody Metadata metadata, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    log.info(
        "You asked for me to PUT: " + metadata + " to the namespace " + request.getRequestURI());

    return metadataService.setMetadataForNamespace(metadata, getTrimmedPath(request));
  }

  String getTrimmedPath(HttpServletRequest request) {
    return request.getRequestURI().replace("/metadata", "");
  }

}
