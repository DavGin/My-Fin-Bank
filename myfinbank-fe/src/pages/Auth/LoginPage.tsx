// src/pages/Auth/LoginPage.tsx
import { useForm } from 'react-hook-form'
import { useMutation } from '@tanstack/react-query'
import { useAppDispatch } from '../../app/hooks'
import { setCredentials } from '../../features/auth/authSlice'
import { authApi } from '../../api/authApi'
import {
    Container,
    TextField,
    Button,
    Box,
    Typography,
    Alert,
    CircularProgress,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'

// form type: email + password
type FormData = { username: string; password: string }

type ResponseData = {
    username?: string
    accessToken?: string
    access_token?: string
    token?: string
    refreshToken?: string
    refresh_token?: string
    user?: { username: string; }
}

export default function LoginPage() {
    const { register, handleSubmit } = useForm<FormData>()
    const dispatch = useAppDispatch()
    const navigate = useNavigate()

    // nuova sintassi React Query v5
    const mutation = useMutation<ResponseData, Error, FormData>({
        mutationFn: (data: FormData) => authApi.login(data),
        onSuccess: (data) => {
            const accessToken = data.accessToken || data.access_token || data.token
            const refreshToken = data.refreshToken || data.refresh_token
            const userFromBody = data.user || (data.username ? { username: data.username } : null)

            const user = userFromBody || { username: '' }

            dispatch(setCredentials({ user, accessToken, refreshToken }))

            navigate('/dashboard') // Redirect dopo login
        },
    })

    const onSubmit = (data: FormData) => mutation.mutate(data)

    return (
        <Container maxWidth="sm">
            <Box sx={{ mt: 8 }}>
                <Typography variant="h5" gutterBottom>
                    Login
                </Typography>

                {mutation.isError && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {(mutation.error as any)?.response?.data || 'Credenziali non valide'}
                    </Alert>
                )}

                <form onSubmit={handleSubmit(onSubmit)}>
                    <TextField
                        label="Username"
                        fullWidth
                        margin="normal"
                        {...register('username', { required: true })}
                    />
                    <TextField
                        label="Password"
                        type="password"
                        fullWidth
                        margin="normal"
                        {...register('password', { required: true })}
                    />

                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 2 }}>
                        <Button type="submit" variant="contained" disabled={mutation.isPending}>
                            {mutation.isPending ? <CircularProgress size={20} /> : 'Accedi'}
                        </Button>
                        <Button
                            sx={{ ml: 2 }}
                            variant="text"
                            onClick={() => navigate('/auth/register')}
                        >
                            Registrati
                        </Button>
                    </Box>
                </form>
            </Box>
        </Container>
    )
}
