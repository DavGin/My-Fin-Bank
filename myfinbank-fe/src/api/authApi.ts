import axiosClient from "./axiosClient.ts";


export type ResponseData = {
    username?: string
    accessToken?: string
    access_token?: string
    token?: string
    refreshToken?: string
    refresh_token?: string
    user?: { username: string; }
}

type FormData = { username: string; password: string; role: string; }

export async function login(data: FormData): Promise<ResponseData> {
    console.log('Effettuando login con:', data);
    const res = await axiosClient.post('/auth/login', data); // Assicura che `ResponseData` sia specificato
    return res.data;
}
export async function refresh(token: string): Promise<ResponseData> {
    try {
        const res = await axiosClient.post(`/auth/refresh`, { token }, { withCredentials: true });
        return res.data; // Restituisci i dati della risposta
    } catch (error) {
        console.error('Errore durante il refresh del token:', error);
        throw error;
    }
}

export type Registrazione = {
    username: string
    email: string
    password: string
    nome: string
    cognome: string
    codiceFiscale: string
    dataNascita: string
    isAdmin: boolean
    ruolo: string
}

export async function registrazione(data: Registrazione): Promise<Registrazione> {
    console.log('Effettuando login con:', data);
    const res = await axiosClient.post('/auth/register', data); // Assicura che `ResponseData` sia specificato
    return res.data;
}

export async function logoutApi(): Promise<void> {
    const res = await axiosClient.post('/auth/logout');
    return res.data
}
