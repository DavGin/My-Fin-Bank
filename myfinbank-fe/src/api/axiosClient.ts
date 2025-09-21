import axios, { AxiosError } from 'axios'
import { store } from '../app/store'
import { logout, setCredentials } from '../features/auth/authSlice'
import { authApi } from './authApi'

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    headers: { 'Content-Type': 'application/json' },
    timeout: 30000,
    withCredentials: true,
})

// Logging utility
const log = (message: string, data?: any) => {
    console.log(`[AxiosClient] ${message}`, data || '')
}

// Add accessToken to every request
api.interceptors.request.use((config) => {
    console.log('Request data:', config.data) // Logga i dati della richiesta
    const state = store.getState()
    const token = state.auth.accessToken
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
        log('Authorization header set', { token })
    } else {
        log('No token found in state')
    }
    return config
}, (error) => {
    console.error('Request error:', error)
    log('Request error intercepted', error)
    return Promise.reject(error)
})

// Response interceptor for handling 401
let isRefreshing = false
let failedQueue: any[] = []

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) {
            reject(error)
        } else {
            resolve(token)
        }
    })
    failedQueue = []
    log('Processed failedQueue', { error, token })
}

api.interceptors.response.use(
    (response) => {
        log('Response received', response)
        return response
    },
    async (error: AxiosError) => {
        const originalRequest: any = error.config
        log('Response error intercepted', { error, originalRequest })

        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                log('Adding request to failedQueue as refresh is in progress')
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject })
                })
                    .then((token) => {
                        originalRequest.headers['Authorization'] = `Bearer ${token}`
                        log('Retrying original request with new token', { token })
                        return api(originalRequest)
                    })
                    .catch((err) => {
                        log('Retry failed', err)
                        return Promise.reject(err)
                    })
            }

            originalRequest._retry = true
            isRefreshing = true

            try {
                log('Performing token refresh')
                const data = await authApi.refresh() 
                const newToken = data.accessToken

                store.dispatch(setCredentials({ user: store.getState().auth.user!, accessToken: newToken }))
                log('New token obtained and credentials updated', { newToken })

                processQueue(null, newToken)

                originalRequest.headers['Authorization'] = `Bearer ${newToken}`
                log('Retrying original request', { originalRequest })
                return api(originalRequest)
            } catch (err) {
                log('Error during token refresh', err)
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
