import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchAllMutui, approvaMutuo, rigiutaMutuo } from '../../features/Mutui/api'
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
        mutationFn: async ({ id, stato }: { id: number; stato: 'APPROVATO' | 'RIFIUTATO' }) => {
            const motivo = stato === 'RIFIUTATO' ? 'Motivo del rifiuto.' : 'Motivo dell’approvazione.'
            
            if (stato === 'APPROVATO') {
                return approvaMutuo(id.toString(), stato, motivo)
            } else if (stato === 'RIFIUTATO') {
                return rigiutaMutuo(id.toString(), stato, motivo)
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['mutui-admin'] })
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
                                    {m.stato === 'IN_APPROVAZIONE' && (
                                        <>
                                            <Button
                                                variant="contained"
                                                color="success"
                                                size="small"
                                                sx={{ mr: 1 }}
                                                onClick={() => mutation.mutate({ id: m.id, stato: 'APPROVATO' })}
                                            >
                                                Approva
                                            </Button>
                                            <Button
                                                variant="contained"
                                                color="error"
                                                size="small"
                                                onClick={() => mutation.mutate({ id: m.id, stato: 'RIFIUTATO' })}
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
