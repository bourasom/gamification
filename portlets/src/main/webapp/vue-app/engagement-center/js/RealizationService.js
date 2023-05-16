export function getAllRealizations(fromDate, toDate, earnerIds, sortBy, sortDescending, offset, limit, domainIds) {
  const formData = new FormData();
  if (fromDate) {
    formData.append('fromDate', fromDate);
  }

  if (toDate) {
    formData.append('toDate', toDate);
  }
  if (earnerIds?.length > 0) {
    for (const earnerId of earnerIds) {
      formData.append('earnerIds', earnerId);
    }
  }
  if (sortBy) {
    formData.append('sortBy', sortBy);
  }
  if (sortDescending != null) {
    formData.append('sortDescending', sortDescending);
  }
  if (offset) {
    formData.append('offset', offset);
  }
  if (limit) {
    formData.append('limit', limit);
  }
  if (domainIds?.length > 0) {
    for (const element of domainIds) {
      formData.append('domainIds', element);
    }
  }

  const params = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/realizations/api/allRealizations?returnSize=true&${params}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting realizations');
    }
  });
}

export function updateRealization( id, status, actionLabel, domain, points) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/realizations/api/updateRealizations?realizationId=${id}&status=${status}&actionLabel=${actionLabel || ''}&domain=${domain || ''}&points=${points || 0}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error updating realization status');
    }
  });
}

export function exportFile(fromDate, toDate, earnerIds, domainIds) {
  const formData = new FormData();

  if (fromDate) {
    formData.append('fromDate', fromDate);
  }

  if (toDate) {
    formData.append('toDate', toDate);
  }
  if (earnerIds?.length > 0) {
    for (const earnerId of earnerIds) {
      formData.append('earnerIds', earnerId);
    }
  }
  if (domainIds?.length > 0) {
    for (const element of domainIds) {
      formData.append('domainIds', element);
    }
  }
  formData.append('returnType', 'xlsx');

  const params = new URLSearchParams(formData).toString();

  window.open(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/realizations/api/allRealizations?${params}`, '_blank');
}