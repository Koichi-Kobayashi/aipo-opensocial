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
package com.aipo.social.opensocial.spi;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.social.opensocial.model.ALMobileNotificationSettings;
import com.aipo.social.opensocial.model.ALPerson;

public interface PersonService {

  public Future<RestfulCollection<ALPerson>> getPeople(Set<UserId> paramSet,
      GroupId paramGroupId, CollectionOptions paramCollectionOptions,
      Set<String> paramSet1, SecurityToken paramSecurityToken)
      throws ProtocolException;

  public Future<ALPerson> getPerson(UserId paramUserId, Set<String> paramSet,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public InputStream getIcon(UserId paramUserId,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public Future<Void> putIcon(UserId paramUserId, FormDataItem icon,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public Future<Void> deleteIcon(UserId paramUserId,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public Future<RestfulCollection<ALMobileNotificationSettings>> getMobileNotification(
      UserId paramUserId, SecurityToken paramSecurityToken)
      throws ProtocolException;

  public Future<RestfulCollection<ALMobileNotificationSettings>> putMobileNotification(
      UserId paramUserId, String notification, SecurityToken paramSecurityToken)
      throws ProtocolException;
}
