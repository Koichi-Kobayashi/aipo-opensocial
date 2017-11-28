/*
 * Aipo is a groupware program developed by TOWN, Inc.
 * Copyright (C) 2004-2015 TOWN, Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aipo.social.core.config;

import java.util.List;
import java.util.Set;

import org.apache.shindig.auth.AipoSecurityTokenAuthenticationHandler;
import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.common.servlet.ParameterFetcher;
import org.apache.shindig.protocol.DataServiceServletFetcher;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.conversion.BeanXStreamConverter;
import org.apache.shindig.protocol.conversion.xstream.XStreamConfiguration;
import org.apache.shindig.social.core.util.BeanXStreamAtomConverter;
import org.apache.shindig.social.core.util.xstream.XStream081Configuration;
import org.apache.shindig.social.opensocial.service.AppDataHandler;

import com.aipo.social.core.oauth.AipoAuthenticationHandlerProvider;
import com.aipo.social.core.oauth.AipoSecurityTokenAuthenticationHandlerProvider;
import com.aipo.social.opensocial.service.AipoActivityHandler;
import com.aipo.social.opensocial.service.AipoGroupHandler;
import com.aipo.social.opensocial.service.AipoHandler;
import com.aipo.social.opensocial.service.AipoIconsHandler;
import com.aipo.social.opensocial.service.AipoMessageHandler;
import com.aipo.social.opensocial.service.AipoMessageRoomHandler;
import com.aipo.social.opensocial.service.AipoMobileNotificationHandler;
import com.aipo.social.opensocial.service.AipoPersonHandler;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * @SocialApiGuiceModule
 */
public class AipoSocialApiGuiceModule extends AbstractModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(ParameterFetcher.class)
      .annotatedWith(Names.named("AipoDataServiceServlet"))
      .to(DataServiceServletFetcher.class);

    bind(Boolean.class)
      .annotatedWith(
        Names.named(AnonymousAuthenticationHandler.ALLOW_UNAUTHENTICATED))
      .toInstance(Boolean.TRUE);
    bind(XStreamConfiguration.class).to(XStream081Configuration.class);
    bind(BeanConverter.class).annotatedWith(
      Names.named("shindig.bean.converter.xml")).to(BeanXStreamConverter.class);
    bind(BeanConverter.class).annotatedWith(
      Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
    bind(BeanConverter.class).annotatedWith(
      Names.named("shindig.bean.converter.atom")).to(
      BeanXStreamAtomConverter.class);

    bind(new TypeLiteral<List<AuthenticationHandler>>() {
    }).toProvider(AipoAuthenticationHandlerProvider.class);

    bind(new TypeLiteral<List<AipoSecurityTokenAuthenticationHandler>>() {
    }).toProvider(AipoSecurityTokenAuthenticationHandlerProvider.class);

    Multibinder<Object> handlerBinder =
      Multibinder.newSetBinder(binder(), Object.class, Names
        .named("org.apache.shindig.handlers"));
    for (Class<?> handler : getHandlers()) {
      handlerBinder.addBinding().toInstance(handler);
    }
  }

  /**
   *
   * @return
   */
  protected Set<Class<?>> getHandlers() {
    return ImmutableSet.<Class<?>> of(
      AipoMobileNotificationHandler.class,
      AipoHandler.class,
      AipoActivityHandler.class,
      AppDataHandler.class,
      AipoPersonHandler.class,
      AipoGroupHandler.class,
      AipoMessageRoomHandler.class,
      AipoMessageHandler.class,
      AipoIconsHandler.class);
  }
}
