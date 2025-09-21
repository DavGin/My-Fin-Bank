import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchInvestimentoById,
  closeInvestimento,
  type Investimento,
} from '../../features/Investimenti/api';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Divider,
  Button,
  Table,
  TableBody,
  TableRow,
  TableCell,
} from '@mui/material';
import InvestimentoRendimentoPage from './InvestimentoRendimentoPage';


export default function InvestimentoDetailPage() {
  const navigate = useNavigate();
  const { identificativo, durataMesi } = useParams<{ identificativo: string; durataMesi: string }>();

  const queryClient = useQueryClient();
  
  console.log('Pagina di dettaglio caricata con identificativo:', identificativo, 'e durata mesi:', durataMesi);

  const { data, isLoading, isError } = useQuery<Investimento>({
    queryKey: ['investimento', identificativo],
    queryFn: () => fetchInvestimentoById(identificativo, durataMesi),
    onSuccess: (data) => {
      console.log('Dati investimento caricati:', data);
    },
    onError: (error) => {
      console.error('Errore durante il caricamento dell\'investimento:', error);
    },
  });

  const mutation = useMutation({
    mutationFn: () => closeInvestimento(identificativo),
    onMutate: () => {
      console.log('Inizio chiusura per investimento con ID:', identificativo);
    },
    onSuccess: () => {
      console.log('Investimento chiuso correttamente. Invalidazione cache in corso...');
      queryClient.invalidateQueries({ queryKey: ['investimento', identificativo] });
      queryClient.invalidateQueries({ queryKey: ['investimenti'] });
    },
    onError: (error) => {
      console.error('Errore nella chiusura dell\'investimento con ID:', identificativo, 'Errore:', error);
    },
  });

  if (isLoading) return <CircularProgress />;
  if (isError) return <Alert severity="error">Errore caricamento investimento</Alert>;
  if (!data) {
    console.warn('Investimento non trovato per ID:', identificativo);
    return <Alert severity="warning">Investimento non trovato</Alert>;
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Dettaglio Investimento #{data.identificativo}
      </Typography>

      <Card>
        <CardContent>
          <Typography variant="h6">Informazioni principali</Typography>
          <Divider sx={{ my: 2 }} />

          <Table>
            <TableBody>
              <TableRow>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell>{data.identificativo}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell><strong>Tipo</strong></TableCell>
                <TableCell>{data.tipoInvestimento}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell><strong>Importo</strong></TableCell>
                <TableCell>{data.importoInvestito}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell><strong>Durata mesi</strong></TableCell>
                <TableCell>{data.mesi}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell><strong>Data Apertura</strong></TableCell>
                <TableCell>{new Date(data.dataInizio).toLocaleDateString('it-IT')}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell><strong>Data Chiusura</strong></TableCell>
                <TableCell>{new Date(data.dataFine).toLocaleDateString('it-IT')}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell><strong>Rendimento stimato</strong></TableCell>
                <TableCell>{data.tassoRitornoPrevisto}%</TableCell>
              </TableRow>
              <TableRow>
                <TableCell><strong>Stato</strong></TableCell>
                <TableCell>{data.statoInvestimento}</TableCell>
              </TableRow>
            </TableBody>
          </Table>

          {/* Bottone di chiusura solo se attivo */}
          {data.statoInvestimento === 'ACTIVE' && (
            <Button
              variant="contained"
              color="error"
              sx={{ mt: 3 }}
              onClick={() => mutation.mutate()}
              disabled={mutation.isPending}
            >
              {mutation.isPending ? 'Chiusura in corso...' : 'Chiudi investimento'}
            </Button>
          )}

          {mutation.isError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              Errore nella chiusura dell’investimento
            </Alert>
          )}
          {mutation.isSuccess && (
            <Alert severity="success" sx={{ mt: 2 }}>
              Investimento chiuso correttamente
            </Alert>
          )}
        </CardContent>
      </Card>
      <InvestimentoRendimentoPage identificativo={Number(identificativo)} />
    </Box>
  );
}
