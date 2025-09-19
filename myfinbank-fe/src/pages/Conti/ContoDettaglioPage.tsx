import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchConto, fetchTransazioniByConto, type Transazioni } from '../../features/transazioni/api'
import {
    Box,
    Typography,
    CircularProgress,
    Alert,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Paper,
    TableContainer,
} from '@mui/material'
import type { Conto } from '../../features/Conti/api'

export default function ContoDettaglioPage() {
    const { numeroConto } = useParams<{ numeroConto: string }>() // Corretto uso di `useParams`

    const { data: conto, isLoading: loadingConto, isError: errorConto } = useQuery<Conto>({
        queryKey: ['conto', numeroConto],
        queryFn: () => (numeroConto ? fetchConto(numeroConto) : Promise.reject('Numero conto non valido')),
    })

    const { data: transazioni, isLoading: loadingTx, isError: errorTx } = useQuery<Transazioni[]>({
        queryKey: ['transazioni', numeroConto],
        queryFn: () => (numeroConto ? fetchTransazioniByConto(numeroConto) : Promise.reject('Numero conto non valido')),
        enabled: !!numeroConto, // Abilita la query solo se `numeroConto` è valido
    })

    if (loadingConto) return <CircularProgress />
    if (errorConto || !conto) return <Alert severity="error">Errore nel caricamento del conto</Alert>

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Dettaglio conto #{conto.numeroConto}
            </Typography>
            <Typography variant="subtitle1">IBAN: {conto.iban}</Typography>
            <Typography variant="subtitle1">Tipo: {conto.tipo}</Typography>
            <Typography variant="subtitle1">
                Saldo: {conto.saldo.toLocaleString('it-IT', {
                    style: 'currency',
                    currency: 'EUR',
                })}
            </Typography>

            <Box sx={{ mt: 4 }}>
                <Typography variant="h6">Transazioni</Typography>
                {loadingTx && <CircularProgress />}
                {errorTx && <Alert severity="error">Errore nel caricamento delle transazioni</Alert>}
                {!loadingTx && !errorTx && transazioni?.length === 0 && (
                    <Typography variant="body1">Nessuna transazione disponibile.</Typography>
                )}
                {!loadingTx && !errorTx && transazioni?.length > 0 && (
                    <TableContainer component={Paper} sx={{ mt: 2 }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>ID</TableCell>
                                    <TableCell>Descrizione</TableCell>
                                    <TableCell>Data</TableCell>
                                    <TableCell align="right">Importo</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {transazioni.map((tx) => (
                                    <TableRow key={tx.id}>
                                        <TableCell>{tx.id}</TableCell>
                                        <TableCell>{tx.descrizione}</TableCell>
                                        <TableCell>
                                            {new Date(tx.data).toLocaleDateString('it-IT')}
                                        </TableCell>
                                        <TableCell align="right">
                                            {tx.importo.toLocaleString('it-IT', {
                                                style: 'currency',
                                                currency: 'EUR',
                                            })}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </Box>
        </Box>
    )
}
