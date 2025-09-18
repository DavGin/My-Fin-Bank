import {type JSX, Suspense} from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from '../pages/Auth/LoginPage'
import RegisterPage from '../pages/Auth/RegisterPage'
import DashboardPage from '../pages/Dashboard/DashboardPage'
import { useAppSelector } from '../app/hooks'

function ProtectedRoute({ children }: { children: JSX.Element }) {
    const token = useAppSelector(state => state.auth.accessToken)
    if (!token) return <Navigate to="/auth/login" replace />
    return children
}

export default function AppRouter() {
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <Routes>
                <Route path="/auth/login" element={<LoginPage />} />
                <Route path="/auth/register" element={<RegisterPage />} />


                <Route path="/" element={<Navigate to="/dashboard" replace />} />
                <Route path="/dashboard" element={
                    <ProtectedRoute>
                        <DashboardPage />
                    </ProtectedRoute>
                } />


                {/* aggiungi altre rotte protette qui (accounts, transactions, investments, loans, profile) */}
            </Routes>
        </Suspense>
    )
}
