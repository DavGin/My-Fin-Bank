// src/features/conti/api.ts
import axiosClient from '../../api/axiosClient'

export type Conto = {
    id: number
    iban: string
    saldo: number
    tipo: string
}

export async function fetchConti(): Promise<Conto[]> {
    const res = await axiosClient.get('/listaConti')
    return res.data
}
