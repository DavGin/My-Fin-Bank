import axiosClient from '../../api/axiosClient'

export type Investimento = {
    id:number;
    identificativo: string;
    tipoInvestimento:string;
    importoInvestito:number;
    tassoRitornoPrevisto:number; // es. 5% annuo
    statoInvestimento: string;
    dataInizio: string;
    dataFine:string;
    mesi:number;
    importoTotale:number
}

export type CreateInvestimentoInput = {
    tipoInvestimento: 'AZIONI' | 'OBBLIGAZIONI' | 'FONDI' | 'ETF'
    importoInvestito: number
    dataInizio: string
    durataMesi: number
    tassoRitornoPrevisto:number
}

// export type SimulazioneInvestimentoOutputDto = {
//     id:number;
//     identificativo: string;
//     tipoInvestimento:string;
//     importoInvestito:number;
//     tassoRitornoPrevisto:number; // es. 5% annuo
//     statoInvestimento: string;
//     dataInizio: string;
//     dataFine:string;
//     mesi:number;
//     importoTotale:number
// }

export async function fetchInvestimenti(): Promise<Investimento[]> {
    const res = await axiosClient.get('/v1/investimento/getUserInvestments')
    return res.data
}

export async function createInvestimento(data: CreateInvestimentoInput): Promise<Investimento> {
    const res = await axiosClient.post('/v1/investimento/createInvestimento', data)
    return res.data
}

export async function fetchInvestimentoById(identificativo: string, mesi:string): Promise<Investimento> {
    const res = await axiosClient.get(`/v1/investimento/proiezioneInvestimento/${identificativo}/${mesi}`)
    return res.data
}

export async function closeInvestimento(identificativo: string): Promise<Investimento> {
    const res = await axiosClient.post(`/v1/investimento/chiudiInvestimento/${identificativo}`)
    return res.data
}

export type SimulationInvestimentoInput = {
    importoIniziale: number
    mesi: number
    tassoPrevisto: number

}

export type SimulazioneInvestimentoOutputDto = {
    importoIniziale: number;
    tassoPrevisto: number;
    mesi: number;
    importoFinale:number;
}

export async function simulateInvestimento(data: SimulationInvestimentoInput): Promise<SimulazioneInvestimentoOutputDto> {
    const res = await axiosClient.post('/v1/investimento/simulaInvestimento', data)
    return res.data
}

type Rendimento = {
    periodo: string;
    valoreIniziale: number;
    rendimentoMaturato: number;
    valoreAttuale: number;
};

export async function rendimentoInvestimento(identificativo: string): Promise<Investimento> {
    const res = await axiosClient.post(`/v1/investimento/getStoricoRendimenti/${identificativo}`)
    return res.data
}


// TODO Lista investimenti di tutti gli utenti
export async function fetchAllInvestimenti(): Promise<Investimento[]> {
    const res = await axiosClient.get('/admin/investimenti')
    return res.data
}

// TODO Chiusura investimento come admin
export async function adminCloseInvestimento(id: number): Promise<Investimento> {
    const res = await axiosClient.patch(`/admin/investimenti/${id}/chiudi`)
    return res.data
}

// TODO (Opzionale) Approvazione investimento
export async function approveInvestimento(id: number): Promise<Investimento> {
    const res = await axiosClient.patch(`/admin/investimenti/${id}/approva`)
    return res.data
}

