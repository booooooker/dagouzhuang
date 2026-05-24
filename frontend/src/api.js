import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const getCurrentUser = () => api.get('/user')
export const endGame = (userId, score, isWin) => api.post('/game/end', null, {
  params: { userId, score, isWin }
})
export const updateNickname = (userId, nickname) => api.put('/user/nickname', null, {
  params: { userId, nickname }
})
export const getLeaderboardByScore = () => api.get('/leaderboard/score')
export const getLeaderboardByMaxScore = () => api.get('/leaderboard/maxscore')
