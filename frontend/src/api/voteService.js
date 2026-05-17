import api from './api';

export const getVotesByActivity = async (activityId) => {
  const response = await api.get(`/votes/activity/${activityId}`);
  return response.data;
};

export const createVote = async (voteData) => {
  const response = await api.post('/votes', voteData);
  return response.data;
};

export const deleteVote = async (voteId) => {
  await api.delete(`/votes/${voteId}`);
};
