import React, { useState, useContext } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import AuthContext from '../contexts/AuthContext';

function Login() {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [message, setMessage] = useState('');
  const navigate = useNavigate();
  const { setToken } = useContext(AuthContext);

  const { username, password } = formData;

  const onChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/authenticate', formData);
      const accessToken = response.data.accessToken;
      const refreshToken = response.data.refreshToken;

      setToken(accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      const decodedToken = jwtDecode(accessToken);
      const roles = decodedToken.roles;
      const userId = decodedToken.userId;

      setMessage('Login successful');

      if (roles.includes('ROLE_ADMIN')) {
        navigate('/admin-dashboard');
      } else if (roles.includes('ROLE_USER')) {
        navigate('/');
      } else {
        setMessage('You do not have access');
      }
    } catch (error) {
      setMessage('Login failed');
      console.error(error);
    }
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
      <div className="card p-4 shadow-lg rounded" style={{ width: '400px' }}>
        <h2 className="text-center mb-4">Login</h2>
        {message && <p className="text-center text-danger">{message}</p>}
        <form onSubmit={onSubmit}>
          <div className="mb-3">
            <label className="form-label">Username:</label>
            <input
              type="text"
              name="username"
              value={username}
              onChange={onChange}
              className="form-control"
              required
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Password:</label>
            <input
              type="password"
              name="password"
              value={password}
              onChange={onChange}
              className="form-control"
              required
            />
          </div>
          <button
            type="submit"
            className="btn btn-primary w-100"
            style={{ background: 'linear-gradient(90deg, #007BFF, #00C6FF)', border: 'none' }}
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;
