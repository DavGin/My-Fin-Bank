// src/api/authApi.ts
import api from './axiosClient'

export const authApi = {
    login: (payload: { username: string; password: string }) =>
        api.post('/auth/login', payload).then(res => res.data),
    register: (payload: any) => api.post('/auth/register', payload).then(res => res.data),
    refresh: () =>
        api.post('/auth/refresh').then(res => res.data),}
