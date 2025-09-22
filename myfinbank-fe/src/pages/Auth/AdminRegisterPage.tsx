import { useForm, Controller } from 'react-hook-form'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../../api/authApi'
import {
    Container,
    Box,
    TextField,
    Button,
    Typography,
    Alert,
} from '@mui/material'

type RegisterFormData = {
    username: string
    email: string
    password: string
    nome: string
    cognome: string
    codiceFiscale: string
    dataNascita: string
    isAdmin: boolean
}


export default function AdminRegisterPage() {
    const { register, control, handleSubmit } = useForm<RegisterFormData>({
        defaultValues: { isAdmin: true }, // Ruolo admin impostato a "true"
    })
    const navigate = useNavigate()

    // Nuova sintassi React Query v5
    const mutation = useMutation({
        mutationFn: (data: RegisterFormData) => authApi.registrazione(data), // Funzione API di registrazione
        onSuccess: () => {
            alert('Registrazione completata, torna alla login')
            navigate('/auth/login') // Reindirizza alla pagina di login
        },
        onError: (error: any) => {
            console.error('Errore durante la registrazione:', error)
        },
    })

    const onSubmit = (formData: RegisterFormData) => {
        formData.isAdmin = true;
        console.log('[AdminRegisterPage] Form inviato con dati:', formData)

        mutation.mutate(formData)
    }

    return (
        <Container maxWidth="sm">
            <Box sx={{ mt: 8 }}>
                <Typography variant="h4" gutterBottom>
                    Registrazione Admin
                </Typography>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <Controller
                        name="nome"
                        control={control}
                        render={({ field }) => (
                            <TextField {...field} fullWidth label="Nome" margin="normal" required />
                        )}
                    />
                    <Controller
                        name="cognome"
                        control={control}
                        render={({ field }) => (
                            <TextField {...field} fullWidth label="Cognome" margin="normal" required />
                        )}
                    />
                    <Controller
                        name="email"
                        control={control}
                        render={({ field }) => (
                            <TextField {...field} fullWidth label="Email" type="email" margin="normal" required />
                        )}
                    />
                    <Controller
                        name="codiceFiscale"
                        control={control}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                fullWidth
                                label="Codice Fiscale"
                                margin="normal"
                                required
                            />
                        )}
                    />
                    <Controller
                        name="dataNascita"
                        control={control}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                fullWidth
                                type="date"
                                label="Data di Nascita"
                                InputLabelProps={{ shrink: true }}
                                margin="normal"
                                required
                            />
                        )}
                    />
                    <Controller
                        name="username"
                        control={control}
                        render={({ field }) => (
                            <TextField {...field} fullWidth label="Username" margin="normal" required />
                        )}
                    />
                    <Controller
                        name="password"
                        control={control}
                        rules={{
                            required: 'La password è obbligatoria',
                            pattern: {
                                value: /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
                                message: 'La password deve contenere almeno 8 caratteri, incluse maiuscole, minuscole, numeri e caratteri speciali',
                            },
                        }}
                        render={({ field, fieldState }) => (
                            <TextField
                                {...field}
                                fullWidth
                                type="password"
                                label="Password"
                                margin="normal"
                                required
                                error={!!fieldState.error}
                                helperText={fieldState.error?.message}
                            />
                        )}
                    />

                    {/* Mostra eventuali errori durante la registrazione */}
                    {mutation.isError && (
                        <Alert severity="error" sx={{ mt: 2 }}>
                            {(mutation.error as any)?.response?.data?.message || 'Si è verificato un errore durante la registrazione'}
                        </Alert>
                    )}
                    <input type="hidden" value="true" {...register('isAdmin')} />

                    <Button
                        type="submit"
                        variant="contained"
                        fullWidth
                        sx={{ mt: 3 }}
                        disabled={mutation.isPending}
                    >
                        {mutation.isPending ? 'Registrazione in corso...' : 'Registrati'}
                    </Button>
                </form>
            </Box>
        </Container>
    )
}
