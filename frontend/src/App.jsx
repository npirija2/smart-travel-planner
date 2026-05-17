import React, { useEffect, useState } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Planning from './pages/Planning';
import Dashboard from './pages/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';
import AppLayout from './components/AppLayout';
import {
    ActivitySchedulingModule,
    ActivityVotingModule,
    AttractionRecommendationsModule,
    BudgetManagementModule,
    CollaborativePlanningModule,
    LocalRecommendationsModule,
    NotificationsModule,
    OfflineAccessModule,
    ReservationManagementModule,
    RouteOptimizationModule,
    ScheduleLoadModule,
    TravelPlanSharingModule,
    WaitingTimeModule,
    WeatherForecastModule,
} from './pages/WorkspaceModules';
import './App.css';

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('token'));

    useEffect(() => {
        const checkLogin = () => {
            setIsLoggedIn(!!localStorage.getItem('token'));
        };

        window.addEventListener('storage', checkLogin);
        window.addEventListener('auth-change', checkLogin);

        return () => {
            window.removeEventListener('storage', checkLogin);
            window.removeEventListener('auth-change', checkLogin);
        };
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.dispatchEvent(new Event('auth-change'));
    };

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/" element={<AppLayout isLoggedIn={isLoggedIn} onLogout={handleLogout} />}>
                    <Route index element={<Dashboard isLoggedIn={isLoggedIn} />} />
                    <Route
                        path="planning"
                        element={
                            <ProtectedRoute>
                                <Planning />
                            </ProtectedRoute>
                        }
                    />
                    <Route path="route-optimization" element={<ProtectedRoute><RouteOptimizationModule /></ProtectedRoute>} />
                    <Route path="activity-scheduling" element={<ProtectedRoute><ActivitySchedulingModule /></ProtectedRoute>} />
                    <Route path="attractions" element={<ProtectedRoute><AttractionRecommendationsModule /></ProtectedRoute>} />
                    <Route path="weather" element={<ProtectedRoute><WeatherForecastModule /></ProtectedRoute>} />
                    <Route path="budget" element={<ProtectedRoute><BudgetManagementModule /></ProtectedRoute>} />
                    <Route path="collaborative" element={<ProtectedRoute><CollaborativePlanningModule /></ProtectedRoute>} />
                    <Route path="voting" element={<ProtectedRoute><ActivityVotingModule /></ProtectedRoute>} />
                    <Route path="reservations" element={<ProtectedRoute><ReservationManagementModule /></ProtectedRoute>} />
                    <Route path="offline" element={<ProtectedRoute><OfflineAccessModule /></ProtectedRoute>} />
                    <Route path="notifications" element={<ProtectedRoute><NotificationsModule /></ProtectedRoute>} />
                    <Route path="schedule-load" element={<ProtectedRoute><ScheduleLoadModule /></ProtectedRoute>} />
                    <Route path="local-recommendations" element={<ProtectedRoute><LocalRecommendationsModule /></ProtectedRoute>} />
                    <Route path="share" element={<ProtectedRoute><TravelPlanSharingModule /></ProtectedRoute>} />
                    <Route path="waiting-times" element={<ProtectedRoute><WaitingTimeModule /></ProtectedRoute>} />
                </Route>

                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
