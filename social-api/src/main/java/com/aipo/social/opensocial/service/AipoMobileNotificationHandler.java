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
package com.aipo.social.opensocial.service;

import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoPreconditions;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.container.protocol.AipoScope;
import com.aipo.social.opensocial.spi.PersonService;
import com.google.inject.Inject;

/**
 * RPC/REST handler for notification API
 */
@Service(name = "notification")
public class AipoMobileNotificationHandler {

  private final PersonService personService;

  @Inject
  public AipoMobileNotificationHandler(PersonService personService) {
    this.personService = personService;
  }

  /**
   * モバイル通知 <br>
   * <code>
   * GET /notification/mobile
   * </code><br>
   * <code>
   * osapi.mobilenotification.get
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", name = "mobilenotification.get", path = "/mobile")
  public Future<?> getNotification(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      return personService.getMobileNotification(
        userIds.iterator().next(),
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * モバイル通知設定更新 <br>
   * <code>
   * PUT /notification/mobile
   * </code><br>
   * <code>
   * osapi.mobilenotification.put
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "PUT", name = "mobilenotification.put", path = "/mobile")
  public Future<?> putNotification(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String mobileNotification = request.getParameter("mobileNotification");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      return personService.putMobileNotification(
        userIds.iterator().next(),
        mobileNotification,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }
}
