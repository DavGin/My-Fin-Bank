import axiosClient from '../../api/axiosClient'

export type Conto = {
    id: number
    numeroConto: string
    iban: string
    saldo: number
    tipo: string
}

export async function fetchListaConti(): Promise<Conto[]> {
    try {
        console.log('Fetching conti...')
        const res = await axiosClient.get('/conti/listaConti')
        console.log('Fetch conti successful', res.data)
        return res.data
    } catch (error) {
        console.log('Error fetching conti', error)
        throw error
    }
}

export type CreateContoInput = {
    id: number
    numeroConto: string,
    tipo: string,
    iban: string,
    valuta: string,
    saldo: 0
}

export async function createConto(data: CreateContoInput): Promise<Conto> {
    try {
        console.log('Creating conto with data:', data)
        const res = await axiosClient.post('/conti/createConto', data)
        console.log('Create conto successful', res.data)
        return res.data
    } catch (error) {
        console.log('Error creating conto', error)
        throw error
    }
}
