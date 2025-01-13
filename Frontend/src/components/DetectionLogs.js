import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';

function DetectionLogs() {
  const { petId } = useParams();
  const [detectionData, setDetectionData] = useState([]);
  const [message, setMessage] = useState('');

  const fetchLogs = async () => {
    try {
      const response = await api.get(`/api/logs/${petId}`);
      setDetectionData(response.data);
      setMessage('');
    } catch (error) {
      console.error('Error fetching detection logs:', error);
      setMessage('Failed to fetch detection logs. Please ensure you are logged in and the backend is running.');
    }
  };

  useEffect(() => {
    if (petId) {
      fetchLogs();
    }
  }, [petId]);

  return (
    <div className="container mt-5">
      <h2>Pet Detection Logs</h2>
      {message && <p>{message}</p>}
      {!message && detectionData.length > 0 && (
        <div className="mt-3">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Previous State</th>
                <th>Current State</th>
                <th>Timestamp</th>
                <th>Pet Name</th>
                <th>Pet Type</th>
                <th>Pet Breed</th>
              </tr>
            </thead>
            <tbody>
              {detectionData.map(log => (
                <tr key={log.id}>
                  <td>{log.id}</td>
                  <td>{log.previousState}</td>
                  <td>{log.currentState}</td>
                  <td>{new Date(log.timestamp).toLocaleString()}</td>
                  <td>{log.pet.name}</td>
                  <td>{log.pet.type}</td>
                  <td>{log.pet.breed}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {(!message && detectionData.length === 0) && <p>No logs found for this pet.</p>}
      <button className="btn btn-secondary mt-3" onClick={fetchLogs}>Refresh</button>
    </div>
  );
}

export default DetectionLogs;