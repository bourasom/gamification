<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <exo-drawer
    id="programDetailDrawer"
    ref="drawer"
    v-model="drawer"
    :right="!$vuetify.rtl">
    <template #title>
      <span
        :title="$t('rule.detail.letsSeeWhatToDo')"
        class="text-truncate">
        {{ $t('programs.label.programSummary') }}
      </span>
    </template>
    <template #titleIcons>
      <v-btn
        :href="programLink"
        icon>
        <v-icon size="24">fa-external-link-alt</v-icon>
      </v-btn>
    </template>
    <template v-if="program && drawer" #content>
      <v-card
        class="pa-5"
        transparent
        flat>
        <div class="text-header-title dark-grey-color text-truncate-2">
          {{ program.title }}
        </div>
        <v-avatar
          id="programImageCover"
          height="auto"
          max-width="100%"
          min-width="100%"
          class="align-start flex-grow-0 content-box-sizing border-color rounded position-relative mt-2"
          tile>
          <img
            id="engagementCenterImgSelectorImg"
            :src="program.coverUrl"
            class="full-width"
            role="presentation"
            alt="">
        </v-avatar>
        <div class="subtitle-1 dark-grey-color font-weight-bold mt-6">
          {{ $t('programs.details.label.description') }}
        </div>
        <div
          class="text-color text-wrap text-start text-break rich-editor-content mt-2"
          v-sanitized-html="program.description">
        </div>
        <div class="d-flex flex-column mt-6">
          <div class="d-flex flex-row">
            <div class="subtitle-1 dark-grey-color font-weight-bold flex-start text-start flex-grow-1 flex-shrink-1 text-truncate">
              {{ $t('programs.details.label.audienceSpace') }}
            </div>
            <div class="subtitle-1 dark-grey-color font-weight-bold flex-end text-end flex-grow-0 flex-shrink-0">
              {{ $t('programs.details.label.programOwners') }}
            </div>
          </div>
          <div class="d-flex flex-row">
            <div class="flex-start text-start flex-grow-1 flex-shrink-1 text-truncate">
              <exo-space-avatar
                v-if="program.space"
                :space="program.space"
                :size="32"
                class="text-truncate mt-2"
                popover />
            </div>
            <div class="flex-end text-end flex-grow-0 flex-shrink-0">
              <div v-if="owners.length">
                <engagement-center-avatars-list
                  :avatars="owners"
                  :max-avatars-to-show="3"
                  :avatars-count="ownersCount"
                  :size="25"
                  class="d-flex justify-sm-end pt-2"
                  @open-avatars-drawer="$root.$emit('open-owners-drawer', owners, true)" />
              </div>
              <v-chip v-else class="ms-sm-auto mt-2">
                {{ $t('programs.label.rewardAdmins') }}
              </v-chip>
            </div>
          </div>
        </div>
      </v-card>
      <gamification-rules-overview-widget
        v-if="rules"
        :rules="rules"
        :page-size="rulesLimit"
        :loading="loading"
        :see-all-url="programLink"
        hide-empty-placeholder
        class="mt-n4 mb-4 mx-1">
        <template #title>
          <div class="subtitle-1 dark-grey-color font-weight-bold text-truncate">
            {{ $t('programs.label.programActions') }}
          </div>
        </template>
      </gamification-rules-overview-widget>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    program: null,
    loading: false,
    rules: null,
    rulesLimit: 4,
  }),
  computed: {
    programLink() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/contributions/programs/${this.program.id}`;
    },
    space() {
      return this.program?.space;
    },
    spaceManagers() {
      return this.space?.managers;
    },
    spaceManagersList() {
      return (this.spaceManagers || []).map(owner => ({
        userName: owner
      }));
    },
    addedOwners() {
      return (this.program?.owners || []).filter(owner => !this.program?.space || !this.program?.space?.managers.includes(owner.remoteId))
        .map(owner => ({
          userName: owner.remoteId
        }));
    },
    owners() {
      return this.addedOwners.concat(this.spaceManagersList);
    },
    ownersCount() {
      return this.owners?.length;
    },
  },
  watch: {
    loading() {
      if (this.loading) {
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
  },
  created() {
    this.$root.$on('program-detail-drawer', this.open);
    this.$root.$on('announcement-added', this.retrieveRules);
    this.$root.$on('rule-updated', this.retrieveRules);
    this.$root.$on('rule-deleted', this.retrieveRules);
  },
  methods: {
    open(program) {
      this.program = program;
      this.rules = null;
      this.$refs.drawer.open();
      this.$nextTick()
        .then(() => {
          this.retrieveRules();
          this.collectProgramVisit();
        });
    },
    close() {
      this.$refs.drawer.close();
      this.$nextTick().then(() => this.program = null);
    },
    retrieveRules() {
      if (!this.drawer) {
        return;
      }
      this.loading = true;
      return this.$ruleService.getRules({
        status: 'ENABLED',
        programStatus: 'ENABLED',
        dateFilter: 'ACTIVE',
        programId: this.program.id,
        offset: 0,
        limit: 100,
        expand: 'countRealizations',
        lang: eXo.env.portal.language,
      })
        .then(data => this.rules = data?.rules || [])
        .finally(() => this.loading = false);
    },
    collectProgramVisit() {
      if (this.program?.id) {
        document.dispatchEvent(new CustomEvent('exo-statistic-message', {
          detail: {
            module: 'gamification',
            subModule: 'program',
            userId: eXo.env.portal.userIdentityId,
            userName: eXo.env.portal.userName,
            spaceId: this.spaceId || 0,
            operation: 'viewProgram',
            timestamp: Date.now(),
            parameters: {
              programId: this.programId,
              programTitle: this.programTitle,
              programBudget: this.programBudget,
              programType: this.program.type,
              portalName: eXo.env.portal.portalName,
              portalUri: eXo.env.server.portalBaseURL,
              pageUrl: window.location.pathname,
              pageTitle: eXo.env.portal.pageTitle,
              pageUri: eXo.env.portal.selectedNodeUri,
            },
          }
        }));
      }
    },
  }
};
</script>