import React, { useState, useEffect } from 'react';
import { getTravelPlans, createTravelPlan } from '../api/planService';
import { getDestinations } from '../api/destinationService';

const Planning = () => {
    const [plans, setPlans] = useState([]);
    const [destinations, setDestinations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [formData, setFormData] = useState({
        name: '',
        startDate: '',
        endDate: '',
        destinationId: '',
        description: '',
        status: 'PENDING'
    });

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const [plansData, destData] = await Promise.all([
                getTravelPlans(),
                getDestinations()
            ]);
            setPlans(plansData);
            setDestinations(destData);
            
            if (destData.length > 0) {
                setFormData(prev => ({ ...prev, destinationId: destData.id }));
            }
        } catch (err) {
            console.error("Error loading data:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await createTravelPlan(formData);
            alert("Plan created successfully!");
            setFormData({
                ...formData,
                name: '',
                startDate: '',
                endDate: '',
                description: ''
            });
            const updatedPlans = await getTravelPlans();
            setPlans(updatedPlans);
        } catch (err) {
            alert("Error: " + (err.response?.data?.message || "Check database connection"));
        }
    };

    if (loading) return <div className="container">Loading...</div>;

    return (
        <div className="container">
            <div className="planning-header" style={{ marginBottom: '30px' }}>
                <h2>✈️ My Travel Plans</h2>
                <p>Organize your next adventures and track approval status.</p>
            </div>

            {/* CREATE PLAN FORM */}
            <section className="form-section" style={{ backgroundColor: '#fdfdfd', padding: '20px', borderRadius: '10px', border: '1px solid #eee', marginBottom: '40px' }}>
                <h3 style={{ marginTop: 0 }}>New Plan</h3>
                <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                    <div className="form-group">
                        <label>Plan Name</label>
                        <input 
                            type="text" 
                            placeholder="e.g. Summer Vacation 2026" 
                            required
                            value={formData.name}
                            onChange={e => setFormData({...formData, name: e.target.value})} 
                        />
                    </div>

                    <div className="form-group">
                        <label>Destination</label>
                        <select 
                            value={formData.destinationId}
                            onChange={e => setFormData({...formData, destinationId: e.target.value})}
                            required
                        >
                            <option value="">Select a destination...</option>
                            {destinations.map(dest => (
                                <option key={dest.id} value={dest.id}>
                                    {dest.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Start Date</label>
                        <input 
                            type="date" 
                            required
                            value={formData.startDate}
                            onChange={e => setFormData({...formData, startDate: e.target.value})} 
                        />
                    </div>

                    <div className="form-group">
                        <label>End Date</label>
                        <input 
                            type="date" 
                            required
                            value={formData.endDate}
                            onChange={e => setFormData({...formData, endDate: e.target.value})} 
                        />
                    </div>

                    <div className="form-group" style={{ gridColumn: 'span 2' }}>
                        <label>Additional Notes</label>
                        <textarea 
                            placeholder="What are you planning to visit?" 
                            value={formData.description}
                            onChange={e => setFormData({...formData, description: e.target.value})} 
                        />
                    </div>

                    <button type="submit" className="btn-primary" style={{ gridColumn: 'span 2', padding: '12px', fontSize: '1rem' }}>
                        Save Plan
                    </button>
                </form>
            </section>

            {/* PLANS LIST */}
            <section className="list-section">
                <h3>Active Plans</h3>
                {plans.length === 0 ? (
                    <p style={{ color: '#666' }}>You currently have no travel plans created.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Destination</th>
                                <th>Period</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {plans.map(plan => (
                                <tr key={plan.id}>
                                    <td style={{ fontWeight: '500' }}>{plan.name}</td>
                                    <td>{plan.destinationName || 'Not specified'}</td>
                                    <td style={{ fontSize: '0.9rem' }}>
                                        {plan.startDate} to {plan.endDate}
                                    </td>
                                    <td>
                                        <span className={`status-badge ${plan.status?.toLowerCase()}`}>
                                            {plan.status}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </section>
        </div>
    );
};

export default Planning;