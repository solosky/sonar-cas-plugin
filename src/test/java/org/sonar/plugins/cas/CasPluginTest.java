/*
 * Sonar CAS Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cas;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@Ignore
public class CasPluginTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void enable_extensions_if_cas_realm_is_enabled() {
    final Configuration configuration = new ConfigurationBridge(new MapSettings()
            .setProperty("sonar.security.realm", "cas")
            .setProperty("sonar.authenticator.createUsers", "true")
            .setProperty("sonar.cas.protocol", "cas2")
    );

    final List<Object> extensions = new CasPlugin(configuration).collectExtensions();

    assertThat(extensions).hasSize(4);
    assertThat(extensions).doesNotHaveDuplicates();
    // assertThat(extensions).contains(Cas2AuthenticationFilter.class);
  }

  @Test
  public void enable_cas1_extensions() {
    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.security.realm", "cas")
      .setProperty("sonar.authenticator.createUsers", "true")
      .setProperty("sonar.cas.protocol", "cas1")
    );
    final List<Object> extensions = new CasPlugin(configuration).collectExtensions();

    assertThat(extensions).hasSize(4);
    assertThat(extensions).doesNotHaveDuplicates();
    // assertThat(extensions).contains(Cas1AuthenticationFilter.class);
  }

  @Test
  public void enable_saml11_extensions() {
    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.security.realm", "cas")
      .setProperty("sonar.authenticator.createUsers", "true")
      .setProperty("sonar.cas.protocol", "saml11")
    );
    final List<Object> extensions = new CasPlugin(configuration).collectExtensions();

    assertThat(extensions).hasSize(4);
    assertThat(extensions).doesNotHaveDuplicates();
    // assertThat(extensions).contains(Saml11AuthenticationFilter.class);
  }

  @Test
  public void fail_if_unknown_protocol() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Unknown CAS protocol");

    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.security.realm", "cas")
      .setProperty("sonar.authenticator.createUsers", "true")
      .setProperty("sonar.cas.protocol", "other")
    );

    new CasPlugin(configuration).collectExtensions();
  }

  @Test
  public void property_createUsers_must_be_true() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Property sonar.authenticator.createUsers must be set to true");

    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.security.realm", "cas")
      .setProperty("sonar.authenticator.createUsers", "false")
      .setProperty("sonar.cas.protocol", "saml11")
    );

    new CasPlugin(configuration).collectExtensions();
  }

  @Test
  public void fail_if_missing_protocol() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Missing CAS protocol");

    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.security.realm", "cas")
      .setProperty("sonar.authenticator.createUsers", "true")
    );
    new CasPlugin(configuration).collectExtensions();
  }

  @Test
  public void disable_extensions_if_default_realm() {
    final Configuration configuration = new ConfigurationBridge(new MapSettings());
    final List<Object> extensions = new CasPlugin(configuration).collectExtensions();

    assertThat(extensions).isEmpty();
  }

  @Test
  public void disable_extensions_if_cas_realm_is_disabled() {
    final Configuration configuration = new ConfigurationBridge(new MapSettings()
            .setProperty("sonar.security.realm", "LDAP")
    );
    final List<Object> extensions = new CasPlugin(configuration).collectExtensions();

    assertThat(extensions).isEmpty();
  }


//  @Test
//  public void should_disable_logout_by_default() {
//    final Configuration configuration = new ConfigurationBridge(new MapSettings()
//      .setProperty("sonar.security.realm", "cas")
//      .setProperty("sonar.authenticator.createUsers", "true")
//      .setProperty("sonar.cas.protocol", "saml11")
//    );
//    final List<Object> extensions = new CasPlugin(configuration).collectExtensions();
//
//    assertThat(extensions).excludes(CasLogoutRequestFilter.class);
//    assertThat(extensions).excludes(SonarLogoutRequestFilter.class);
//  }
//
//  @Test
//  public void should_enable_logout_by_default() {
//    final Configuration configuration = new ConfigurationBridge(new MapSettings()
//      .setProperty("sonar.security.realm", "cas")
//      .setProperty("sonar.authenticator.createUsers", "true")
//      .setProperty(SonarLogoutRequestFilter.PROPERTY_CAS_LOGOUT_URL, "http://localhost:8080/cas/logout")
//      .setProperty("sonar.cas.protocol", "saml11")
//    );
//
//    final List<Object> extensions = new CasPlugin(configuration).collectExtensions();
//
//    assertThat(extensions).contains(CasLogoutRequestFilter.class, SonarLogoutRequestFilter.class);
//  }
}
