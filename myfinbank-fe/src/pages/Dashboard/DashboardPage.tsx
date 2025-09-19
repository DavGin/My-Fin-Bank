// src/features/dashboard/DashboardPage.tsx
import { useAppSelector } from '../../app/hooks'
import {
    Typography,
    Box,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
} from '@mui/material'
import { type Conto, fetchListaConti } from '../../features/Conti/api'
import { useQuery } from '@tanstack/react-query'
import {fetchTransazioniByConto, type Transazioni} from "../../features/transazioni/api.ts";

export default function DashboardPage() {
    const user = useAppSelector((state) => state.auth.user)
    const { data: conti, isLoading, isError } = useQuery<Conto[]>({
        queryKey: ['conti'],
        queryFn: fetchListaConti,
    })

    // Filtra il conto con id 1 (questo non deve essere dentro il render condizionale)
    const contoSelezionato = conti?.find((c) => c.id === 1)

    // Ottieni transazioni per il conto con id 1
    const { data: transazioni, isLoading: loadingTx, isError: errorTx } = useQuery<Transazioni[]>({
        queryKey: ['transazioni', contoSelezionato?.numeroConto],
        queryFn: () =>
            contoSelezionato?.numeroConto
                ? fetchTransazioniByConto(contoSelezionato.numeroConto)
                : Promise.reject('Numero conto non valido'),
        enabled: !!contoSelezionato?.numeroConto, // Definisci la proprietà enabled per abilitare/disabilitare la query
    })

    if (isLoading) {
        return (
            <Box sx={{ mt: 4 }}>
                <Typography variant="body2">Caricamento...</Typography>
            </Box>
        )
    }

    if (isError) {
        return (
            <Box sx={{ mt: 4 }}>
                <Typography variant="body2" color="error">
                    Si è verificato un errore durante il caricamento dei conti.
                </Typography>
            </Box>
        )
    }

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Benvenuto, {user?.nome} {user?.cognome}
            </Typography>
            {contoSelezionato ? (
                <>
                    <Box>
                        <Typography variant="h6" sx={{ mt: 4 }}>
                            Dettagli conto
                        </Typography>
                        <TableContainer component={Paper} sx={{ mt: 2 }}>
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
                                    <TableRow>
                                        <TableCell>{contoSelezionato.numeroConto}</TableCell>
                                        <TableCell>{contoSelezionato.iban}</TableCell>
                                        <TableCell>{contoSelezionato.tipo}</TableCell>
                                        <TableCell align="right">
                                            {contoSelezionato.saldo.toLocaleString('it-IT', {
                                                style: 'currency',
                                                currency: 'EUR',
                                            })}
                                        </TableCell>
                                    </TableRow>
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Box>

                    {/* Mostra le transazioni relative al conto */}
                    <Box sx={{ mt: 4 }}>
                        <Typography variant="h6">Transazioni</Typography>
                        {loadingTx && <Typography variant="body2">Caricamento transazioni...</Typography>}
                        {errorTx && (
                            <Typography variant="body2" color="error">
                                Si è verificato un errore durante il caricamento delle transazioni.
                            </Typography>
                        )}
                        {!loadingTx && !errorTx && transazioni?.length === 0 && (
                            <Typography variant="body2">Nessuna transazione disponibile.</Typography>
                        )}
                        {!loadingTx && !errorTx && transazioni?.length > 0 && (
                            <TableContainer component={Paper} sx={{ mt: 2 }}>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>ID</TableCell>
                                            <TableCell>Descrizione</TableCell>
                                            <TableCell>Data</TableCell>
                                            <TableCell align="right">Importo (€)</TableCell>
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
                </>
            ) : (
                <Typography variant="body2">Il conto con ID 1 non esiste.</Typography>
            )}
        </Box>
    )
}
