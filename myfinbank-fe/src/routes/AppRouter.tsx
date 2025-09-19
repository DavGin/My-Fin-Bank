import {type JSX, Suspense} from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from '../pages/Auth/LoginPage'
import DashboardPage from '../pages/Dashboard/DashboardPage'
import { useAppSelector } from '../app/hooks'
import AdminRegisterPage from "../pages/Auth/AdminRegisterPage.tsx";
import UserRegisterPage from "../pages/Auth/UserRegisterPage.tsx";
import DashboardLayout from "../features/dashboard/DashboardLayout.tsx";
import ContiPage from "../pages/Conti/ContiPage.tsx";
import ProfilePage from "../pages/Profile/ProfilePage.tsx";

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
                <Route path="/login" element={<LoginPage />} />
                <Route path="/auth/register" element={<UserRegisterPage />} />
                <Route path="/auth/admin/register" element={<AdminRegisterPage />} />
                <Route
                    path="/"
                    element={
                        <ProtectedRoute>
                            <DashboardLayout />
                        </ProtectedRoute>
                    }
                >
                    <Route index element={<DashboardPage />} />
                    <Route path="/conti" element={<ContiPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                </Route>
                <Route path="/" element={<Navigate to="/auth/login" replace />} />
            </Routes>
        </Suspense>
    )
}

