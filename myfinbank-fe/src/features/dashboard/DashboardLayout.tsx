import { Outlet, useNavigate } from 'react-router-dom'
import {
    Box,
    CssBaseline,
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Toolbar,
    AppBar,
    Typography,
} from '@mui/material'
import AccountBalanceIcon from '@mui/icons-material/AccountBalance'
import SwapHorizIcon from '@mui/icons-material/SwapHoriz'
import SavingsIcon from '@mui/icons-material/Savings'
import AssignmentIcon from '@mui/icons-material/Assignment'
import LogoutIcon from '@mui/icons-material/Logout'
import HomeIcon from '@mui/icons-material/Home'
import SecurityIcon from '@mui/icons-material/Security'
import { useAppSelector } from '../../app/hooks'
import UserMenu from '../auth/UserMenu.tsx'
import {performLogout} from "../../api/axiosClient.ts";

const drawerWidth = 240

const userMenuItems = [
    { label: 'Menu', path: '/', icon: <HomeIcon /> },
    { label: 'Conti', path: '/conti', icon: <AccountBalanceIcon /> },
    { label: 'Operazioni', path: '/operazioni', icon: <SwapHorizIcon /> },
    { label: 'Mutui', path: '/mutui', icon: <AssignmentIcon /> },
    { label: 'Investimenti', path: '/investimenti', icon: <SavingsIcon /> },
]

const adminMenuItems = [
    { label: 'Dashboard Admin', path: '/admin/dashboard', icon: <HomeIcon /> },
    { label: 'Gestione Mutui', path: '/admin/mutui', icon: <SecurityIcon /> },
    { label: 'Profilo', path: '/profile', icon: <AccountBalanceIcon /> },
]


export default function DashboardLayout() {
    const navigate = useNavigate()
    const user = useAppSelector(state => state.auth.user)

    const handleLogout = async () => {
        const refreshToken = localStorage.getItem('refreshToken') ?? '';
        console.log('[HANDLE_LOGOUT] Tentativo di logout con token:', refreshToken);
        await performLogout(navigate);

    }

    const menuItems = user?.ruolo === 'ADMIN' ? adminMenuItems : userMenuItems


    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />

            {/* Top bar */}
            <AppBar
                position="fixed"
                sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
            >
                <Toolbar>
                    <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
                        MyFinBank
                    </Typography>
                    <Typography variant="body1">
                        {user?.nome} {user?.cognome}
                    </Typography>
                    {user && <UserMenu />}
                </Toolbar>
            </AppBar>

            {/* Sidebar */}
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
                }}
            >
                <Toolbar />
                <Box sx={{ overflow: 'auto' }}>
                    <List>

                        {/* Loop sui menu */}
                        {menuItems.map((item) => (
                            <ListItem key={item.label} disablePadding>
                                <ListItemButton onClick={() => navigate(item.path)}>
                                    <ListItemIcon>{item.icon}</ListItemIcon>
                                    <ListItemText primary={item.label} />
                                </ListItemButton>
                            </ListItem>
                        ))}

                        {/* Logout */}
                        <ListItem disablePadding>
                            <ListItemButton onClick={handleLogout}>
                                <ListItemIcon><LogoutIcon /></ListItemIcon>
                                <ListItemText primary="Logout" />
                            </ListItemButton>
                        </ListItem>
                    </List>
                </Box>
            </Drawer>

            {/* Main content */}
            <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
                <Toolbar /> {/* spazio sotto AppBar */}
                <Outlet />
            </Box>
        </Box>
    )
}
