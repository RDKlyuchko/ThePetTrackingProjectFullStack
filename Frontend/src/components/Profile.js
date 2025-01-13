import React, { useEffect, useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import AuthContext from '../contexts/AuthContext';
import VideoSourceForm from './VideoSourceForm';
import '../styles/ContainerStyles.css';

function Profile() {
  const navigate = useNavigate();
  const { setToken } = useContext(AuthContext);

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [feedback, setFeedback] = useState('');

  useEffect(() => {
    api
      .get('/user/profile')
      .then((response) => {
        const { username, email } = response.data;
        setUsername(username);
        setEmail(email);
      })
      .catch((error) => {
        console.error('Error fetching profile', error);
        setFeedback('Error fetching profile');
      });
  }, []);

  const handleUpdate = async (e) => {
    e.preventDefault();
    setFeedback('');

    try {
      const { data } = await api.patch('/user/profile', {
        username,
        email,
        password: password ? password : undefined,
      });

      const { accessToken, refreshToken } = data;
      setToken(accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      setFeedback('Profile updated successfully!');
      setPassword('');
    } catch (error) {
      console.error(error);
      setFeedback('Failed to update profile.');
    }
  };

  const handleDeleteAccount = async () => {
    if (!window.confirm('Are you sure you want to delete your account?')) return;
    try {
      await api.delete('/user/delete');
      setToken(null);
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      navigate('/login');
    } catch (error) {
      console.error(error);
      setFeedback('Failed to delete account.');
    }
  };

  return (
      <div className="container mt-5 pobolee">
        <h2>Your Profile</h2>

        {feedback && <p>{feedback}</p>}

        <form onSubmit={handleUpdate}>
          <div className="form-group">
            <label>Username:</label>
            <input
              className="form-control"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className="form-group mt-3">
            <label>Email:</label>
            <input
              className="form-control"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group mt-3">
            <label>New Password (leave blank if you don’t want to change it):</label>
            <input
              className="form-control"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <button className="btn btn-primary mt-3" type="submit">
            Update Profile
          </button>
        </form>

        <button className="btn btn-danger mt-4" onClick={handleDeleteAccount}>
          Delete Account
        </button>

        <div className="mt-5">
          <VideoSourceForm />
        </div>
      </div>
  );
}

export default Profile;
