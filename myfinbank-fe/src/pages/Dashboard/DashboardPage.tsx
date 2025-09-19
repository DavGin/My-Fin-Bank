// src/features/dashboard/DashboardPage.tsx
import { useAppSelector } from '../../app/hooks'
import { Typography, Box } from '@mui/material'

export default function DashboardPage() {
    const user = useAppSelector(state => state.auth.user)

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Benvenuto, {user?.nome} {user?.cognome}
            </Typography>
            <Typography variant="subtitle1">
                Seleziona una sezione dal menu laterale.
            </Typography>
        </Box>
    )
}
