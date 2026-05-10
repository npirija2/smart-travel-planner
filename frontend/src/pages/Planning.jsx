import React, { useState, useEffect } from 'react';
import { getTravelPlans, createTravelPlan } from '../api/planService';

const Planning = () => {
    const [plans, setPlans] = useState([]);
    const [formData, setFormData] = useState({
        name: '',
        startDate: '',
        endDate: '',
        destinationId: 1, 
        description: '',
        status: 'PENDING' 
    });

    useEffect(() => {
        loadPlans();
    }, []);

    const loadPlans = async () => {
        try {
            const data = await getTravelPlans();
            setPlans(data);
        } catch (err) {
            console.error("Greška pri učitavanju planova", err);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await createTravelPlan(formData);
            alert("Plan kreiran! Čeka se potvrda sistema (Saga)...");
            loadPlans(); 
        } catch (err) {
            alert("Greška: " + (err.response?.data?.message || "Neuspješno"));
        }
    };

    return (
        <div style={{ padding: '20px' }}>
            <h2>Moji Planovi Putovanja</h2>
            
            <form onSubmit={handleSubmit} style={{ marginBottom: '30px', border: '1px solid #ddd', padding: '15px' }}>
                <h3>Novi Plan</h3>
                <input type="text" placeholder="Naziv plana" required
                    onChange={e => setFormData({...formData, name: e.target.value})} />
                <input type="date" required
                    onChange={e => setFormData({...formData, startDate: e.target.value})} />
                <input type="date" required
                    onChange={e => setFormData({...formData, endDate: e.target.value})} />
                <textarea placeholder="Opis" 
                    onChange={e => setFormData({...formData, description: e.target.value})} />
                <button type="submit">Kreiraj Plan</button>
            </form>

            {/* LISTA PLANOVA */}
            <table border="1" width="100%" style={{ borderCollapse: 'collapse' }}>
                <thead>
                    <tr>
                        <th>Naziv</th>
                        <th>Destinacija</th>
                        <th>Period</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    {plans.map(plan => (
                        <tr key={plan.id}>
                            <td>{plan.name}</td>
                            <td>{plan.destinationName || plan.destinationId}</td>
                            <td>{plan.startDate} do {plan.endDate}</td>
                            <td style={{ 
                                color: plan.status === 'REJECTED' ? 'red' : 
                                       plan.status === 'CONFIRMED' ? 'green' : 'orange' 
                            }}>
                                {plan.status}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default Planning;