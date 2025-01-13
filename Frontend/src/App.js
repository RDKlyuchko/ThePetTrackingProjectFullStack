import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import Register from './components/Register';
import Login from './components/Login';
import Profile from './components/Profile';
import AdminDashboard from './components/AdminDashboard';
import RequireAuth from './components/RequireAuth';
import Unauthorized from './components/Unauthorized';
import Navbar from './components/Navbar';
import { AuthProvider } from './contexts/AuthContext';
import CreatePet from './components/CreatePet';
import DetectionLogs from './components/DetectionLogs';
import PetDetailsPage from './components/PetDetailsPage';
import ContainerComponent from './components/ContainerComponent';
import HomeGuest from './components/HomeGuest';
import './styles/Home.css';





function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <div className="container stoplimit centered-container">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/home-guest" element={<HomeGuest />} />
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<ContainerComponent />} />
            <Route path="/pets/:petId" element={<PetDetailsPage />} />
            <Route
              path="/profile"
              element={
                <RequireAuth requiredRoles={['ROLE_USER', 'ROLE_ADMIN']}>
                  <Profile />
                </RequireAuth>
              }
            />
            <Route
              path="/admin-dashboard"
              element={
                <RequireAuth requiredRoles={['ROLE_ADMIN']}>
                  <AdminDashboard />
                </RequireAuth>
              }
            />
            <Route
              path="/create-pet"
              element={
                <RequireAuth requiredRoles={['ROLE_USER', 'ROLE_ADMIN']}>
                  <CreatePet />
                </RequireAuth>
              }
            />
            <Route
              path="/logs/:petId"
              element={
                <RequireAuth requiredRoles={['ROLE_USER', 'ROLE_ADMIN']}>
                  <DetectionLogs />
                </RequireAuth>
              }
            />
            <Route path="/unauthorized" element={<Unauthorized />} />
          </Routes>
          </div>
      </Router>
    </AuthProvider>
  );
}

export default App;