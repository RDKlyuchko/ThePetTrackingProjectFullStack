import React, { createContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [authState, setAuthState] = useState({
    isAuthenticated: false,
    username: '',
    roles: [],
    userId: null,
  });

  const setToken = (token) => {
    if (token) {
      localStorage.setItem('token', token);
      updateAuthState();
    } else {
      localStorage.removeItem('token');
      setAuthState({
        isAuthenticated: false,
        username: '',
        roles: [],
        userId: null,
      });
    }
  };

  const updateAuthState = () => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        const currentTime = Date.now() / 1000;
        if (decodedToken.exp > currentTime) {
          setAuthState({
            isAuthenticated: true,
            username: decodedToken.sub,
            roles: decodedToken.roles || [],
            userId: decodedToken.userId || null,
          });
        } else {
          setAuthState({
            isAuthenticated: false,
            username: '',
            roles: [],
            userId: null,
          });
          localStorage.removeItem('token');
        }
      } catch (error) {
        setAuthState({
          isAuthenticated: false,
          username: '',
          roles: [],
          userId: null,
        });
        localStorage.removeItem('token');
      }
    } else {
      setAuthState({
        isAuthenticated: false,
        username: '',
        roles: [],
        userId: null,
      });
    }
  };

  useEffect(() => {
    updateAuthState();
  }, []);

  return (
    <AuthContext.Provider value={{ authState, setToken, updateAuthState }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;