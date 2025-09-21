// src/features/conti/ContiPage.tsx
import { useQuery } from '@tanstack/react-query'
import {fetchListaConti} from '../../features/Conti/api'
import type { Conto } from '../../features/Conti/api'
import {
    Box,
    Typography,
    CircularProgress,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper, Button,
} from '@mui/material'
import ContoForm from "../../features/Conti/ContoFrom.tsx";
import {useNavigate} from "react-router-dom";

export default function ContiPage() {
    const navigate = useNavigate()
    const { data, isLoading, isError } = useQuery<Conto[]>({
        queryKey: ['conti'],
        queryFn: fetchListaConti,
    })


    if (isLoading) return <CircularProgress />
    if (isError) return <Typography color="error">Errore nel caricamento conti</Typography>

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                I tuoi conti
            </Typography>
            <ContoForm />

            <TableContainer component={Paper}>
                {/* ...tabella esistente */}
            </TableContainer>
            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Conto N°</TableCell>
                            <TableCell>IBAN</TableCell>
                            <TableCell>Tipo</TableCell>
                            <TableCell align="right">Saldo (€)</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {data?.map((conto) => (
                            <TableRow
                                key={conto.numeroConto}
                                hover
                                sx={{ cursor: 'pointer' }}
                                onClick={() => navigate(`/conti/findByNumeroConto/${conto.numeroConto}`)}
                            >
                                <TableCell>{conto.numeroConto}</TableCell>
                                <TableCell>{conto.iban}</TableCell>
                                <TableCell>{conto.tipo}</TableCell>
                                <TableCell align="right">
                                    {conto.saldo.toLocaleString('it-IT', {
                                        style: 'currency',
                                        currency: 'EUR',
                                    })}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Box sx={{ display: 'flex', justifyContent: 'flex-start', mb: 2 }}>
                <Button
                    variant="outlined"
                    color="primary"
                    onClick={() => navigate(-1)}
                >
                    Indietro
                </Button>
            </Box>

        </Box>
    )
}
