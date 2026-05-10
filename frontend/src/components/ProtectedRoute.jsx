import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
    const token = localStorage.getItem('token');

    if (!token) {
        alert("Morate biti ulogovani da biste pristupili ovoj stranici!");
        return <Navigate to="/login" replace />;
    }

    return children;
};

export default ProtectedRoute;