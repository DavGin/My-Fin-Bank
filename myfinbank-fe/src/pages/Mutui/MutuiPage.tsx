import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import {
    fetchListaMutui,
    createMutuo,
    type Mutuo,
    type CreateMutuoInput,
    fetchRateByNumeroPratica
} from '../../features/Mutui/api';
import {
    Box,
    Typography,
    TextField,
    Button,
    Alert,
    CircularProgress,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    Paper,
    TableContainer,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';

export default function MutuiPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [loadingRate, setLoadingRate] = useState(false); // Stato per indicare se sta caricando
    const [errorRate, setErrorRate] = useState<string | null>(null);

    // Query lista mutui
    const { data: mutui, isLoading, isError } = useQuery<Mutuo[]>({
        queryKey: ['mutui'],
        queryFn: fetchListaMutui,
    });

    // Form
    const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateMutuoInput>();

    // Mutation richiesta mutuo
    const mutation = useMutation({
        mutationFn: createMutuo,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['mutui'] });
            reset();
        },
    });

    const onSubmit = async (data: CreateMutuoInput) => {
        try {
            const formattedData = {
                ...data,
                importo: Number(data.importo),
                durataMesi: Number(data.durataMesi),
                tassoInteresse: Number(data.tassoInteresse),
            };

            mutation.mutate(
                formattedData,
                {
                    onSuccess: async (mutuoCreato) => {
                        try {
                            const rate = await fetchRateByNumeroPratica(mutuoCreato.numeroPratica);
                            console.log('Rate calcolate con successo:', rate);
                        } catch (error) {
                            console.error('Errore durante il calcolo delle rate:', error);
                        }
                    },
                }
            );
        } catch (error) {
            console.error('Errore nella sottomissione del modulo:', error);
        }
    };


    const handleDettaglioRate = async (numeroPratica: string) => {
        setLoadingRate(true);
        setErrorRate(null); // Reset dell'errore

        try {
            const response = await fetchRateByNumeroPratica(numeroPratica);
            navigate(`/dettaglioRate`, {
                state: {
                    rate: response, // Includi le rate
                    numeroPratica: numeroPratica, // Assicurati di passare numeroPratica
                },
            });
            console.log('Rate calcolate con successo:', numeroPratica, response);

        } catch (error) {
            setErrorRate('Errore nel calcolo delle rate');
        } finally {
            setLoadingRate(false);
        }
    };

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate('/simulazioneMutui')}
                >
                    Vai a Simulazione Mutui
                </Button>
            </Box>

            <Typography variant="h5" gutterBottom>
                Richiesta Mutuo
            </Typography>

            <form onSubmit={handleSubmit(onSubmit)}>
                <TextField
                    fullWidth
                    margin="normal"
                    label="Importo richiesto (€)"
                    type="number"
                    {...register('importo', {
                        required: 'Importo obbligatorio',
                        min: { value: 1000, message: 'Importo minimo 1000€' },
                        valueAsNumber: true,
                    })}
                    error={!!errors.importo}
                    helperText={errors.importo?.message}
                />
                <TextField
                    fullWidth
                    margin="normal"
                    label="Durata (mesi)"
                    type="number"
                    {...register('durataMesi', {
                        required: 'Durata obbligatoria',
                        min: { value: 12, message: 'Almeno 12 mesi (1 anno)' },
                        max: { value: 360, message: 'Massimo 360 mesi (30 anni)' },
                    })}
                    error={!!errors.durataMesi}
                    helperText={errors.durataMesi?.message}
                />
                <TextField
                    fullWidth
                    margin="normal"
                    label="Tasso interesse (%)"
                    type="number"
                    inputProps={{ step: 0.01 }}
                    {...register('tassoInteresse', {
                        required: 'Tasso obbligatorio',
                        min: { value: 0.1, message: 'Minimo 0.1%' },
                    })}
                    error={!!errors.tassoInteresse}
                    helperText={errors.tassoInteresse?.message}
                />

                {mutation.isError && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                        Errore richiesta mutuo
                    </Alert>
                )}
                {mutation.isSuccess && (
                    <Alert severity="success" sx={{ mt: 2 }}>
                        Richiesta inviata!
                    </Alert>
                )}

                <Button
                    type="submit"
                    variant="contained"
                    sx={{ mt: 2 }}
                    disabled={mutation.isPending}
                >
                    {mutation.isPending ? 'Invio...' : 'Richiedi mutuo'}
                </Button>
            </form>

            <Box sx={{ mt: 4 }}>
                <Typography variant="h6">I tuoi mutui</Typography>
                {isLoading && <CircularProgress />}
                {isError && <Alert severity="error">Errore caricamento mutui</Alert>}
                {mutui && (
                    <TableContainer component={Paper} sx={{ mt: 2 }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Pratica N°</TableCell>
                                    <TableCell>Importo</TableCell>
                                    <TableCell>Durata</TableCell>
                                    <TableCell>Tasso</TableCell>
                                    <TableCell>Stato</TableCell>
                                    <TableCell>Data richiesta</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {mutui.map((m) => (
                                    <TableRow
                                        key={m.numeroPratica}
                                        hover
                                        sx={{ cursor: 'pointer' }}
                                        onClick={() => handleDettaglioRate(m.numeroPratica)} // Calcola e reindirizza
                                    >
                                        <TableCell>{m.numeroPratica}</TableCell>
                                        <TableCell>{m.importo}</TableCell>
                                        <TableCell>{m.durataMesi}</TableCell>
                                        <TableCell>{m.tassoInteresse}</TableCell>
                                        <TableCell>{m.stato}</TableCell>
                                        <TableCell>{m.dataCreazione}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}

                {loadingRate && <CircularProgress sx={{ mt: 2 }} />}
                {errorRate && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                        {errorRate}
                    </Alert>
                )}
            </Box>
        </Box>
    );
}
