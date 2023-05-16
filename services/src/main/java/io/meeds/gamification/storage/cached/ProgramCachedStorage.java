/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association
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

package io.meeds.gamification.storage.cached;

import static io.meeds.gamification.utils.Utils.POST_CREATE_RULE_EVENT;
import static io.meeds.gamification.utils.Utils.POST_DELETE_RULE_EVENT;
import static io.meeds.gamification.utils.Utils.POST_UPDATE_RULE_EVENT;

import java.io.Serializable;
import java.util.List;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.upload.UploadService;

import io.meeds.gamification.constant.EntityStatusType;
import io.meeds.gamification.dao.ProgramDAO;
import io.meeds.gamification.dao.RuleDAO;
import io.meeds.gamification.model.ProgramDTO;
import io.meeds.gamification.model.filter.ProgramFilter;
import io.meeds.gamification.storage.ProgramStorage;
import io.meeds.gamification.storage.cached.model.CacheKey;

public class ProgramCachedStorage extends ProgramStorage {

  private static final int                               DOMAIN_ID_CONTEXT      = 0;

  private static final int                               ALL_DOMAIN_CONTEXT     = 2;

  private static final int                               DOMAIN_ENABLED_CONTEXT = 3;

  private static final String                            DOMAIN_CACHE_NAME      = "gamification.domain";

  private FutureExoCache<Serializable, Object, CacheKey> domainFutureCache;

  public ProgramCachedStorage(FileService fileService,
                              UploadService uploadService,
                              ProgramDAO programDAO,
                              RuleDAO ruleDAO,
                              CacheService cacheService,
                              ListenerService listenerService) {
    super(fileService, uploadService, programDAO, ruleDAO);
    ExoCache<Serializable, Object> domainCache = cacheService.getCacheInstance(DOMAIN_CACHE_NAME);
    Loader<Serializable, Object, CacheKey> domainLoader = new Loader<Serializable, Object, CacheKey>() {
      @Override
      public Object retrieve(CacheKey context, Serializable key) throws Exception {
        if (context.getContext() == DOMAIN_ID_CONTEXT) {
          return ProgramCachedStorage.super.getDomainById(context.getId());
        } else if (context.getContext() == ALL_DOMAIN_CONTEXT) {
          return ProgramCachedStorage.super.getDomainsByFilter(context.getDomainFilter(),
                                                               context.getOffset(),
                                                               context.getLimit());
        } else if (context.getContext() == DOMAIN_ENABLED_CONTEXT) {
          return ProgramCachedStorage.super.getEnabledDomains();
        } else {
          throw new IllegalStateException("Unknown context id " + context);
        }
      }
    };
    this.domainFutureCache = new FutureExoCache<>(domainLoader, domainCache);
    listenerService.addListener(POST_CREATE_RULE_EVENT, new RuleUpdatedListener());
    listenerService.addListener(POST_DELETE_RULE_EVENT, new RuleUpdatedListener());
    listenerService.addListener(POST_UPDATE_RULE_EVENT, new RuleUpdatedListener());
  }

  @Override
  public ProgramDTO saveDomain(ProgramDTO domain) {
    try {
      domain = super.saveDomain(domain);
      return domain;
    } finally {
      clearCache();
    }
  }

  @Override
  public ProgramDTO getDomainById(Long id) {
    CacheKey key = new CacheKey(DOMAIN_ID_CONTEXT, id);
    return (ProgramDTO) this.domainFutureCache.get(key, key.hashCode());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ProgramDTO> getEnabledDomains() {
    ProgramFilter domainFilter = new ProgramFilter();
    domainFilter.setEntityStatusType(EntityStatusType.ENABLED);
    CacheKey key = new CacheKey(DOMAIN_ENABLED_CONTEXT, domainFilter);

    return (List<ProgramDTO>) this.domainFutureCache.get(key, key.hashCode());
  }

  @Override
  public void clearCache() {
    domainFutureCache.clear();
  }

  public class RuleUpdatedListener extends Listener<Object, String> {
    @Override
    public void onEvent(Event<Object, String> event) throws Exception {
      clearCache();
    }
  }

}