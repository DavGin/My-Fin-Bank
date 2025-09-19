// src/api/axiosClient.ts
import axios, { AxiosError } from 'axios'
import { store } from '../app/store'
import { logout, setCredentials } from '../features/auth/authSlice'
import { authApi } from './authApi'

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    headers: { 'Content-Type': 'application/json' },
    timeout: 30000,
    withCredentials: true, // 🔑 cookie HttpOnly per refreshToken
})

// Add accessToken to every request
api.interceptors.request.use((config) => {
    const state = store.getState()
    const token = state.auth.accessToken
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
})

// Response interceptor for handling 401
let isRefreshing = false
let failedQueue: any[] = []

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error)
        } else {
            prom.resolve(token)
        }
    })
    failedQueue = []
}

api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest: any = error.config

        // Se 401 e non già in refresh
        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                // Accoda la richiesta finché refresh non termina
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject })
                })
                    .then((token) => {
                        originalRequest.headers['Authorization'] = `Bearer ${token}`
                        return api(originalRequest)
                    })
                    .catch((err) => Promise.reject(err))
            }

            originalRequest._retry = true
            isRefreshing = true

            try {
                const data = await authApi.refresh() // cookie HttpOnly inviato in automatico
                const newToken = data.accessToken

                store.dispatch(setCredentials({ user: store.getState().auth.user!, accessToken: newToken }))

                processQueue(null, newToken)

                originalRequest.headers['Authorization'] = `Bearer ${newToken}`
                return api(originalRequest)
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
