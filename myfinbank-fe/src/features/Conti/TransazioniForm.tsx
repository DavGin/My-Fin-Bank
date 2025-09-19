// src/features/conti/TransactionForm.tsx
import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createTransazione, type CreateTransactionInput } from '../../features/transazioni/api'
import {
    Box,
    TextField,
    MenuItem,
    Button,
    Alert,
    Typography,
} from '@mui/material'

type Props = { numeroConto: string }

export default function TransactionForm({ numeroConto }: Props) {
    const { register, handleSubmit, reset } = useForm<CreateTransactionInput>()
    const queryClient = useQueryClient()

    const mutation = useMutation({
        mutationFn: (data: CreateTransactionInput) => createTransazione(numeroConto, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['transazioni', numeroConto] })
            reset()
        },
    })

    const onSubmit = (data: CreateTransactionInput) => {
        mutation.mutate({ ...data, importo: Number(data.importo) })
    }

    return (
        <Box sx={{ maxWidth: 400, mb: 3 }}>
            <Typography variant="h6" gutterBottom>Nuova transazione</Typography>
            <form onSubmit={handleSubmit(onSubmit)}>
                <TextField
                    fullWidth
                    margin="normal"
                    label="Descrizione"
                    {...register('descrizione', { required: true })}
                />
                <TextField
                    fullWidth
                    margin="normal"
                    label="Importo"
                    type="number"
                    inputProps={{ step: 0.01 }}
                    {...register('importo', { required: true, valueAsNumber: true })}
                />
                <TextField
                    select
                    fullWidth
                    margin="normal"
                    label="Tipo"
                    defaultValue=""
                    {...register('tipoTransazione', { required: true })}
                >
                    <MenuItem value="BONIFICO">Bonifico</MenuItem>
                    <MenuItem value="VERSAMENTO">Versamento</MenuItem>
                    <MenuItem value="PAGAMENTO">Pagamento</MenuItem>
                    <MenuItem value="PRELIEVO">Prelievo</MenuItem>
                </TextField>

                {mutation.isError && <Alert severity="error">Errore nella creazione</Alert>}
                {mutation.isSuccess && <Alert severity="success">Transazione registrata</Alert>}

                <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={mutation.isPending}>
                    {mutation.isPending ? 'Salvataggio...' : 'Registra'}
                </Button>
            </form>
        </Box>
    )
}
