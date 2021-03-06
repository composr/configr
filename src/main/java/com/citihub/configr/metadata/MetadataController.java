package com.citihub.configr.metadata;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.citihub.configr.exception.NotFoundException;
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
    return metadataService.getMetadataForNamespace(getTrimmedPath(request))
        .orElseThrow(() -> new NotFoundException());
  }

  @PreAuthorize("@authorizer.canWrite()")
  @PutMapping(consumes = {"application/json", "application/yaml", "application/yml"}, value = "/**",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Metadata putData(@RequestBody Metadata metadata, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    log.info(
        "You asked for me to PUT: " + metadata + " to the namespace " + getTrimmedPath(request));

    return metadataService.setMetadataForNamespace(metadata, getTrimmedPath(request));
  }

  @PreAuthorize("@authorizer.canWrite()")
  @PatchMapping(consumes = {"application/json", "application/yaml", "application/yml"},
      value = "/**", produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody Metadata patchData(@RequestBody Metadata metadata,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.info(
        "You asked for me to PATCH: " + metadata + " to the namespace " + getTrimmedPath(request));

    return metadataService.patchMetadataForNamespace(metadata, getTrimmedPath(request));
  }

  String getTrimmedPath(HttpServletRequest request) {
    return request.getRequestURI().replace("/metadata", "");
  }

}
