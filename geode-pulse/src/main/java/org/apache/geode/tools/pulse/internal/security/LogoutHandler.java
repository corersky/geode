/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.tools.pulse.internal.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler is used to close jmx connection maintained at user-level
 *
 */
public class LogoutHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {
  private static final Logger logger = LogManager.getLogger();

  public LogoutHandler(String defaultTargetURL) {
    this.setDefaultTargetUrl(defaultTargetURL);
  }

  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    logger.debug("Invoked #LogoutHandler ...");
    GemFireAuthentication gemauthentication = (GemFireAuthentication) authentication;
    if (gemauthentication != null) {
      gemauthentication.getJmxc().close();
      logger.info("#LogoutHandler : Closing GemFireAuthentication JMX Connection...");
    }
    super.onLogoutSuccess(request, response, authentication);
  }

}
