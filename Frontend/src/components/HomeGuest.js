import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Home.css';

function HomeGuest() {
  const navigate = useNavigate();

  return (
    <div className="centered-container">
    <div className="container mt-4">
      <div className="row">
        <div className="col-md-8">
          <h4 className="mb-3">Welcome to Pet Tracker</h4>
          <div className="bordik p-2 mb-4">
            <p>
              Pet Tracker is a web-based pet monitoring system designed to keep your furry friends safe and secure. Using cutting-edge AI technology (YOLOv8), we provide real-time detection of your pets' movements via a live CCTV feed.
            </p>
            <p>
              With our easy-to-use platform, you can track your pets inside and outside your house, ensuring they are always where they should be.
            </p>
            <button
              className="btn btn-primary mt-3"
              onClick={() => navigate('/register')}
            >
              Get Started
            </button>
          </div>
        </div>

        <div className="col-md-4">
          <h4 className="mb-3">Why Choose Pet Tracker?</h4>
          <div className="bordik p-3 mb-4">
            <ul>
              <li>Real-time pet monitoring using AI-powered CCTV feed.</li>
              <li>User-friendly web application interface.</li>
              <li>Ensure your pet’s safety and prevent accidental mishaps.</li>
              <li>Historical tracking of your pet’s movement patterns.</li>
              <li>Secure login and data protection using JWT authentication.</li>
            </ul>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-12">
          <h4>How It Works</h4>
          <div className="bordik p-2 mb-4">
            <ol>
              <li>Connect your existing CCTV camera with our platform.</li>
              <li>Our AI detects and tracks your pets in real-time.</li>
              <li>View live updates and detection logs via the web app.</li>
              <li>Enjoy peace of mind knowing your pets are safe.</li>
            </ol>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-8">
          <h4>Ready to Keep Your Pets Safe?</h4>
          <p>
            Join the many pet owners who trust Pet Tracker to ensure their pets' safety and well-being.
          </p>
          <button
            className="btn btn-success mt-3"
            onClick={() => navigate('/register')}
          >
            Sign Up Now
          </button>
        </div>
        <div className="col-md-4">
          <h4>Already Have an Account?</h4>
          <button
            className="btn btn-secondary mt-3"
            onClick={() => navigate('/login')}
          >
            Log In
          </button>
        </div>
      </div>
    </div>
    </div>

  );
}

export default HomeGuest;
