import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {fetchAllMutui, approvaMutuo, type EsitoMutuoDto, rejectMutuo} from '../../features/Mutui/api'
import type { Mutuo } from '../../features/Mutui/api'
import {
    Box,
    Typography,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    Paper,
    TableContainer,
    Button,
    Alert,
    CircularProgress,
} from '@mui/material'

export default function AdminMutuiPage() {
    const queryClient = useQueryClient()

    const { data: mutui, isLoading, isError } = useQuery<Mutuo[]>({
        queryKey: ['mutui-admin'],
        queryFn: fetchAllMutui,
    })

    const mutation = useMutation({
        mutationFn: async ({ numeroPratica, stato }: { numeroPratica: string; stato: 'APPROVATO' | 'RIFIUTATO' }) => {
            const motivo = stato === 'RIFIUTATO'
                ? 'Motivo del rifiuto.'
                : 'Motivo dell’approvazione.'

            if (stato === 'APPROVATO') {
                const data: EsitoMutuoDto = {
                    numeroPratica: numeroPratica,
                    newStato: stato,
                    motivo,
                }
                return approvaMutuo(data)
            } else if (stato === 'RIFIUTATO') {
                const data: EsitoMutuoDto = {
                    numeroPratica: numeroPratica,
                    newStato: 'RIFIUTATA', // correzione dello stato
                    motivo,
                }
                return rejectMutuo(data)
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['mutui-admin'] }) // Aggiorniamo la cache
        },
    })


    if (isLoading) return <CircularProgress />
    if (isError) return <Alert severity="error">Errore caricamento mutui</Alert>

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Gestione Mutui (Admin)
            </Typography>

            <TableContainer component={Paper} sx={{ mt: 2 }}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Pratica N°</TableCell>
                            <TableCell>Utente</TableCell>
                            <TableCell>Importo</TableCell>
                            <TableCell>Durata</TableCell>
                            <TableCell>Tasso</TableCell>
                            <TableCell>Stato</TableCell>
                            <TableCell>Azioni</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {mutui?.map((m) => (
                            <TableRow key={m.id}>
                                <TableCell>{m.numeroPratica}</TableCell>
                                <TableCell>{/* backend deve restituire nome utente */}</TableCell>
                                <TableCell>
                                    {m.importo.toLocaleString('it-IT', { style: 'currency', currency: 'EUR' })}
                                </TableCell>
                                <TableCell>{m.durataMesi} anni</TableCell>
                                <TableCell>{m.tassoInteresse}%</TableCell>
                                <TableCell>{m.stato}</TableCell>
                                <TableCell>
                                    {m.stato === 'PENDING' && (
                                        <>
                                            <Button
                                                variant="contained"
                                                color="success"
                                                size="small"
                                                sx={{ mr: 1 }}
                                                onClick={() => mutation.mutate({ numeroPratica: m.numeroPratica, stato: 'APPROVATO' })}
                                            >
                                                Approva
                                            </Button>
                                            <Button
                                                variant="contained"
                                                color="error"
                                                size="small"
                                                onClick={() => mutation.mutate({ numeroPratica: m.numeroPratica, stato: 'RIFIUTATO' })}
                                            >
                                                Rifiuta
                                            </Button>
                                        </>
                                    )}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>
    )
}
