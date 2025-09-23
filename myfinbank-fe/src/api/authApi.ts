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
export type RefreshResponse = {
    accessToken: string;
    refreshToken: string;
};

export async function refresh(token: string): Promise<RefreshResponse> {
    console.log('Effettuando login con:', token);
    const res =  await axiosClient.post(`/auth/refres/${token}`); // Assicura che `ResponseData` sia specificato
    return res.data;
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
