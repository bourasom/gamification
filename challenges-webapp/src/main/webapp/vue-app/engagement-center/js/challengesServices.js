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

export function canAddChallenge() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/challenges/canAddChallenge`, {
    headers: {
      'Content-Type': 'text/plain'
    },
    method: 'GET'
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Server indicates an error while sending request');
    }
  });
}

export function saveChallenge(challenge) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/challenges/`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(challenge),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error saving challenge');
    }
  });
}

export function updateChallenge(challenge) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/challenges/`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(challenge),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error updating challenge');
    }
  });
}

export function getAllChallengesByUser(term , offset, limit, announcements, domainId, groupByDomain, filter) {
  const formData = new FormData();
  if (term) {
    formData.append('term', term);
  }
  if (offset) {
    formData.append('offset', offset);
  }
  if (limit) {
    formData.append('limit', limit);
  }
  if (announcements) {
    formData.append('announcements', announcements);
  }
  if (domainId) {
    formData.append('domainId', domainId);
  }
  if (groupByDomain) {
    formData.append('groupByDomain', groupByDomain);
  }
  if (filter) {
    formData.append('filter', filter);
  }
  const params = new URLSearchParams(formData).toString();

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/challenges?${params}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting challenges');
    }
  });
}

export function getAllAnnouncementsByChallenge(challengeId, offset, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/announcement/api/ByChallengeId/${challengeId}?offset=${offset || 0}&limit=${limit || 10}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting challenges');
    }
  });
}

export function saveAnnouncement(announcement) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/announcement/api/addAnnouncement`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(announcement),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error(resp.status);
    }
  });
}

export function getChallengeById(id) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/challenges/${id}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      return null;
    }
  });
}

export function getAllDomains() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/domains`, {
    headers: {
      'Content-Type': 'text/plain'
    },
    method: 'GET'
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Server indicates an error while sending request');
    }
  });
}


export function deleteChallenge(challengeId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/challenges/${challengeId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then((resp) => {
    if (!resp || !resp.ok) {
      throw new Error(resp.status);
    }
  });
}