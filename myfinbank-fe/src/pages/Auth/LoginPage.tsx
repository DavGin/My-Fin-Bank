// src/pages/Auth/LoginPage.tsx
import { useForm } from 'react-hook-form'
import {useMutation} from '@tanstack/react-query'
import { useAppDispatch } from '../../app/hooks'
import {setCredentials, setUser} from '../../features/auth/authSlice'
import {login, type ResponseData} from '../../api/authApi'
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
import {useState} from "react";
import "../../theme/loginPage.css";

// form type: email + password
type FormData = { username: string; password: string; role: string; }



export default function LoginPage() {
    const {register, handleSubmit} = useForm<FormData>()
    const dispatch = useAppDispatch()
    const navigate = useNavigate()

    // Stato per messaggi di errore
    const [errore, setError] = useState<string | null>(null);


    const mutation = useMutation<ResponseData, unknown, FormData>({

        mutationFn: login,

        onSuccess: async (data) => {
            console.log('[LoginPage] Login riuscito. Dati restituiti:', data)

            const accessToken = data.accessToken || data.access_token || data.token || ''
            console.log('[ACCESS_TOKEN] ----> ', accessToken)
            const refreshToken = data.refreshToken || data.refresh_token
            console.log('[REFRESH_TOKEN] ----> ', refreshToken)
            const userFromBody = data.user ||
                (data.username ? {username: data.username} : null)
            const user = {
                ...(userFromBody || {username: ''}),
                nome: '',
                cognome: '',
                ruolo: '',
                username: '',
                email: ''
            }
            console.log('[LoginPage] Dati memorizzati nel Redux store:', {user, accessToken, refreshToken})
            dispatch(setCredentials({user, accessToken, refreshToken: refreshToken || ''}))

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
            console.log('[LoginPage] Dati memorizzati nel Redux store:', {newuser})
            dispatch(setUser(newuser))
            console.log('[PROFILE]----> ', newuser.ruolo)
            if (newuser.ruolo === 'ADMIN')

                navigate('/admin/dashboard')
            else
                navigate('/')
        },
        onError: (errore: any) => {
            console.error('[LoginPage] Errore durante il login:', errore)
            // Controlla se l'errore ha una risposta del backend
            if (errore.response && errore.response.data) {
                console.error('[LoginPage] Dettagli errore del backend:', errore.response.data);
            }
            // Salva il messaggio di errore generico o specifico per visualizzarlo a video
            setError(
                errore.response?.data?.message || 'Credenziali non valide. Riprova.'
            );
        },
    })

    const onSubmit = (data: FormData) => {
        console.log('[LoginPage] Form inviato con dati:', data)
        mutation.mutate(data)

    }

    return (

        <Box sx={{display: 'flex', flexDirection: 'column', minHeight: '100vh', marginTop:'60px'}}>
            {/* Header */}
            <Box>
                <Typography
                    variant="h3"
                    textAlign="center"
                    sx={{
                        color: 'blue',           // Colore del testo
                        fontFamily: 'Arial',     // Font
                        fontWeight: 'bold',      // (Opzionale) Peso del font
                    }}
                >
                    My Fin Bank
                </Typography>
            </Box>

            <Box sx={{padding: 3, marginTop:'30px'}}>

                <Container
                    maxWidth="lg"
                    sx={{
                        width: '550px',
                        height: '500px',
                        backgroundColor: 'white', // Sfondo del container
                        borderRadius: '8px',
                        boxShadow: 3,
                        padding: '2rem',
                    }}
                >
                    <Box sx={{mt: 0, padding:0}}>
                        <Box>
                            <Typography  variant="h5"
                                         textAlign="center"
                                         sx={{
                                             color: 'black',           // Colore del testo
                                             fontFamily: 'Arial',     // Font

                                         }}>
                                Accedi al tuo account
                            </Typography>
                        </Box>
                        <Box sx={{mt: 2, mb: 2}}>
                            {/* Messaggio d'errore */}
                            {errore && (
                                <Alert severity="error" sx={{mb: 2}}>
                                    {/* Label personalizzata */}
                                    <Typography variant="subtitle2" color="error" fontWeight="bold">
                                        Credenziali errate
                                    </Typography>
                                </Alert>
                            )}


                            <form onSubmit={handleSubmit(onSubmit)}>
                                <TextField
                                    fullWidth
                                    margin="normal"
                                    InputProps={{
                                        placeholder: 'Username', // Placeholder centrato agisce come etichetta
                                        style: {
                                            textAlign: 'center', // Testo centrato
                                            fontSize: '20px',
                                                    // Modifica grandezza del testo
                                        },
                                    }}
                                    {...register('username', {required: true})} />
                                <TextField
                                    type="password"
                                    fullWidth
                                    margin="normal"
                                    InputProps={{
                                        placeholder: 'Password', // Placeholder centrato agisce come etichetta
                                        style: {
                                            textAlign: 'center', // Testo centrato
                                            fontSize: '20px',
                                            // Modifica grandezza del testo
                                        },
                                    }}
                                    {...register('password', {required: true})} />

                                <Box sx={{display: 'flex', alignItems: 'center', mt: 2}}>
                                    <Button sx={{
                                        mt: 2,
                                        width: '500px',       // Larghezza
                                        height: '50px',       // Altezza
                                        fontSize: '16px',

                                    }}
                                            type="submit" variant="contained" disabled={mutation.isPending}>
                                        {mutation.isPending ? <CircularProgress size={20}/> : 'Accedi'}
                                    </Button>
                                </Box>
                                <Box sx={{mt: 3}}>
                                    <Typography sx={{
                                        variant:"h7",
                                        color: 'black',           // Colore del testo
                                        fontFamily: 'Arial',     // Font
                                        m:2
                                    }}>
                                        ---------------------------------------- o ------------------------------------------
                                    </Typography>
                                </Box>
                                <Box>
                                    <Button
                                        sx={{ mt: 2,
                                            width: '500px',       // Larghezza
                                            height: '50px',       // Altezza
                                            fontSize: '16px',
                                            backgroundColor: 'green', // Colore di sfondo personalizzato
                                            color: 'white',             // Colore del testo

                                        }}
                                        variant="contained"
                                        onClick={() => navigate('/auth/register')}
                                    >
                                        Crea un nuovo account
                                    </Button>
                                </Box>
                            </form>
                        </Box>
                    </Box>
                </Container>

            </Box>
        </Box>

    );
}
