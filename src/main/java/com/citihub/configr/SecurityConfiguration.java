package com.citihub.configr;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.azure.spring.aad.webapi.AADResourceServerWebSecurityConfigurerAdapter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends AADResourceServerWebSecurityConfigurerAdapter {

  private final String[] ALLOWED_VERBS = {"HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"};
  private final String[] ALLOWED_HEADERS = {"Authorization", "Cache-Control", "Content-Type"};

  @Value("${authentication.enabled}")
  private boolean authEnabled;

  @Value("${authentication.proxy.host}")
  private String authProxyHost;

  @Value("${authentication.proxy.port}")
  private String authProxyPort;

  @Value("${cors.allowedOrigins}")
  private String[] allowedOrigins;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable();

    if (authEnabled) {
      log.info("Using Azure AD authentication.");
      super.configure(http);
      enableRequestAuth(http);
      checkAuthProxies();
    } else {
      log.info("Bypassing all authentication.");
      disableRequestAuth(http);
    }
  }

  private void checkAuthProxies() {
    if (!Strings.isNullOrEmpty(authProxyHost)) {
      log.info("Using auth proxy, {}:{}, for calls to microsoftonline", authProxyHost,
          authProxyPort);

      System.setProperty("https.proxyHost", authProxyHost);
      System.setProperty("https.proxyPort", authProxyPort);
      System.setProperty("https.nonProxyHost", "!*microsoftonline*");
    }
  }

  private void disableRequestAuth(HttpSecurity http) throws Exception {
    http.authorizeRequests().anyRequest().permitAll().and().csrf().disable();
  }

  private void enableRequestAuth(HttpSecurity http) throws Exception {
    http.authorizeRequests((requests) -> {
      requests.antMatchers("/swagger-ui.html").permitAll().antMatchers("/swagger-ui/**").permitAll()
          .antMatchers("/v3/**").permitAll().antMatchers("/liveness").permitAll().anyRequest()
          .authenticated();
    });
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(allowedOrigins));
    configuration.setAllowedMethods(List.of(ALLOWED_VERBS));
    configuration.setAllowCredentials(true);
    configuration.setAllowedHeaders(List.of(ALLOWED_HEADERS));

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
