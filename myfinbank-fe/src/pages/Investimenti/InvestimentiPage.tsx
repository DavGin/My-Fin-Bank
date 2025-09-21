// src/features/investimenti/InvestimentiPage.tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { fetchInvestimenti, createInvestimento, type Investimento, type CreateInvestimentoInput } from '../../features/Investimenti/api'
import {
    Box,
    Typography,
    TextField,
    MenuItem,
    Button,
    Alert,
    CircularProgress,
    Paper,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    TableContainer,
} from '@mui/material'
import {useNavigate} from "react-router-dom";

export default function InvestimentiPage() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()

    // Query lista investimenti
    const { data: investimenti, isLoading, isError } = useQuery<Investimento[]>({
        queryKey: ['investimenti'],
        queryFn: fetchInvestimenti,
    })

    // Form react-hook-form
    const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateInvestimentoInput>()

    // Mutation
    const mutation = useMutation({
        mutationFn: createInvestimento,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['investimenti'] })
            reset()
        },
    })

    // Handler per il click sulla riga
    const handleRowClick = (identificativo: string, durataMesi: number) => {
        // Reindirizza alla pagina dei dettagli passando i parametri
        navigate(`/investimenti/${identificativo}/${durataMesi}`)
    }

    const onSubmit = (data: CreateInvestimentoInput) => {
        mutation.mutate({ ...data, importoInvestito: Number(data.importoInvestito), durataMesi: Number(data.durataMesi) })
    }

    return (
        <Box>

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate('/investimenti/simulazione')}
                >
                    Vai a Simulazione Investimenti
                </Button>
            </Box>
            <Typography variant="h5" gutterBottom>Nuovo Investimento</Typography>

            <form onSubmit={handleSubmit(onSubmit)}>
                <TextField
                    select
                    fullWidth
                    margin="normal"
                    label="Tipo investimento"
                    defaultValue=""
                    {...register('tipoInvestimento', { required: 'Seleziona un tipo' })}
                    error={!!errors.tipoInvestimento}
                    helperText={errors.tipoInvestimento?.message}
                >
                    <MenuItem value="AZIONI">Azioni</MenuItem>
                    <MenuItem value="OBBLIGAZIONI">Obbligazioni</MenuItem>
                    <MenuItem value="FONDI">Fondi</MenuItem>
                    <MenuItem value="ETF">ETF</MenuItem>
                </TextField>

                <TextField
                    fullWidth
                    margin="normal"
                    label="Tasso di interesse (%)"
                    type="number"
                    {...register('tassoRitornoPrevisto', { required: 'Tasso di interesse (%) obbligatorio' })}
                    error={!!errors.tassoRitornoPrevisto}
                    helperText={errors.tassoRitornoPrevisto?.message}
                />
                <TextField
                    fullWidth
                    margin="normal"
                    label="Importo (€)"
                    type="number"
                    {...register('importoInvestito', { required: 'Importo obbligatorio', min: { value: 100, message: 'Importo minimo 100€' } })}
                    error={!!errors.importoInvestito}
                    helperText={errors.importoInvestito?.message}
                />

                <TextField
                    fullWidth
                    margin="normal"
                    label="Durata (mesi)"
                    type="number"
                    {...register('durataMesi', { required: 'Durata obbligatoria', min: { value: 1, message: 'Almeno 1 mese' } })}
                    error={!!errors.durataMesi}
                    helperText={errors.durataMesi?.message}
                />

                {mutation.isError && <Alert severity="error" sx={{ mt: 2 }}>Errore creazione investimento</Alert>}
                {mutation.isSuccess && <Alert severity="success" sx={{ mt: 2 }}>Investimento creato</Alert>}

                <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={mutation.isPending}>
                    {mutation.isPending ? 'Invio...' : 'Crea'}
                </Button>
            </form>

            <Box sx={{ mt: 4 }}>
                <Typography variant="h6">I tuoi investimenti</Typography>
                {isLoading && <CircularProgress />}
                {isError && <Alert severity="error">Errore caricamento investimenti</Alert>}
                {investimenti && (
                    <TableContainer component={Paper} sx={{ mt: 2 }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>ID</TableCell>
                                    <TableCell>Tipo</TableCell>
                                    <TableCell>Importo</TableCell>
                                    <TableCell>Durata mesi</TableCell>
                                    <TableCell>Data Apertura</TableCell>
                                    <TableCell>Data Chiusura</TableCell>
                                    <TableCell>Rendimento stimato</TableCell>
                                    <TableCell>Stato</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {investimenti.map((inv) => (
                                    <TableRow
                                        key={inv.id}
                                        sx={{ cursor: 'pointer' }}
                                        onClick={() => handleRowClick(inv.identificativo, inv.mesi)}
                                    >
                                        <TableCell>{inv.identificativo}</TableCell>
                                        <TableCell>{inv.tipoInvestimento}</TableCell>
                                        <TableCell>{inv.importoInvestito}</TableCell>
                                        <TableCell>{inv.mesi}</TableCell>
                                        <TableCell>{new Date(inv.dataInizio).toLocaleDateString('it-IT')}</TableCell>
                                        <TableCell>{new Date(inv.dataFine).toLocaleDateString('it-IT')}</TableCell>
                                        <TableCell>{inv.tassoRitornoPrevisto}%</TableCell>
                                        <TableCell>{inv.statoInvestimento}</TableCell>
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
