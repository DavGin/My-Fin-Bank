// src/features/auth/authSlice.ts
import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'

interface User { nome: string; cognome: string; email: string; username: string;}
interface AuthState { user: User | null; accessToken: string | null }

const initialState: AuthState = {
    user: null,
    accessToken: localStorage.getItem('accessToken') || null,
}

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials(state, action: PayloadAction<{ user: User; accessToken: string }>) {
            state.user = action.payload.user
            state.accessToken = action.payload.accessToken
            localStorage.setItem('accessToken', action.payload.accessToken)
        },
        setUser(state, action: PayloadAction<User>) {
            state.user = action.payload
        },
        logout(state) {
            state.user = null
            state.accessToken = null
            localStorage.removeItem('accessToken')
        },
    },
})

export const { setCredentials, setUser,logout } = authSlice.actions
export default authSlice.reducer
