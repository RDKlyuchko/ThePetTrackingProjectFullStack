import React, { useContext } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import AuthContext from '../contexts/AuthContext';
import '../styles/Navbar.css';

function Navbar() {
  const navigate = useNavigate();
  const { authState, setToken } = useContext(AuthContext);
  const { isAuthenticated, username, roles } = authState;

  const logout = () => {
    setToken(null);
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg lox" style={{ backgroundColor: '#f2f2f2' }}>
      <div className="container-fluid closer">
        <NavLink
          to="/"
          className="navbar-brand d-flex align-items-center"
          style={{ fontWeight: 'bold' }}
        >
          
          <img 
            src="/logo2.png"
            alt="Logo" 
            height="35"
            className="me-2" 
          />
          PetTracking
        </NavLink>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav ms-auto">
            <li className="nav-item">
              <NavLink
                to="/"
                className={({ isActive }) =>
                  'nav-link' + (isActive ? ' active' : '')
                }
              >
                Home
              </NavLink>
            </li>

            {isAuthenticated && roles.includes('ROLE_USER') && (
              <li className="nav-item">
                <NavLink
                  to="/profile"
                  className={({ isActive }) =>
                    'nav-link' + (isActive ? ' active' : '')
                  }
                >
                  Profile
                </NavLink>
              </li>
            )}

            {!isAuthenticated && (
              <li className="nav-item">
                <button
                  onClick={() => navigate('/login')}
                  className="btn btn-logout m-2"
                >
                  Login
                </button>
              </li>
            )}

            {isAuthenticated && (
              <li className="nav-item">
                <button onClick={logout} className="btn btn-logout m-2">
                  Logout
                </button>
              </li>
            )}

          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
