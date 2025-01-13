import React, { useState } from 'react';
import api from '../services/api';
import '../styles/ContainerStyles.css';

function CreatePet() {
  const [formData, setFormData] = useState({
    name: '',
    type: '',
    breed: '',
  });
  const { name, type, breed } = formData;

  const [mainPhoto, setMainPhoto] = useState(null);
  const [detectionPhotos, setDetectionPhotos] = useState([]);

  const [message, setMessage] = useState('');

  const onChange = (e) => 
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const onMainPhotoChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      setMainPhoto(e.target.files[0]);
    } else {
      setMainPhoto(null);
    }
  };

  const onDetectionPhotosChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      setDetectionPhotos(Array.from(e.target.files));
    } else {
      setDetectionPhotos([]);
    }
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    const formDataToSend = new FormData();
    formDataToSend.append('name', name.trim());
    formDataToSend.append('type', type.trim());
    formDataToSend.append('breed', breed.trim());
    
    if (mainPhoto) {
      formDataToSend.append('mainPhoto', mainPhoto);
    }

    detectionPhotos.forEach((photo) => {
      formDataToSend.append('detectionPhotos', photo);
    });

    try {
      const response = await api.post('/api/pets/create-with-photos', formDataToSend, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      setMessage(`Pet "${response.data.name}" created successfully!`);

      setFormData({ name: '', type: '', breed: '' });
      setMainPhoto(null);
      setDetectionPhotos([]);

      e.target.reset();
    } catch (error) {
      console.error('Error creating pet with photos:', error);
      setMessage('Failed to create pet. Ensure you are logged in and fields + files are valid.');
    }
  };

  return (
    <div className="container mt-3 pobolee2">
      <h2>Add a Pet</h2>
      {message && <p>{message}</p>}
      <form onSubmit={onSubmit}>
        <div className="mb-3">
          <input 
            type="text"
            placeholder="Name"
 
            name="name" 
            value={name} 
            onChange={onChange} 
            className="form-control" 
            required 
          />
        </div>
        <div className="mb-3">
          <input 
            type="text"
            placeholder="Type" 
            name="type" 
            value={type} 
            onChange={onChange} 
            className="form-control" 
            required 
          />
        </div>
        <div className="mb-3">
          <input 
            type="text"
            placeholder="Breed" 
            name="breed" 
            value={breed} 
            onChange={onChange} 
            className="form-control" 
            required 
          />
        </div>
        
        <div className="mb-3">
          <label>Main Photo:</label>
          <input 
            type="file"
            accept="image/*"
            onChange={onMainPhotoChange}
            className="form-control"
            required
          />
        </div>

        <div className="mb-3">
          <label>Detection Photos (up to 10):</label>
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={onDetectionPhotosChange}
            className="form-control"
          />
          <small>Selected {detectionPhotos.length} files.</small>
        </div>

        <button type="submit" className="move-right">
          Create Pet
        </button>
      </form>
    </div>
  );
}

export default CreatePet;
