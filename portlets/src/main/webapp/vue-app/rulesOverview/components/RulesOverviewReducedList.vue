<!--
  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2023 Meeds Association contact@meeds.io

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <gamification-overview-widget
    v-if="hasValidRules"
    :see-all-url="actionsPageURL"
    extra-class="pa-0 justify-space-between height-auto">
    <template #title>
      {{ $t('gamification.overview.challengesOverviewTitle') }}
    </template>
    <template #content>
      <template v-if="endingRulesCount">
        <div class="d-flex align-center mx-4">
          <span class="me-2">{{ $t('gamification.overview.endingActionsTitle') }}</span>
          <v-divider />
        </div>
        <gamification-rules-overview-item
          v-for="rule in endingRulesToDisplay"
          :key="rule.id"
          :rule="rule"
          dense />
      </template>
      <template v-if="activeRulesCount">
        <div v-if="!hasAvailableRulesOnly" class="d-flex align-center mx-4">
          <span class="me-2">{{ $t('gamification.overview.availableActionsTitle') }}</span>
          <v-divider />
        </div>
        <gamification-rules-overview-item
          v-for="rule in activeRulesToDisplay"
          :key="rule.id"
          :rule="rule"
          :dense="!hasAvailableRulesOnly" />
      </template>
      <template v-if="upcomingRulesCount">
        <div class="d-flex align-center mx-4">
          <span class="me-2">{{ $t('gamification.overview.upcomingActionsTitle') }}</span>
          <v-divider />
        </div>
        <gamification-rules-overview-item
          v-for="rule in upcomingRulesToDisplay"
          :key="rule.id"
          :rule="rule"
          dense />
      </template>
      <template v-if="remainingCount">
        <gamification-overview-widget-empty-row
          v-for="index in remainingCount"
          :key="index"
          class="flex" />
      </template>
    </template>
  </gamification-overview-widget>
  <gamification-overview-widget
    v-else
    :loading="loading">
    <template #title>
      {{ $t('gamification.overview.emptyChallengesOverviewTitle') }}
    </template>
    <template #content>
      <gamification-overview-widget-row
        v-show="!loading"
        class="my-auto">
        <template #icon>
          <v-icon color="secondary" size="55px">fas fa-rocket</v-icon>
        </template>
        <template #content>
          <span v-sanitized-html="emptySummaryText"></span>
        </template>
      </gamification-overview-widget-row>
    </template>
  </gamification-overview-widget>
</template>
<script>
export default {
  data: () => ({
    actionsPageURL: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/contributions/actions`,
    pageSize: 4,
    loading: true,
    rules: [],
    activeRules: [],
    upcomingRules: [],
    endingRules: [],
    weekInMs: 604800000,
  }),
  computed: {
    emptySummaryText() {
      return this.$t('gamification.overview.challengesOverviewSummary', {
        0: `<a class="primary--text font-weight-bold" href="${this.actionsPageURL}">`,
        1: '</a>',
      });
    },
    endingRulesToDisplay() {
      if (!this.endingRules?.length) {
        return [];
      }
      return this.endingRules
        .filter(r => r.endDate && (new Date(r.endDate).getTime() - Date.now()) < this.weekInMs)
        .slice(0, this.pageSize - 1);
    },
    endingRulesCount() {
      return this.endingRulesToDisplay.length;
    },
    upcomingRulesToDisplay() {
      if (this.endingRulesCount || !this.upcomingRules?.length) {
        return [];
      }
      return this.upcomingRules
        .filter(r => r.startDate && (new Date(r.startDate).getTime() - Date.now()) < this.weekInMs)
        .slice(0, this.pageSize - 1);
    },
    upcomingRulesCount() {
      return this.upcomingRulesToDisplay.length;
    },
    activeRulesToDisplay() {
      if (!this.activeRules?.length) {
        return [];
      }
      return this.activeRules
        .filter(r => !this.endingRulesToDisplay.find(rule => rule.id === r.id))
        .slice(0, this.pageSize - this.endingRulesCount - this.upcomingRulesCount);
    },
    activeRulesCount() {
      return this.activeRulesToDisplay.length;
    },
    validRulesCount() {
      return this.activeRulesCount + this.upcomingRulesCount + this.endingRulesCount;
    },
    hasValidRules() {
      return this.validRulesCount > 0;
    },
    hasAvailableRulesOnly() {
      return this.activeRulesCount && !this.endingRulesCount && !this.upcomingRulesCount;
    },
    remainingCount() {
      return this.pageSize - this.validRulesCount;
    },
  },
  created() {
    this.$root.$on('announcement-added', this.retrieveRules);
    this.$root.$on('rule-updated', this.retrieveRules);
    this.$root.$on('rule-deleted', this.retrieveRules);
    this.retrieveRules();
  },
  beforeDestroy() {
    this.$root.$off('announcement-added', this.retrieveRules);
    this.$root.$off('rule-updated', this.retrieveRules);
    this.$root.$off('rule-deleted', this.retrieveRules);
  },
  methods: {
    retrieveRules() {
      this.loading = true;
      Promise.all([
        this.retrieveEndingRules(),
        this.retrieveActiveRules(),
        this.retrieveUpcomingRules(),
      ]).finally(() => this.loading = false);
    },
    retrieveEndingRules() {
      return this.$ruleService.getRules({
        status: 'ENABLED',
        programStatus: 'ENABLED',
        dateFilter: 'STARTED_WITH_END',
        offset: 0,
        limit: this.pageSize,
        sortBy: 'endDate',
        sortDescending: false,
        expand: 'countRealizations',
        lang: eXo.env.portal.language,
        returnSize: false,
      }).then(result => this.endingRules = result?.rules || []);
    },
    retrieveActiveRules() {
      return this.$ruleService.getRules({
        status: 'ENABLED',
        programStatus: 'ENABLED',
        dateFilter: 'STARTED',
        offset: 0,
        limit: this.pageSize,
        sortBy: 'createdDate',
        sortDescending: true,
        expand: 'countRealizations',
        lang: eXo.env.portal.language,
        returnSize: false,
      }).then(result => this.activeRules = result?.rules || []);
    },
    retrieveUpcomingRules() {
      return this.$ruleService.getRules({
        status: 'ENABLED',
        programStatus: 'ENABLED',
        dateFilter: 'UPCOMING',
        offset: 0,
        limit: this.pageSize,
        sortBy: 'startDate',
        sortDescending: false,
        expand: 'countRealizations',
        lang: eXo.env.portal.language,
        returnSize: false,
      }).then(result => this.upcomingRules = result?.rules || []);
    },
  },
};
</script>