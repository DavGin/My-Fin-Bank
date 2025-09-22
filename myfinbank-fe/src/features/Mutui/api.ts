import axiosClient from '../../api/axiosClient'

export type Mutuo = {
    schedule: any;
    id:number;
   numeroPratica: string;
      importo: number;
      durataMesi: number;
      tassoInteresse: number;
      dataCreazione: string;
      motivoMutuo: string;
      stato: string;
      motivoRifiuto: string;
      importoRata: number;
}

export type CreateMutuoInput = {
    id: number
    importo: number
    durataMesi: number
    tassoInteresse: number
}

export async function fetchListaMutui(): Promise<Mutuo[]> {
    const res = await axiosClient.get('/v1/mutui/listMutuiUtente')
    return res.data
}

export async function createMutuo(data: CreateMutuoInput): Promise<Mutuo> {
    const res = await axiosClient.post('/v1/mutui/createMutuo', data)
    return res.data
}

export async function fetchAllMutui(): Promise<Mutuo[]> {
    const res = await axiosClient.get('/v1/mutui/getListaStoricoRegistroMutuo')
    return res.data
}


export async function rigiutaMutuo(numeroPratica: string, stato: 'RIFIUTATA', motivo: string): Promise<Mutuo> {
    const res = await axiosClient.put(`/v1/mutui/rejectMutuo/${numeroPratica}/${stato}/${motivo}`);
    return res.data
}

export async function fetchMutuoDetail(numeroPratica: string): Promise<Mutuo> {
    const res = await axiosClient.get(`/v1/mutui/getMutuo/${numeroPratica}`)
    return res.data
}

export type SimulationInput = {
    importo: number
    durataMesi: number
    tassoInteresse: number
    motivo: string
}

export type SimulationRow = {
      numeroRata:number;
      scadenza:string;
      quotaCapitale:number;
      interessi:number;
      rataTotale:number;
      saldoRimanente:number;
      statoRata:string;
}

export async function simulateMutuo(data: SimulationInput): Promise<SimulationRow[]> {
    const res = await axiosClient.post('/v1/mutui/simulatoreMutuo', data)
    return res.data
}

export interface Rata {
    numeroRata: number
    quotaCapitale: number
    interessi: number
    rataTotale: number
    saldoRimanente: number
    scadenza: string
    statoRata: 'PAGATO' | 'SCADUTO' | 'DA_PAGARE'
}

export async function calcolaRata(numeroPratica: string): Promise<Rata[]> {
    const res = await axiosClient.get(`/v1/mutui/calcolaRata/${numeroPratica}`)
    return res.data
}
export async function pagaRata(numeroPratica: string, numeroRata: number, numeroConto: string): Promise<void> {
    await axiosClient.post(`/v1/mutui/pagaRata/${numeroPratica}/${numeroRata}/${numeroConto}`)
}

export async function fetchRateByNumeroPratica(numeroPratica: string): Promise<Rata[]> {
    const res = await axiosClient.get(`/v1/mutui/findByNumeroPratica/${numeroPratica}`);
    return res.data;
}

export interface MutuoRequestDto {
    numeroPratica: string
    importo: number
    durataMesi: number
    tassoInteresse: number
    stato: string
    motivo?: string
    dataRichiesta: string
    userId: number
}

export interface RegistroMutuoDto {
    id: number
    numeroPratica: string
    oldStato: string
    newStato: string
    motivo: string
    dataCambio: string
    adminId: number
}

// ----------------- API -----------------

export async function getMutuo(numeroPratica: string): Promise<MutuoRequestDto> {
    const res = await axiosClient.get(`/v1/mutui/getMutuo/${numeroPratica}`)
    return res.data
}


export interface EsitoMutuoDto{
    numeroPratica: string
    newStato: string
    motivo: string
}

export async function approvaMutuo( data: EsitoMutuoDto): Promise<EsitoMutuoDto[]> {
    const res = await axiosClient.post(`/v1/mutui/approvaMutuo`, data)
    return res.data
}

export async function rejectMutuo(data: EsitoMutuoDto): Promise<EsitoMutuoDto[]> {
    const res = await axiosClient.post(`/v1/mutui/rejectMutuo`, data)
    return res.data
}

export async function getStoricoRegistroMutuo(
    numeroPratica: string
): Promise<RegistroMutuoDto[]> {
    const res = await axiosClient.get(`/v1/mutui/getStoricoRegistroMutuo/${numeroPratica}`)
    return res.data
}

export async function getListaStoricoRegistroMutuo(): Promise<MutuoRequestDto[]> {
    const res = await axiosClient.get(`/v1/mutui/getListaStoricoRegistroMutuo`)
    return res.data
}
