import React, { useEffect, useContext, useState } from 'react';
import AuthContext from '../contexts/AuthContext';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import api from '../services/api';
import axios from 'axios';
import { useNavigate, Navigate } from 'react-router-dom';
import '../styles/Home.css';

function Home() {
  const { authState } = useContext(AuthContext);
  const token = localStorage.getItem('token');
  const navigate = useNavigate();

  const [pets, setPets] = useState([]);
  const [logs, setLogs] = useState([]);
  const [filteredLogs, setFilteredLogs] = useState([]);
  const [isFeedConfigured, setIsFeedConfigured] = useState(false);
  const [isCheckingFeed, setIsCheckingFeed] = useState(true);
  const [cameraFeedInput, setCameraFeedInput] = useState('');
  const [selectedPetName, setSelectedPetName] = useState('');
  const [selectedState, setSelectedState] = useState('');
  const [selectedDate, setSelectedDate] = useState('');

  const userId = authState.userId;

  useEffect(() => {
    if (token) {
      api.get('/api/pets')
        .then((response) => setPets(response.data))
        .catch((error) => console.error('Error fetching user pets:', error));
    }
  }, [token]);

  useEffect(() => {
    if (token) {
      api.get('/user/video-source')
        .then((response) => {
          const existingSource = response.data.videoSource || '';
          setIsFeedConfigured(!!existingSource);
        })
        .catch((error) => console.error('Error checking video source:', error))
        .finally(() => setIsCheckingFeed(false));
    }
  }, [token]);

  useEffect(() => {
    if (authState.isAuthenticated && userId) {
      const topicName = `/topic/user_${userId}_logs`;

      const stompClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        connectHeaders: { Authorization: `Bearer ${token}` },
        onConnect: (frame) => {
          console.log('Connected to WebSocket:', frame);

          stompClient.subscribe(topicName, (message) => {
            const log = JSON.parse(message.body);
            setLogs((prevLogs) => [log, ...prevLogs]);
          });
        },
        onStompError: (frame) => {
          console.error('Broker error:', frame.headers['message'], frame.body);
        },
      });

      stompClient.activate();
      return () => stompClient.deactivate();
    }
  }, [authState.isAuthenticated, userId, token]);

  const fetchLogs = async () => {
    try {
      const response = await api.get(`/api/logs/user/${userId}/all`);
      setLogs(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching detection logs:', error);
      alert('Failed to fetch detection logs. Ensure you are logged in and backend is running.');
    }
  };

  useEffect(() => {
    if (token && userId) {
      fetchLogs();
    }
  }, [token, userId]);

  useEffect(() => {
    let filtered = logs;

    if (selectedPetName) {
      filtered = filtered.filter((log) => log.petName === selectedPetName);
    }

    if (selectedState) {
      filtered = filtered.filter((log) => log.currentState === selectedState);
    }

    if (selectedDate) {
      const selectedDayStart = new Date(selectedDate).setHours(0, 0, 0, 0);
      const selectedDayEnd = new Date(selectedDate).setHours(23, 59, 59, 999);

      filtered = filtered.filter((log) => {
        const logTimestamp = new Date(log.timestamp).getTime();
        return logTimestamp >= selectedDayStart && logTimestamp <= selectedDayEnd;
      });
    }

    setFilteredLogs(filtered);
  }, [logs, selectedPetName, selectedState, selectedDate]);

  const handleCameraFeedSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/user/video-source', { videoSource: cameraFeedInput });

      await axios.post('http://localhost:5001/setup_camera', {
        token,
        userId: String(userId),
        cameraUrl: cameraFeedInput,
      });

      setIsFeedConfigured(true);
      alert('Camera feed source saved and Python service notified!');
    } catch (error) {
      console.error('Error setting up camera feed source:', error);
      alert('Failed to set up camera feed source.');
    }
  };

  const handlePetClick = (pet) => {
    navigate(`/pets/${pet.id}`);
  };

  if (!token) {
    return <Navigate to="/home-guest" />;
  }

  if (isCheckingFeed) {
    return (
      <div className="container mt-5">
        <h4>Checking camera feed configuration...</h4>
      </div>
    );
  }

  const encodedToken = encodeURIComponent(token || '');
  const pythonServiceUrl = `http://localhost:5001/video_feed?token=${encodedToken}&userId=${userId}`;

  return (
<div className="container mt-4">
      <div className="row">
        {/* Camera section */}
        <div className="col-md-8">
          <h4 className="mb-3">Garden Camera</h4>
          <div className="bordik p-2 mb-4">
            {isFeedConfigured ? (
              <img
                src={pythonServiceUrl}
                alt="Live Detection Feed"
                className="img-fluid"
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.src = "/test.png";
                }}
              />
            ) : (
              <form onSubmit={handleCameraFeedSubmit} className="setup-form">
                <p>Please enter your camera source URL:</p>
                <label>
                  <input
                    type="text"
                    value={cameraFeedInput}
                    onChange={(e) => setCameraFeedInput(e.target.value)}
                    className="form-control"
                  />
                </label>
                <button type="submit" className="move-right">
                  Set Camera Feed
                </button>
              </form>
            )}
          </div>
        </div>

        {/* Pets Section */}
        <div className="col-md-4">
          <h4 className="mb-3">My Pets</h4>
          <div className="bordik p-3 mb-4">
            {pets.length > 0 ? (
              pets.map((pet) => (
                <div
                  key={pet.id}
                  className="d-flex justify-content-between align-items-center mb-3 p-2 border rounded"
                  onClick={() => handlePetClick(pet)}
                  style={{ cursor: 'pointer', backgroundColor: '#f9f9f9' }}
                >
                  <span
                    style={{ fontSize: '1.1rem', fontWeight: 'bold', color: '#2A2A2A' }}
                  >
                    {pet.name}
                  </span>
                  <img
                    src={pet.mainPhotoUrl || '/husky.png'}
                    alt={pet.name}
                    className="rounded-circle"
                    style={{ width: '50px', height: '50px', objectFit: 'cover' }}
                  />
                </div>
              ))
            ) : (
              <p>You have no pets yet.</p>
            )}
            <button
              className="btn btn-addpet mt-2"
              onClick={() => navigate('/create-pet')}
            >
              Add New +
            </button>
          </div>
        </div>
      </div>

      {/* Logs Section */}
  <div className="row">
    <h4>Logs</h4>
    <div className="bordik p-2 mb-4">
      {/* Filter Section */}
      <div className="row mb-3">
        <div className="col-md-4">
          <label>Filter by Pet Name:</label>
          <select
            className="form-control"
            value={selectedPetName}
            onChange={(e) => setSelectedPetName(e.target.value)}
          >
            <option value="">All Pets</option>
            {pets.map((pet) => (
              <option key={pet.id} value={pet.name}>
                {pet.name}
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-4">
          <label>Filter by Date:</label>
          <input
            type="date"
            className="form-control"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
          />
        </div>
      </div>

      {/* Logs Table */}
      <div className="col-12">
        {filteredLogs.length > 0 ? (
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Current State</th>
                <th>Timestamp</th>
                <th>Pet Name</th>
              </tr>
            </thead>
            <tbody>
              {filteredLogs.map((log) => (
                <tr key={log.id}>
                  <td>{log.id}</td>
                  <td>{log.currentState}</td>
                  <td>{new Date(log.timestamp).toLocaleString()}</td>
                  <td>{log.petName}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>No logs found.</p>
        )}
      </div>
    </div>
  </div>
  </div>
  );
}

export default Home;
