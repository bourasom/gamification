/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.meeds.gamification.rest.builder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.gamification.model.ProgramDTO;
import io.meeds.gamification.model.UserInfo;
import io.meeds.gamification.model.UserInfoContext;
import io.meeds.gamification.rest.model.ProgramRestEntity;
import io.meeds.gamification.service.ProgramService;
import io.meeds.gamification.utils.Utils;

public class ProgramBuilder {

  private static final Log LOG = ExoLogger.getLogger(ProgramBuilder.class);

  private ProgramBuilder() {
    // Class with static methods
  }

  public static ProgramRestEntity toRestEntity(ProgramService programService,
                                               ProgramDTO program,
                                               String username) {
    if (program == null) {
      return null;
    }
    return new ProgramRestEntity(program.getId(), // NOSONAR
                                 program.getTitle(),
                                 program.getDescription(),
                                 program.getAudienceId(),
                                 program.getPriority(),
                                 program.getCreatedBy(),
                                 program.getCreatedDate(),
                                 program.getLastModifiedBy(),
                                 program.getLastModifiedDate(),
                                 program.isDeleted(),
                                 program.isEnabled(),
                                 program.getBudget(),
                                 program.getType(),
                                 null,
                                 program.getCoverFileId(),
                                 program.getCoverUrl(),
                                 program.getOwnerIds(),
                                 program.getRulesTotalScore(),
                                 program.getAudienceId() > 0 ? Utils.getSpaceById(String.valueOf(program.getAudienceId())) : null,
                                 toUserContext(programService, program, username),
                                 getProgramOwnersByIds(program.getOwnerIds(), program.getAudienceId()));
  }

  public static List<ProgramRestEntity> toRestEntities(ProgramService programService,
                                                       List<ProgramDTO> domains,
                                                       String username) {
    return domains.stream().map(program -> toRestEntity(programService, program, username)).toList();
  }

  private static List<UserInfo> getProgramOwnersByIds(Set<Long> ids, long spaceId) {
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(String.valueOf(spaceId));
    return ids.stream()
              .distinct()
              .map(id -> {
                try {
                  Identity identity = identityManager.getIdentity(String.valueOf(id));
                  if (identity != null
                      && OrganizationIdentityProvider.NAME.equals(identity.getProviderId())
                      && !spaceService.isManager(space, identity.getRemoteId())
                      && spaceService.isMember(space, identity.getRemoteId())) {
                    return toUserInfo(identity);
                  }
                } catch (Exception e) {
                  LOG.warn("Error when getting domain owner with id {}. Ignore retrieving identity information", id, e);
                }
                return null;
              })
              .filter(Objects::nonNull)
              .toList();
  }

  public static UserInfoContext toUserContext(ProgramService programService,
                                              ProgramDTO program,
                                              String username) {
    UserInfoContext userContext = new UserInfoContext();

    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    mapUserInfo(userContext, identity);
    if (program != null) {
      long spaceId = program.getAudienceId();
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceById(String.valueOf(spaceId));
      if (space != null) {
        boolean isSuperManager = spaceService.isSuperManager(username);
        boolean isManager = isSuperManager || spaceService.isManager(space, username);
        boolean isMember = isManager || spaceService.isMember(space, username);
        boolean isRedactor = isManager || spaceService.isRedactor(space, username);
        userContext.setManager(isManager);
        userContext.setMember(isMember);
        userContext.setRedactor(isRedactor);
        userContext.setCanEdit(programService.isProgramOwner(program.getId(), username));
      }
    }
    return userContext;
  }

  private static UserInfo toUserInfo(Identity identity) {
    UserInfo userInfo = new UserInfo();
    mapUserInfo(userInfo, identity);
    return userInfo;
  }

  private static <E extends UserInfo> void mapUserInfo(E userInfo, Identity identity) {
    userInfo.setAvatarUrl(identity.getProfile().getAvatarUrl());
    userInfo.setFullName(identity.getProfile().getFullName());
    userInfo.setRemoteId(identity.getRemoteId());
    userInfo.setId(identity.getId());
  }

}
