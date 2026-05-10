import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Planning from './pages/Planning';
import ProtectedRoute from './components/ProtectedRoute';

const Dashboard = () => (
  <div style={{ padding: '20px' }}>
    <h1>Dobrodošli na Smart Travel Planner</h1>
    <p>Odaberite opciju iz menija.</p>
  </div>
);

function App() {
  return (
    <Router>
      <nav style={{ padding: '10px', backgroundColor: '#f4f4f4', marginBottom: '20px' }}>
        <Link to="/" style={{ marginRight: '15px' }}>Dashboard</Link>
        <Link to="/login">Prijava</Link>
        <Link to="/register">Registracija</Link>
        <Link to="/planning">Planiranje</Link>
      </nav>

      <div style={{ padding: '20px' }}>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route 
          path="/planning" 
          element={
              <ProtectedRoute>
                  <Planning />
              </ProtectedRoute>
          } 
      />
        </Routes>
      </div>
    </Router>
  );
}

export default App;