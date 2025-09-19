import axiosClient from "../../api/axiosClient.ts";
import type { Conto } from "../Conti/api.ts";

export type Transazioni = {
    id: number;
    importo: number;
    tipoTransazione: string
    descrizione: string;
    data: string;
};

export async function fetchConto(numeroConto: string): Promise<Conto> {
    try {
        const res = await axiosClient.get(`/conti/findByNumeroConto/${numeroConto}`);
        return res.data;
    } catch (error) {
        console.error("Errore durante il fetch del conto:", error);
        throw error;
    }
}

export async function fetchTransazioniByConto(numeroConto: string): Promise<Transazioni[]> {
    try {
        const res = await axiosClient.get(`/v1/transazioni/listTransazioni/${numeroConto}`);
        return res.data;
    } catch (error) {
        console.error("Errore durante il fetch delle transazioni:", error);
        throw error;
    }
}


export type CreateTransactionInput = {
    id: number
    importo: number
    descrizione: string
    data: string
    tipoTransazione: 'BONIFICO' | 'VERSAMENTO' | 'PRELIEVO' | 'PAGAMENTO'
    valuta: string,
    dataTransazione: string,
    numeroConto: string,
    targetIban: string
}


export async function createTransazione(data: CreateTransactionInput): Promise<Transazioni> {
    try {
        console.log("Creazione transazione con data:", data);

        const res = await axiosClient.post(`/v1/transazioni/creaTransazione`, data)
        return res.data
    }catch (error) {
        console.error("Errore durante il fetch delle transazioni:", error);
        throw error;

    }

}
