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
import ContoDettaglioPage from "../pages/Conti/ContoDettaglioPage.tsx";
import OperazioniPage from "../pages/Transazioni/OperazioniPage.tsx";
import MutuiPage from "../pages/Mutui/MutuiPage.tsx";
import AdminMutuiPage from "../pages/Mutui/AdminMutuiPage.tsx";
import MutuoDetailPage from "../pages/Mutui/MutuiDetailPage.tsx";
import MutuoSimulationPage from "../pages/Mutui/MutuoSimulationPage.tsx";
import InvestimentiPage from "../pages/Investimenti/InvestimentiPage.tsx";
import InvestimentoDetailPage from "../pages/Investimenti/InvestimentoDetailsPage.tsx";
import SimulazioneInvestimentoPage from "../pages/Investimenti/InvestimentiSimulationPage.tsx";

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
                <Route path="/" element={<Navigate to="/auth/login" replace />} />
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
                    <Route
                        path="/conti/findByNumeroConto/:numeroConto"
                        element={
                            <ProtectedRoute>
                                <ContoDettaglioPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route index element={<DashboardPage />} />
                    <Route path="/conti" element={<ContiPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                    <Route path="/operazioni" element={<OperazioniPage />} />
                    <Route path="/mutui" element={<MutuiPage />} />
                    <Route path="/admin/mutui" element={<AdminMutuiPage />} />
                    <Route path="/dettaglioRate" element={<MutuoDetailPage />} />
                    <Route path="/simulazioneMutui" element={<MutuoSimulationPage />} />
                    <Route path="/investimenti" element={<InvestimentiPage />} />
                    <Route path="investimenti/:identificativo/:durataMesi" element={<InvestimentoDetailPage />} />
                    <Route path="/investimenti/simulazione" element={<SimulazioneInvestimentoPage />} />
                </Route>
                <Route path="/" element={<Navigate to="/auth/login" replace />} />
            </Routes>
        </Suspense>
    )
}

