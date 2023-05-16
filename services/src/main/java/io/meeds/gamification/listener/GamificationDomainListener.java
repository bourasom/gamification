/*
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
package io.meeds.gamification.listener;

import static io.meeds.gamification.service.ProgramService.GAMIFICATION_DOMAIN_DELETE_LISTENER;
import static io.meeds.gamification.service.ProgramService.GAMIFICATION_DOMAIN_DISABLE_LISTENER;
import static io.meeds.gamification.service.ProgramService.GAMIFICATION_DOMAIN_ENABLE_LISTENER;

import java.util.List;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.gamification.model.BadgeDTO;
import io.meeds.gamification.model.ProgramDTO;
import io.meeds.gamification.model.RuleDTO;
import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.BadgeService;
import io.meeds.gamification.service.ProgramService;
import io.meeds.gamification.service.RuleService;

public class GamificationDomainListener extends Listener<ProgramDTO, String> {

  private static final Log LOG = ExoLogger.getLogger(GamificationDomainListener.class);

  protected ProgramService programService;

  protected RuleService    ruleService;

  protected BadgeService   badgeService;

  public GamificationDomainListener(ProgramService programService,
                                    RuleService ruleService,
                                    BadgeService badgeService) {
    this.programService = programService;
    this.ruleService = ruleService;
    this.badgeService = badgeService;
  }

  @Override
  public void onEvent(Event<ProgramDTO, String> event) throws Exception { // NOSONAR
    ProgramDTO program = event.getSource();
    String username = event.getData();
    String action = event.getEventName();
    RuleFilter ruleFilter = new RuleFilter();
    ruleFilter.setDomainId(program.getId());
    List<RuleDTO> rules = ruleService.getRules(ruleFilter, 0, -1);
    List<BadgeDTO> badges = badgeService.findBadgesByDomain(program.getId());
    switch (action) {
    case GAMIFICATION_DOMAIN_DELETE_LISTENER:
      for (RuleDTO rule : rules) {
        if (!rule.isDeleted()) {
          ruleService.deleteRuleById(rule.getId(), username);
        }
      }
      for (BadgeDTO badge : badges) {
        badge.setEnabled(false);
        badge.setProgram(null);
        badgeService.updateBadge(badge);
      }
      break;
    case GAMIFICATION_DOMAIN_DISABLE_LISTENER:
      for (RuleDTO rule : rules) {
        if (!rule.isDeleted()) {
          rule.setEnabled(false);
          ruleService.updateRule(rule);
        }
      }
      for (BadgeDTO badge : badges) {
        badge.setEnabled(false);
        badgeService.updateBadge(badge);
      }
      break;
    case GAMIFICATION_DOMAIN_ENABLE_LISTENER:
      for (RuleDTO rule : rules) {
        if (!rule.isDeleted()) {
          rule.setEnabled(true);
          ruleService.updateRule(rule);
        }
      }
      for (BadgeDTO badge : badges) {
        badge.setEnabled(true);
        badgeService.updateBadge(badge);
      }
      break;
    default:
      LOG.warn("Unknown  triggered action name {}", action);
      break;
    }
  }
}