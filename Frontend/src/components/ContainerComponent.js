import React, { useState, useContext } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import AuthContext from '../contexts/AuthContext';

import styles from '../styles/ContainerComponent.module.css';

const ContainerComponent = () => {
  const [rightPanelActive, setRightPanelActive] = useState(false);
  const { setToken } = useContext(AuthContext);
  const navigate = useNavigate();

  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginError, setLoginError] = useState('');

  const [registerUsername, setRegisterUsername] = useState('');
  const [registerEmail, setRegisterEmail] = useState('');
  const [registerPassword, setRegisterPassword] = useState('');
  const [registerError, setRegisterError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/authenticate', {
        username: loginUsername,
        password: loginPassword,
      });
      const { accessToken, refreshToken } = response.data;
      setToken(accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      const decoded = jwtDecode(accessToken);
      const roles = decoded.roles || [];

      if (roles.includes('ROLE_ADMIN')) {
        navigate('/admin-dashboard');
      } else {
        navigate('/');
      }
    } catch (error) {
      setLoginError('Login failed');
      console.error(error);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/register', {
        username: registerUsername,
        email: registerEmail,
        password: registerPassword,
      });
      navigate('/');
    } catch (error) {
      setRegisterError('Registration failed');
      console.error(error);
    }
  };

  return (
    <div className="">
    <div className={styles.wrapper}>

      <div
        className={`${styles.container} ${
          rightPanelActive ? styles['right-panel-active'] : ''
        }`}
      >
        <div className={`${styles['form-container']} ${styles['sign-in-container']}`}>
          <form className={styles.form0} onSubmit={handleLogin}>
            <h1>Sign in</h1>
            {loginError && <div className={styles['login-error']}>{loginError}</div>}
            <input
              type="text"
              placeholder="Username"
              value={loginUsername}
              onChange={(e) => setLoginUsername(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Password"
              value={loginPassword}
              onChange={(e) => setLoginPassword(e.target.value)}
              required
            />
            <button type="submit">Sign In</button>
          </form>
        </div>

        <div className={`${styles['form-container']} ${styles['sign-up-container']}`}>
          <form className={styles.form0} onSubmit={handleRegister}>
            <h1>Create Account</h1>
            {registerError && <div className={styles['login-error']}>{registerError}</div>}
            <input
              type="text"
              placeholder="Username"
              value={registerUsername}
              onChange={(e) => setRegisterUsername(e.target.value)}
              required
            />
            <input
              type="email"
              placeholder="Email"
              value={registerEmail}
              onChange={(e) => setRegisterEmail(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Password"
              value={registerPassword}
              onChange={(e) => setRegisterPassword(e.target.value)}
              required
            />
            <button type="submit">Register</button>
          </form>
        </div>

        <div className={styles['overlay-container']}>
          <div className={styles.overlay}>
            <div className={`${styles['overlay-panel']} ${styles['overlay-left']}`}>
              <h1>Welcome Back!</h1>
              <p>To continue tracking yout pets login with your personal info!</p>
              <button
                className="ghost"
                id="signIn"
                onClick={() => setRightPanelActive(false)}
              >
                Sign In
              </button>
            </div>
            <div className={`${styles['overlay-panel']} ${styles['overlay-right']}`}>
              <h1>Hello, Friend!</h1>
              <p>Enter your personal details and start tracking your pets!</p>
              <button
                className="ghost"
                id="signUp"
                onClick={() => setRightPanelActive(true)}
              >
                Sign Up
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div>
  );
};

export default ContainerComponent;
