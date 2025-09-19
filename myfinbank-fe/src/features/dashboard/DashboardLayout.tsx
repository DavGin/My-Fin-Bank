// src/features/dashboard/DashboardLayout.tsx
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
import HomeIcon from '@mui/icons-material/Home';
import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { logout } from '../auth/authSlice'
import UserMenu from "../auth/UserMenu.tsx";

const drawerWidth = 240

const menuItems = [
    { label: 'Menu', path: '/', icon: <HomeIcon /> },
    { label: 'Conti', path: '/conti', icon: <AccountBalanceIcon /> },
    { label: 'Operazioni', path: '/operazioni', icon: <SwapHorizIcon /> },
    { label: 'Mutui', path: '/loans', icon: <AssignmentIcon /> },
    { label: 'Investimenti', path: '/investments', icon: <SavingsIcon /> },

]

export default function DashboardLayout() {
    const navigate = useNavigate()
    const dispatch = useAppDispatch()
    const user = useAppSelector(state => state.auth.user)

    const handleLogout = () => {
        dispatch(logout())
        navigate('/login')
    }

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
                        {menuItems.map((item) => (
                            <ListItem key={item.label} disablePadding>
                                <ListItemButton onClick={() => navigate(item.path)}>
                                    <ListItemIcon>{item.icon}</ListItemIcon>
                                    <ListItemText primary={item.label} />
                                </ListItemButton>
                            </ListItem>
                        ))}
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
