import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';

type Props = {
    open: boolean;
    secondsLeft?: number | null;
    onRefresh: () => void;
    onLogout: () => void;
};

export default function SessionExpiryDialog({ open, secondsLeft, onRefresh, onLogout }: Props) {
    return (
        <Dialog open={open}>
            <DialogTitle>Sessione in scadenza</DialogTitle>
            <DialogContent>
                <Typography>
                    La tua sessione scadrà tra {secondsLeft ? Math.max(0, Math.round(secondsLeft)) : 'alcuni'} secondi.
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    Vuoi rinnovare la sessione per continuare?
                </Typography>
            </DialogContent>
            <DialogActions>
                <Button onClick={onLogout} color="error">Logout</Button>
                <Button onClick={onRefresh} variant="contained">Continua</Button>
            </DialogActions>
        </Dialog>
    );
}
