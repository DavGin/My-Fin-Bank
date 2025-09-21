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
    TableRow, CircularProgress, Alert,
} from '@mui/material'
import { type Conto, fetchListaConti } from '../../features/Conti/api'
import { useQuery } from '@tanstack/react-query'
import {fetchTransazioniByConto, type Transazioni} from "../../features/transazioni/api.ts";
import {Bar, BarChart, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';

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
    const aggregated = transazioni?.reduce((acc, tx) => {
        const month = new Date(tx.dataTransazione).toLocaleString("default", { month: "short", year: "numeric" });
        if (!acc[month]) {
            acc[month] = { month, Entrate: 0, Uscite: 0 };
        }
        console.log("Transazione:", tx); // Aggiungi questo per debug
        console.log("Direzione:", tx.direzione); // Controlla i valori di `direzione`

        if (tx.direzione === "ENTRATA") {
            acc[month].Entrate += parseFloat(tx.importo);
        } else {
            acc[month].Uscite += parseFloat(tx.importo);
        }
        return acc;
    }, {})|| {};

    const chartData = Object.values(aggregated)|| {}
    ;

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Benvenuto
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

                    <Box mt={4}>
                        <Typography variant="h6">Transazioni</Typography>
                        {loadingTx && <CircularProgress />}
                        {errorTx && <Alert severity="error">Errore nel caricamento delle transazioni</Alert>}
                        {!loadingTx && !errorTx && (
                            <>
                                {transazioni?.length === 0 ? (
                                    <Typography variant="body1">Nessuna transazione disponibile.</Typography>
                                ) : (
                                    <TableContainer component={Paper} sx={{ mt: 2 }}>
                                        <Table>
                                            <TableHead>
                                                <TableRow>
                                                    <TableCell>Data</TableCell>
                                                    <TableCell>Tipo</TableCell>
                                                    <TableCell>Descrizione</TableCell>
                                                    <TableCell align="right">Importo</TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {transazioni?.map((tx) => (
                                                    <TableRow key={tx.id}>
                                                        <TableCell>{new Date(tx.dataTransazione).toLocaleDateString('it-IT')}</TableCell>
                                                        <TableCell>
                                                            {(() => {
                                                                switch (tx.tipoTransazione) {
                                                                    case 'BONIFICO':
                                                                        return 'Bonifico';
                                                                    case 'PAGAMENTO':
                                                                        return 'Pagamento';
                                                                    case 'RATA_MUTUO':
                                                                        return 'Rata Mutuo';
                                                                    case 'VERSAMENTO':
                                                                        return 'Versamento';
                                                                    case 'PRELIEVO':
                                                                        return 'Prelievo';
                                                                    default:
                                                                        return 'Tipo sconosciuto';
                                                                }
                                                            })()}
                                                        </TableCell>

                                                        <TableCell>{tx.descrizione}</TableCell>
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
                            </>
                        )}
                        <Box>
                            <Typography variant="h6" gutterBottom>
                                Andamento Entrate/Uscite
                            </Typography>
                            <ResponsiveContainer width="100%" height={300}>
                                <BarChart data={chartData}>
                                    <XAxis dataKey="month" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="Entrate" fill="#4caf50" />
                                    <Bar dataKey="Uscite" fill="#f44336" />
                                </BarChart>
                            </ResponsiveContainer>
                        </Box>
                    </Box>
                </>
            ) : (
                <Typography variant="body2">Il conto con ID 1 non esiste.</Typography>
            )}
        </Box>

    )
}
