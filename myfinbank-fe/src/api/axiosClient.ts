// src/api/axiosClient.ts
import axios, { AxiosError } from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { store } from '../app/store'
import { setCredentials, logout } from '../features/auth/authSlice'
import { authApi } from './authApi'

// flag per evitare più refresh contemporanei
let isRefreshing = false
let failedQueue: Array<{
    resolve: (value?: any) => void
    reject: (error: any) => void
    config: InternalAxiosRequestConfig
}> = []

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error)
        } else {
            if (prom.config.headers) {
                prom.config.headers['Authorization'] = `Bearer ${token}`
            }
            prom.resolve(axios(prom.config))
        }
    })
    failedQueue = []
}

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    headers: { 'Content-Type': 'application/json' },
    timeout: 15000,
    withCredentials: true, // 🔑 manda automaticamente cookie HttpOnly
})

// ➤ Interceptor per request → aggiunge accessToken se presente
api.interceptors.request.use(
    (config) => {
        const state: any = store.getState()
        const token = state.auth.accessToken || localStorage.getItem('accessToken')
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// ➤ Interceptor per response → gestisce 401
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const origConfig = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

        if (error.response?.status === 401 && !origConfig._retry) {
            if (origConfig.url?.includes('/auth/refresh')) {
                // se fallisce il refresh stesso → logout
                store.dispatch(logout())
                return Promise.reject(error)
            }

            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject, config: origConfig })
                })
            }

            origConfig._retry = true
            isRefreshing = true

            try {
                // chiamata al backend: refresh token è nel cookie HttpOnly
                const r = await authApi.refresh()
                const newAccessToken = r.accessToken || r.access_token

                // aggiorna Redux e localStorage
                const state: any = store.getState()
                store.dispatch(
                    setCredentials({
                        user: state.auth.user,
                        accessToken: newAccessToken,
                    })
                )

                processQueue(null, newAccessToken)

                if (origConfig.headers) {
                    origConfig.headers['Authorization'] = `Bearer ${newAccessToken}`
                }
                return api(origConfig)
            } catch (err) {
                processQueue(err, null)
                store.dispatch(logout())
                return Promise.reject(err)
            } finally {
                isRefreshing = false
            }
        }

        return Promise.reject(error)
    }
)

export default api
