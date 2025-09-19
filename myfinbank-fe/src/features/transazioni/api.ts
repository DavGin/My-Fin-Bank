import axiosClient from "../../api/axiosClient.ts";
import type { Conto } from "../Conti/api.ts";

export type Transazioni = {
    id: number;
    importo: number;
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
