import { Container, Typography } from '@mui/material'


export default function DashboardPage() {
    return (
        <Container>
            <Typography variant="h4" sx={{ mt: 4 }}>Benvenuto in MyFinBank</Typography>
            <Typography sx={{ mt: 2 }}>Questa è la Dashboard - aggiungi i widget qui.</Typography>
        </Container>
    )
}
