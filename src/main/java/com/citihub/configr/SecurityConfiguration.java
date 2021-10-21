package com.citihub.configr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import com.azure.spring.aad.webapi.AADResourceServerWebSecurityConfigurerAdapter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends AADResourceServerWebSecurityConfigurerAdapter {

  @Value("${authentication.enabled}")
  private boolean authEnabled;

  /**
   * Add configuration logic as needed.
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.authorizeRequests(authEnabled ? (requests) -> requests.antMatchers("/swagger-ui.html")
        .permitAll().antMatchers("/swagger-ui/**").permitAll(). // Swagger resources
        antMatchers("/v3/**").permitAll().antMatchers("/liveness").permitAll().anyRequest()
        .authenticated() : (requests) -> requests.anyRequest().permitAll());
  }
}
