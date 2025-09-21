import {useNavigate, useParams} from 'react-router-dom'
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
import {Bar, BarChart, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";



export default function ContoDettaglioPage() {
    const navigate  = useNavigate()
    const { numeroConto } = useParams<{ numeroConto: string }>()

    const { data: conto, isLoading: loadingConto, isError: errorConto } = useQuery<Conto>({
        queryKey: ['conto', numeroConto],
        queryFn: () => (numeroConto ? fetchConto(numeroConto) : Promise.reject('Numero conto non valido')),
    })

    const { data: transazioni, isLoading: loadingTx, isError: errorTx } = useQuery<Transazioni[]>({
        queryKey: ['transazioni', numeroConto],
        queryFn: () => (numeroConto ? fetchTransazioniByConto(numeroConto) : Promise.reject('Numero conto non valido')),
        enabled: !!numeroConto,
    })

    if (loadingConto) {
        return <CircularProgress />
    }

    if (errorConto || !conto) {
        return <Alert severity="error">Errore nel caricamento del conto</Alert>
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
            <Box mb={4}>
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

        </Box>
    )
}
