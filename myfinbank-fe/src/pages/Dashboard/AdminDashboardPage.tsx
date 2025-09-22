import { Box, Container, Typography, Button } from '@mui/material'
import { useNavigate } from 'react-router-dom'

export default function AdminDashboardPage() {
    const navigate = useNavigate()

    return (
        <Container maxWidth="lg">
            <Box sx={{ mt: 4 }}>
                <Typography variant="h4" gutterBottom>
                    Benvenuto nella Dashboard Admin
                </Typography>
                <Typography variant="subtitle1" sx={{ mb: 4 }}>
                    Da qui puoi accedere alle funzionalità amministrative e gestire i vari aspetti dell'applicazione.
                </Typography>

                <Button
                    variant="contained"
                    sx={{ mr: 2 }}
                    onClick={() => navigate('/admin/mutui/AdminMutuiPage')}
                >
                    Gestione Mutui
                </Button>
                <Button
                    variant="contained"
                    onClick={() => navigate('/profile')}
                >
                    Profilo Admin
                </Button>
            </Box>
        </Container>
    )
}
