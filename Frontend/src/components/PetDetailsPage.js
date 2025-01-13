import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import '../styles/ContainerStyles.css';


function PetDetailsPage() {
  const { petId } = useParams();
  const navigate = useNavigate();

  const [pet, setPet] = useState({
    id: '',
    name: '',
    type: '',
    breed: '',
    mainPhotoUrl: '',
    detectionPhotoUrls: []
  });

  const [editData, setEditData] = useState({
    name: '',
    type: '',
    breed: ''
  });

  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchPet = async () => {
      try {
        const response = await api.get(`/api/pets/${petId}`);
        setPet(response.data);
        setEditData({
          name: response.data.name,
          type: response.data.type,
          breed: response.data.breed
        });
      } catch (error) {
        console.error('Error fetching pet:', error);
        setMessage('Could not fetch the pet. Maybe it does not exist.');
      }
    };

    fetchPet();
  }, [petId]);

  const onChange = (e) => {
    setEditData({ ...editData, [e.target.name]: e.target.value });
  };

  const onUpdatePet = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      const response = await api.put(`/api/pets/${petId}`, {
        name: editData.name.trim(),
        type: editData.type.trim(),
        breed: editData.breed.trim()
      });
      setPet(response.data);
      setMessage('Pet updated successfully!');
    } catch (error) {
      console.error('Error updating pet:', error);
      setMessage('Failed to update pet. Check console for details.');
    }
  };

  const onDeletePet = async () => {
    if (!window.confirm('Are you sure you want to delete this pet?')) return;

    try {
      await api.delete(`/api/pets/${petId}`);
      alert('Pet deleted successfully.');
      navigate('/');
    } catch (error) {
      console.error('Error deleting pet:', error);
      setMessage('Failed to delete pet. Check console for details.');
    }
  };

  return (
    <div className="container mt-3 pobolee2">
      <h2>Pet Details</h2>
      {message && <p>{message}</p>}

      {pet.id ? (
        <div className="row">
          <div className="col-md-6">
            <img
              src={pet.mainPhotoUrl || '/husky.png'}
              alt={pet.name}
              style={{ width: '200px', height: '200px', objectFit: 'cover' }}
              className="mb-3"
            />

            <h5>Name: {pet.name}</h5>
            <p>Type: {pet.type}</p>
            <p>Breed: {pet.breed}</p>

            {pet.detectionPhotoUrls && pet.detectionPhotoUrls.length > 0 && (
              <div>
                <h6>Detection Photos:</h6>
                {pet.detectionPhotoUrls.map((url, idx) => (
                  <img
                    key={idx}
                    src={url}
                    alt={`Detection ${idx}`}
                    style={{ width: '80px', height: '80px', objectFit: 'cover', marginRight: '10px' }}
                  />
                ))}
              </div>
            )}

            <button onClick={onDeletePet} className="btn btn-danger mt-3">
              Delete Pet
            </button>
          </div>

          <div className="col-md-6">
            <h5>Edit Pet</h5>
            <form onSubmit={onUpdatePet}>
              <div className="mb-3">
                <label>Name:</label>
                <input
                  type="text"
                  name="name"
                  value={editData.name}
                  onChange={onChange}
                  className="form-control"
                  required
                />
              </div>
              <div className="mb-3">
                <label>Type:</label>
                <input
                  type="text"
                  name="type"
                  value={editData.type}
                  onChange={onChange}
                  className="form-control"
                  required
                />
              </div>
              <div className="mb-3">
                <label>Breed:</label>
                <input
                  type="text"
                  name="breed"
                  value={editData.breed}
                  onChange={onChange}
                  className="form-control"
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary">
                Update Pet
              </button>
            </form>
          </div>
        </div>
      ) : (
        <p>No pet data found.</p>
      )}
    </div>
  );
}

export default PetDetailsPage;
