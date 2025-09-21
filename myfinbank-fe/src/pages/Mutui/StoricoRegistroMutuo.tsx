import { useQuery } from "@tanstack/react-query"
import { getListaStoricoRegistroMutuo } from "../../features/Mutui/api"
import { Table, TableHead, TableRow, TableCell, TableBody } from "@mui/material"

export default function StoricoRegistroMutuo() {
    const { data: storico } = useQuery({
        queryKey: ["storicoMutui"],
        queryFn: getListaStoricoRegistroMutuo,
    })

    if (!storico) return <div>Caricamento...</div>

    return (
        <Table>
            <TableHead>
                <TableRow>
                    <TableCell>Numero pratica</TableCell>
                    <TableCell>Vecchio stato</TableCell>
                    <TableCell>Nuovo stato</TableCell>
                    <TableCell>Motivo</TableCell>
                    <TableCell>Data</TableCell>
                    <TableCell>Admin ID</TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {storico.map((s) => (
                    <TableRow key={s.id}>
                        <TableCell>{s.numeroPratica}</TableCell>
                        <TableCell>{s.oldStato}</TableCell>
                        <TableCell>{s.newStato}</TableCell>
                        <TableCell>{s.motivo}</TableCell>
                        <TableCell>
                            {new Date(s.dataCambio).toLocaleString("it-IT")}
                        </TableCell>
                        <TableCell>{s.adminId}</TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    )
}
