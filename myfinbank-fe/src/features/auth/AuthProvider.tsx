// src/features/auth/AuthProvider.tsx
import { useEffect } from 'react'
import { useAppDispatch } from '../../app/hooks'
import { setUser, logout } from  '../../features/auth/authSlice'
import { authApi } from '../../api/authApi'

export default function AuthProvider({ children }: { children: React.ReactNode }) {
    const dispatch = useAppDispatch()

    useEffect(() => {
        const token = localStorage.getItem('accessToken')
        if (!token) return

        // Prova a recuperare il profilo
        authApi.registrazione()
            .then(user => {
                dispatch(setUser(user))
            })
            .catch(err => {
                console.error('Auto-login fallito:', err)
                dispatch(logout())
            })
    }, [dispatch])

    return <>{children}</>
}
