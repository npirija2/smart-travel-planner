import React, { useState } from 'react';
import api from '../api/api';

const Register = () => {
  const [formData, setFormData] = useState({ username: '', email: '', password: '' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/users', formData);
      alert("Korisnik uspješno kreiran! Sada se možeš ulogovati.");
    } catch (error) {
      alert("Greška: " + (error.response?.data?.message || "Provjeri konzolu"));
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Registracija</h2>
      <form onSubmit={handleSubmit}>
        <input type="text" placeholder="Username" 
               onChange={e => setFormData({...formData, username: e.target.value})} /><br/>
        <input type="email" placeholder="Email" 
               onChange={e => setFormData({...formData, email: e.target.value})} /><br/>
        <input type="password" placeholder="Lozinka" 
               onChange={e => setFormData({...formData, password: e.target.value})} /><br/>
        <button type="submit">Kreiraj usera</button>
      </form>
    </div>
  );
};

export default Register;