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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.service.AipoConfigDbService;
import com.aipo.orm.service.TurbineUserDbService;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.FilterOperation;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.aipo.social.core.model.ALMobileNotificationSettingsImpl;
import com.aipo.social.core.model.ALPersonImpl;
import com.aipo.social.opensocial.model.ALMobileNotificationSettings;
import com.aipo.social.opensocial.model.ALPerson;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;

/**
 *
 */
public class AipoPersonService extends AbstractService implements PersonService {

  private final TurbineUserDbService turbineUserDbService;

  private final AipoConfigDbService aipoConfigDbService;

  public static final String CONFIG_MOBILE_NOTIFICATION_PREFIX =
    "saas.mobile.notification.";

  /**
   *
   */
  @Inject
  public AipoPersonService(TurbineUserDbService turbineUserSercice,
      AipoConfigDbService aipoConfigSercice) {
    this.turbineUserDbService = turbineUserSercice;
    this.aipoConfigDbService = aipoConfigSercice;
  }

  /**
   *
   * @param userIds
   * @param groupId
   * @param collectionOptions
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<RestfulCollection<ALPerson>> getPeople(Set<UserId> userIds,
      GroupId groupId, CollectionOptions collectionOptions, Set<String> fields,
      SecurityToken token) throws ProtocolException {

    // TODO: FIELDS

    setUp(token);

    // Search
    SearchOptions options =
      SearchOptions.build().withRange(
        collectionOptions.getMax(),
        collectionOptions.getFirst()).withFilter(
        collectionOptions.getFilter(),
        collectionOptions.getFilterOperation() == null
          ? FilterOperation.equals
          : FilterOperation.valueOf(collectionOptions
            .getFilterOperation()
            .toString()),
        collectionOptions.getFilterValue()).withSort(
        collectionOptions.getSortBy(),
        collectionOptions.getSortOrder() == null
          ? SortOrder.ascending
          : SortOrder.valueOf(collectionOptions.getSortOrder().toString()));

    List<TurbineUser> list = null;
    int totalResults = 0;
    switch (groupId.getType()) {
      case all:
      case friends:
        // /people/{guid}/@all
        // /people/{guid}/@friends
        // {guid} が閲覧できるすべてのユーザーを取得
        // @all = @friends
        list = turbineUserDbService.find(options);
        totalResults = turbineUserDbService.getCount(options);
        break;
      case groupId:
        // /people/{guid}/{groupId}
        // /people/{guid}/{groupId}
        // {guid} が閲覧できるすべてのユーザーで {groupId} グループに所属しているものを取得
        list =
          turbineUserDbService.findByGroupname(groupId.getGroupId(), options);
        totalResults =
          turbineUserDbService.getCountByGroupname(
            groupId.getGroupId(),
            options);
        break;
      case deleted:
        // /people/{guid}/@deleted
        // {guid} が閲覧できる無効なユーザーを取得
        list = Lists.newArrayList();
        break;
      case self:
        // {guid} 自身のユーザー情報を取得
        list = Lists.newArrayList();
        totalResults = 1;
        break;
      default:
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
    }

    List<ALPerson> result = new ArrayList<ALPerson>(list.size());
    for (TurbineUser user : list) {
      result.add(assignPerson(user, fields, token));
    }

    RestfulCollection<ALPerson> restCollection =
      new RestfulCollection<ALPerson>(
        result,
        collectionOptions.getFirst(),
        totalResults,
        collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);

  }

  /**
   *
   * @param id
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<ALPerson> getPerson(UserId id, Set<String> fields,
      SecurityToken token) throws ProtocolException {

    // TODO: FIELDS

    setUp(token);

    String userId = getUserId(id, token);
    TurbineUser user = turbineUserDbService.findByUsername(userId);

    ALPerson person = null;
    if (user != null) {
      person = assignPerson(user, fields, token);
    }

    return ImmediateFuture.newInstance(person);
  }

  protected ALPerson assignPerson(TurbineUser user, Set<String> fields,
      SecurityToken token) {
    String userId =
      new StringBuilder(getOrgId(token) + ":" + user.getLoginName()).toString();
    String displayName =
      new StringBuilder(user.getLastName()).append(" ").append(
        user.getFirstName()).toString();
    Name name = new NameImpl();
    name.setFamilyName(user.getLastName());
    name.setGivenName(user.getFirstName());
    Name nameKana = new NameImpl();
    nameKana.setFamilyName(user.getLastNameKana());
    nameKana.setGivenName(user.getFirstNameKana());
    ALPerson person = new ALPersonImpl(userId, displayName, name, nameKana);
    person.setPhotoModified(user.getPhotoModified());
    person.setHasPhoto("T".equals(user.getHasPhoto())
      || "N".equals(user.getHasPhoto()));
    return person;
  }

  /**
   *
   * @param id
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public InputStream getIcon(UserId id, SecurityToken token)
      throws ProtocolException {

    setUp(token);

    String userId = getUserId(id, token);

    InputStream userIcon = turbineUserDbService.getPhoto(userId);
    if (userIcon == null) {
      throw new AipoProtocolException(AipoErrorCode.ICON_NOT_FOUND);
    }

    return userIcon;
  }

  /**
   *
   * @param userId
   * @param icon
   * @param securityToken
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<Void> putIcon(UserId userId, FormDataItem icon,
      SecurityToken token) throws ProtocolException {
    setUp(token);
    checkSameViewer(userId, token);

    String username = getUserId(userId, token);

    byte[] profileIcon =
      getBytesShrink(
        icon,
        DEF_LARGE_THUMBNAIL_WIDTH,
        DEF_LARGE_THUMBNAIL_HEIGHT,
        false,
        DEF_VALIDATE_WIDTH,
        DEF_VALIDATE_HEIGHT).getShrinkImage();

    byte[] profileIconSmartPhone =
      getBytesShrink(
        icon,
        DEF_NORMAL_THUMBNAIL_WIDTH,
        DEF_NORMAL_THUMBNAIL_HEIGHT,
        false,
        DEF_VALIDATE_WIDTH,
        DEF_VALIDATE_HEIGHT).getShrinkImage();

    turbineUserDbService.setPhoto(username, profileIcon, profileIconSmartPhone);

    return Futures.immediateFuture(null);
  }

  /**
   *
   *
   * @param userId
   * @param securityToken
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<Void> deleteIcon(UserId userId, SecurityToken token)
      throws ProtocolException {
    setUp(token);
    checkSameViewer(userId, token);

    String username = getUserId(userId, token);

    turbineUserDbService.setPhoto(username, null, null);

    return Futures.immediateFuture(null);
  }

  /**
   *
   * @param id
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<RestfulCollection<ALMobileNotificationSettings>> getMobileNotification(
      UserId id, SecurityToken token) throws ProtocolException {

    setUp(token);
    checkSameViewer(id, token);
    String username = getUserId(id, token);

    String userId = getUserId(id, token);

    String notification =
      aipoConfigDbService.get(CONFIG_MOBILE_NOTIFICATION_PREFIX + userId, "A1");
    if (notification == null) {
      throw new AipoProtocolException(AipoErrorCode.NOT_FOUND);
    }

    String mobile = String.valueOf(notification.charAt(0));
    String browse = String.valueOf(notification.charAt(1));

    List<ALMobileNotificationSettings> result =
      new ArrayList<ALMobileNotificationSettings>();

    ALMobileNotificationSettings setting =
      new ALMobileNotificationSettingsImpl();
    String orgId = getOrgId(token);
    setting.setUserId(orgId + ":" + username);
    setting.setMobileNotification(mobile);
    setting.setWhileBrowse(browse);

    result.add(setting);

    RestfulCollection<ALMobileNotificationSettings> restCollection =
      new RestfulCollection<ALMobileNotificationSettings>(result);
    return ImmediateFuture.newInstance(restCollection);
  }

  /**
   * @param userId
   * @param mobileNotification
   * @param token
   * @return
   * @throws ProtocolException
   */

  @Override
  public Future<RestfulCollection<ALMobileNotificationSettings>> putMobileNotification(
      UserId userId, String mobileNotification, SecurityToken token)
      throws ProtocolException {

    setUp(token);

    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    String mobile = String.valueOf(mobileNotification.charAt(0));
    String browse = String.valueOf(mobileNotification.charAt(1));

    if (!("A".equals(mobile) || "F".equals(mobile) || "0".equals(browse) || "1"
      .equals(browse))) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter mobileNotification invalid."));
    }

    aipoConfigDbService.put(
      CONFIG_MOBILE_NOTIFICATION_PREFIX + userId,
      mobileNotification);

    List<ALMobileNotificationSettings> result =
      new ArrayList<ALMobileNotificationSettings>();

    ALMobileNotificationSettings setting =
      new ALMobileNotificationSettingsImpl();
    String orgId = getOrgId(token);
    setting.setUserId(orgId + ":" + username);
    setting.setMobileNotification(mobile);
    setting.setWhileBrowse(browse);

    result.add(setting);

    RestfulCollection<ALMobileNotificationSettings> restCollection =
      new RestfulCollection<ALMobileNotificationSettings>(result);
    return ImmediateFuture.newInstance(restCollection);
  }

}
