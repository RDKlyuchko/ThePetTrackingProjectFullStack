import React, { useEffect, useState, useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import api from '../services/api';
import AuthContext from '../contexts/AuthContext';

function RequireAuth({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(null);
  const { setToken } = useContext(AuthContext);

  useEffect(() => {
    const verifyToken = async () => {
      let token = localStorage.getItem('token');
      if (!token) {
        setIsAuthenticated(false);
        return;
      }

      try {
        const decodedToken = jwtDecode(token);
        const currentTime = Date.now() / 1000;
        if (decodedToken.exp < currentTime) {
          const refreshToken = localStorage.getItem('refreshToken');
          if (refreshToken) {
            try {
              const response = await api.post('/refresh-token', {
                refreshToken: refreshToken,
              });
              token = response.data.accessToken;
              setToken(token);
              setIsAuthenticated(true);
            } catch (error) {
              setToken(null);
              localStorage.removeItem('refreshToken');
              setIsAuthenticated(false);
            }
          } else {
            setIsAuthenticated(false);
          }
        } else {
          setIsAuthenticated(true);
        }
      } catch (error) {
        setToken(null);
        localStorage.removeItem('refreshToken');
        setIsAuthenticated(false);
      }
    };

    verifyToken();
  }, [setToken]);

  if (isAuthenticated === null) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  return children;
}

export default RequireAuth;