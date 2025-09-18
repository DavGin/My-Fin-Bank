// src/pages/Auth/RegisterPage.tsx
import { useForm } from 'react-hook-form'
import { authApi } from '../../api/authApi'
import { useMutation } from '@tanstack/react-query'
import {
    Container,
    TextField,
    Button,
    Box,
    Typography,
    Alert,
    CircularProgress,
} from '@mui/material'

// tipi per i dati del form
type RegisterFormData = {
    firstName: string
    lastName: string
    email: string
    password: string
}

export default function RegisterPage() {
    const { register, handleSubmit } = useForm<RegisterFormData>()

    const mutation = useMutation({
        mutationFn: (data: RegisterFormData) => authApi.register(data),
        onSuccess: () => {
            // qui puoi aggiungere redirect o messaggi di conferma
            console.log('Registrazione avvenuta con successo!')
        },
    })

    const onSubmit = (data: RegisterFormData) => mutation.mutate(data)

    return (
        <Container maxWidth="sm">
            <Box sx={{ mt: 6 }}>
                <Typography variant="h5" gutterBottom>
                    Register
                </Typography>

                {mutation.isError && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {(mutation.error as any)?.response?.data || 'Errore durante la registrazione'}
                    </Alert>
                )}

                {mutation.isSuccess && (
                    <Alert severity="success" sx={{ mb: 2 }}>
                        Registrazione completata! Ora puoi accedere.
                    </Alert>
                )}

                <form onSubmit={handleSubmit(onSubmit)}>
                    <TextField
                        label="First name"
                        fullWidth
                        margin="normal"
                        {...register('firstName', { required: true })}
                    />
                    <TextField
                        label="Last name"
                        fullWidth
                        margin="normal"
                        {...register('lastName', { required: true })}
                    />
                    <TextField
                        label="Email"
                        type="email"
                        fullWidth
                        margin="normal"
                        {...register('email', { required: true })}
                    />
                    <TextField
                        label="Password"
                        type="password"
                        fullWidth
                        margin="normal"
                        {...register('password', { required: true })}
                    />

                    <Button
                        type="submit"
                        variant="contained"
                        sx={{ mt: 2 }}
                        disabled={mutation.isPending}
                    >
                        {mutation.isPending ? <CircularProgress size={20} /> : 'Register'}
                    </Button>
                </form>
            </Box>
        </Container>
    )
}
