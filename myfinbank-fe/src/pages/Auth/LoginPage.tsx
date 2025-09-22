// src/pages/Auth/LoginPage.tsx
import { useForm } from 'react-hook-form'
import {useMutation} from '@tanstack/react-query'
import { useAppDispatch } from '../../app/hooks'
import {setCredentials, setUser} from '../../features/auth/authSlice'
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
import {getProfile} from "../../features/profile/api.ts";

// form type: email + password
type FormData = { username: string; password: string; role: string; }

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

    const mutation = useMutation<ResponseData, Error, FormData>({
        mutationFn: (data: FormData) => {
            console.log('[LoginPage] Tentativo di login con i seguenti dati:', data)
            return authApi.login(data)
        },
        onSuccess: async (data) => {
            console.log('[LoginPage] Login riuscito. Dati restituiti:', data)

            const accessToken = data.accessToken || data.access_token || data.token ||  ''
            console.log('[ACCESS_TOKEN] ----> ', accessToken)
            const refreshToken = data.refreshToken || data.refresh_token
            console.log('[REFRESH_TOKEN] ----> ', refreshToken)
            const userFromBody = data.user ||
                (data.username ? { username: data.username} : null)
            const user = {
            ...(userFromBody || {username: ''}),
                    nome: '',
                    cognome: '',
                    ruolo: '',
                    username: '',
                    email: ''
            }
            console.log('[LoginPage] Dati memorizzati nel Redux store:', { user, accessToken, refreshToken })
            dispatch(setCredentials({ user, accessToken, refreshToken: refreshToken || '' }))

            const profile = await getProfile()
            console.log('[Profile] Dati del profilo:', {profile})
            const newuser = {
                ...(userFromBody || {username: ''}),
                nome: profile.nome,
                cognome: profile.cognome,
                ruolo: profile.ruolo,
                username: profile.username,
                email: profile.email
            }
            console.log('[LoginPage] Dati memorizzati nel Redux store:', { newuser })
            dispatch(setUser(newuser))
            console.log('[PROFILE]----> ', newuser.ruolo)
            if(newuser.ruolo=== 'ADMIN')

                navigate('/admin/dashboard')
            else
                navigate('/')
        },
        onError: (error) => {
            console.error('[LoginPage] Errore durante il login:', error)
        },
    })

    const onSubmit = (data: FormData) => {
        console.log('[LoginPage] Form inviato con dati:', data)
        mutation.mutate(data)
    }

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
