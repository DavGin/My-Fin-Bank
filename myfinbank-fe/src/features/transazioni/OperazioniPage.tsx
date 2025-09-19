// src/features/operazioni/OperazioniPage.tsx
import { useForm } from 'react-hook-form'
import { useMutation, useQuery } from '@tanstack/react-query'
import { createTransazione, type CreateTransactionInput } from './api'
import {
    Box,
    Typography,
    TextField,
    MenuItem,
    Button,
    Alert,
    CircularProgress,
} from '@mui/material'
import {fetchListaConti} from "../Conti/api.ts";
import {queryClient} from "../../queryClient.ts";

type Props = { numeroConto: string }

export default function OperazioniPage({ numeroConto }: Props) {
    const { data: conti, isLoading: loadingConti } = useQuery({
        queryKey: ['conti'],
        queryFn: fetchListaConti,
    })

    const {
        register,
        handleSubmit,
        watch,
        reset,
        formState: { errors },
    } = useForm<CreateTransactionInput>()

    const mutation = useMutation({
        mutationFn: (data: CreateTransactionInput) => createTransazione(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['transazioni', numeroConto] })

            reset()
        },
    })

    const tipo = watch('tipoTransazione')

    const onSubmit = (data: CreateTransactionInput) => {
        mutation.mutate({ ...data, importo: Number(data.importo) })
    }
    if (loadingConti) return <CircularProgress />

    return (
        <Box sx={{ maxWidth: 500 }}>
            <Typography variant="h5" gutterBottom>
                Nuova Operazione
            </Typography>
            <form onSubmit={handleSubmit(onSubmit)}>
                {/* Conto di origine */}
                <TextField
                    select
                    fullWidth
                    margin="normal"
                    label="Seleziona conto"
                    defaultValue=""
                    {...register('numeroConto', { required: 'Seleziona un conto' })}
                    error={!!errors.numeroConto}
                    helperText={errors.numeroConto?.message}
                >
                    {conti?.map((c) => (
                        <MenuItem key={c.numeroConto} value={c.numeroConto}>
                            {c.tipo} - {c.iban} (Saldo: {c.saldo}€)
                        </MenuItem>
                    ))}
                </TextField>

                {/* Descrizione */}
                <TextField
                    fullWidth
                    margin="normal"
                    label="Descrizione"
                    {...register('descrizione', { required: 'La descrizione è obbligatoria' })}
                    error={!!errors.descrizione}
                    helperText={errors.descrizione?.message}
                />

                {/* Importo */}
                <TextField
                    fullWidth
                    margin="normal"
                    label="Importo"
                    type="number"
                    inputProps={{ step: 0.01 }}
                    {...register('importo', {
                        required: 'L\'importo è obbligatorio',
                        valueAsNumber: true,
                        min: { value: 0.01, message: 'L\'importo deve essere positivo' },
                    })}
                    error={!!errors.importo}
                    helperText={errors.importo?.message}
                />

                {/* Tipo di operazione */}
                <TextField
                    select
                    fullWidth
                    margin="normal"
                    label="Tipo"
                    defaultValue=""
                    {...register('tipoTransazione', { required: 'Seleziona il tipo di operazione' })}
                    error={!!errors.tipoTransazione}
                    helperText={errors.tipoTransazione?.message}
                >
                    <MenuItem value="BONIFICO">Bonifico</MenuItem>
                    <MenuItem value="VERSAMENTO">Versamento</MenuItem>
                    <MenuItem value="PRELIEVO">Prelievo</MenuItem>
                    <MenuItem value="PAGAMENTO">Prelievo</MenuItem>
                </TextField>
                <TextField
                    select
                    fullWidth
                    margin="normal"
                    label="Valuta"
                    defaultValue="EURO" // Default a EURO
                    {...register('valuta', { required: true })}
                >
                    <MenuItem value="EURO">EURO</MenuItem>
                    <MenuItem value="DOLLARO">DOLLARO</MenuItem>
                </TextField>


                {/* IBAN destinatario: solo per BONIFICO */}
                {tipo === 'BONIFICO' || tipo === 'PAGAMENTO' && (
                    <TextField
                        fullWidth
                        margin="normal"
                        label="IBAN destinatario"
                        {...register('targetIban', {
                            required: 'L\'IBAN del destinatario è obbligatorio per un bonifico',
                            pattern: {
                                value: /^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$/,
                                message: 'Formato IBAN non valido',
                            },
                        })}
                        error={!!errors.targetIban}
                        helperText={errors.targetIban?.message}
                    />
                )}

                {/* Messaggi di stato */}
                {mutation.isError && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                        Errore durante l'esecuzione dell'operazione
                    </Alert>
                )}
                {mutation.isSuccess && (
                    <Alert severity="success" sx={{ mt: 2 }}>
                        Operazione eseguita con successo
                    </Alert>
                )}

                {/* Submit */}
                <Button
                    type="submit"
                    variant="contained"
                    sx={{ mt: 2 }}
                    disabled={mutation.isPending}
                >
                    {mutation.isPending ? 'Elaborazione...' : 'Esegui operazione'}
                </Button>
            </form>
        </Box>
    )
}
