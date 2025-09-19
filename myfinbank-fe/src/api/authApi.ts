import api from './axiosClient'

export const authApi = {
    login: (payload: { username: string; password: string }) => {
        console.log('Effettuando login con:', payload)
        return api.post('/auth/login', payload)
            .then(res => {
                console.log('Risposta login:', res.data)
                return res.data
            })
            .catch(err => {
                console.error('Errore login:', err)
                throw err
            })
    },
    register: (payload: any) => {
        console.log('Effettuando registrazione con payload:', payload)
        return api.post('/auth/register', payload)
            .then(res => {
                console.log('Risposta registrazione:', res.data)
                return res.data
            })
            .catch(err => {
                console.error('Errore registrazione:', err)
                throw err
            })
    },
    refresh: () => {
        console.log('Effettuando richiesta di refresh token')
        return api.post('/auth/refresh')
            .then(res => {
                console.log('Token aggiornato:', res.data)
                return res.data
            })
            .catch(err => {
                console.error('Errore durante il refresh del token:', err)
                throw err
            })
    },
    registrazione: (payload: {
        username: string
        email: string
        password: string
        nome: string
        cognome: string
        codiceFiscale: string
        dataNascita: string
        isAdmin: boolean
    }) => {
        console.log('Effettuando registrazione admin con payload:', payload)
        return api.post('/auth/register', payload)
            .then(res => {
                console.log('Risultato registrazione admin:', res.data)
                return res.data
            })
            .catch(err => {
                console.error('Errore durante la registrazione admin:', err)
                throw err
            })
    },
}
