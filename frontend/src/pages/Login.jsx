import React, { useState } from 'react';
import { loginUser } from '../api/userService';

const Login = () => {
  const [formData, setFormData] = useState({ email: '', password: '' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = await loginUser(formData.email, formData.password);
      
      localStorage.setItem('token', token);
      
      window.dispatchEvent(new Event("storage"));
      
      alert("Prijava uspješna!");

      window.location.href = "/planning"; 

    } catch (error) {
      console.error(error);
      alert("Greška pri prijavi: " + (error.response?.data || "Server nedostupan"));
    }
  };

  return (
    <div style={{ padding: '20px', border: '1px solid #ccc', width: '300px' }}>
      <h2>Smart Travel Planner</h2>
      <form onSubmit={handleSubmit}>
        <input 
          type="email" 
          placeholder="Email" 
          onChange={(e) => setFormData({...formData, email: e.target.value})} 
          style={{ display: 'block', marginBottom: '10px', width: '100%' }}
        />
        <input 
          type="password" 
          placeholder="Lozinka" 
          onChange={(e) => setFormData({...formData, password: e.target.value})} 
          style={{ display: 'block', marginBottom: '10px', width: '100%' }}
        />
        <button type="submit" style={{ width: '100%' }}>Prijavi se</button>
      </form>
    </div>
  );
};

export default Login;