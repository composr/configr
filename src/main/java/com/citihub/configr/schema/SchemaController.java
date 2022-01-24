package com.citihub.configr.schema;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.citihub.configr.metadata.SchemaValidationResult;
import com.citihub.configr.namespace.Namespace;
import com.citihub.configr.namespace.NamespaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(path = "/validity")
public class SchemaController {

  private SchemaValidationService schemaValidationService;
  private NamespaceService namespaceService;

  public SchemaController(@Autowired SchemaValidationService schemaValidationService,
      @Autowired NamespaceService namespaceService) {
    this.schemaValidationService = schemaValidationService;
    this.namespaceService = namespaceService;
  }

  @PreAuthorize("@authorizer.canWrite()")
  @GetMapping(consumes = {"application/json", "application/yaml", "application/yml"}, value = "/**",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody String validateNamespace(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    log.info("You asked for me to GET validity of: " + getTrimmedPath(request));
    String namespace = getTrimmedPath(request);

    Namespace ns = namespaceService.fetchNamespace(namespace);
    Optional<SchemaValidationResult> result = schemaValidationService.getValidationReport(ns);
    return result.get().getReport().toString();
  }

  String getTrimmedPath(HttpServletRequest request) {
    return request.getRequestURI().replace("/metadata/schema", "");
  }

}
