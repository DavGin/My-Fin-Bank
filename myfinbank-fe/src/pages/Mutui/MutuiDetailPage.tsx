import React from 'react';
import {
    Box,
    Paper,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    TableContainer,
    Typography,
    Alert,
    Button,
    Select,
    MenuItem
} from '@mui/material';
import { useLocation } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { pagaRata, type Rata } from '../../features/Mutui/api';
import { type Conto, fetchListaConti } from '../../features/Conti/api.ts';
import MutuoPieChart from './MutuoPieChart'; // Importa il grafico a torta

export default function MutuoDetailPage() {
    const location = useLocation();
    const queryClient = useQueryClient();

    // Inizializza lo stato per il conto selezionato
    const [contoSelezionato, setContoSelezionato] = React.useState<string>('');

    // Recupera conti tramite useQuery
    const { data: conti, isLoading, isError } = useQuery<Conto[]>({
        queryKey: ['conti'],
        queryFn: fetchListaConti,
    });

    const rate = location.state?.rate as Rata[];
    const numeroPratica = location.state?.numeroPratica;

    // Mutation per pagare una rata
    const mutation = useMutation({
        mutationFn: ({ numeroRata, numeroConto }: { numeroRata: number; numeroConto: string }) =>
            pagaRata(numeroPratica, numeroRata, numeroConto),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rate', numeroPratica] }),
    });

    // Imposta il valore di default per il dropdown quando i conti sono disponibili
    React.useEffect(() => {
        if (conti?.length && !contoSelezionato) {
            setContoSelezionato(conti[0].id); // Seleziona il primo conto come valore predefinito
        }
    }, [conti, contoSelezionato]);

    return (
        <Box sx={{ mt: 4 }}>
            <Typography variant="h5" gutterBottom>
                Dettaglio delle Rate
            </Typography>

            {isLoading && <Typography>Caricamento conti...</Typography>}
            {isError && <Alert severity="error">Errore nel caricamento dei conti.</Alert>}

            {!rate || rate.length === 0 ? (
                <Alert severity="warning">Nessuna rata disponibile</Alert>
            ) : (
                <>
                    {/* Grafico a torta */}
                    <Box sx={{ my: 4 }}>
                        <Typography variant="h6" gutterBottom>
                            Stato delle Rate
                        </Typography>
                        <MutuoPieChart rate={rate} />
                    </Box>

                    {/* Tabella delle rate */}
                    <TableContainer component={Paper} sx={{ mt: 2 }}>
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>#</TableCell>
                                    <TableCell>Data Scadenza</TableCell>
                                    <TableCell>Quota Capitale</TableCell>
                                    <TableCell>Quota Interessi</TableCell>
                                    <TableCell>Totale Rata</TableCell>
                                    <TableCell>Saldo Rimanente</TableCell>
                                    <TableCell>Stato</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {rate.map((r) => (
                                    <TableRow key={r.numeroRata}>
                                        <TableCell>{r.numeroRata}</TableCell>
                                        <TableCell>{new Date(r.scadenza).toLocaleDateString('it-IT')}</TableCell>
                                        <TableCell>{r.quotaCapitale.toFixed(2)} €</TableCell>
                                        <TableCell>{r.interessi.toFixed(2)} €</TableCell>
                                        <TableCell>{r.rataTotale.toFixed(2)} €</TableCell>
                                        <TableCell>{r.saldoRimanente.toFixed(2)} €</TableCell>
                                        <TableCell>
                                            {r.statoRata === 'PAGATO' && <span style={{ color: 'green' }}>✔ Pagata</span>}
                                            {r.statoRata === 'SCADUTO' && <span style={{ color: 'red' }}>✘ Scaduta</span>}
                                            {r.statoRata === 'DA_PAGARE' && (
                                                <>
                                                    <Select
                                                        size="small"
                                                        value={contoSelezionato}
                                                        onChange={(e) => setContoSelezionato(e.target.value)}
                                                        displayEmpty
                                                    >
                                                        {conti?.map((c) => (
                                                            <MenuItem key={c.id} value={c.id}>
                                                                {c.numeroConto} ({c.saldo.toFixed(2)} €)
                                                            </MenuItem>
                                                        ))}
                                                    </Select>
                                                    <Button
                                                        size="small"
                                                        variant="contained"
                                                        color="primary"
                                                        onClick={() =>
                                                            mutation.mutate({
                                                                numeroRata: r.numeroRata,
                                                                numeroConto: contoSelezionato,
                                                            })
                                                        }
                                                        disabled={mutation.isPending || !contoSelezionato}
                                                        sx={{ ml: 4 }}
                                                    >
                                                        Paga
                                                    </Button>
                                                </>
                                            )}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </>
            )}
        </Box>
    );
}
