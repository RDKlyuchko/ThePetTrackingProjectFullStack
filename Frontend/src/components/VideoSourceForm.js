import React, { useState, useEffect } from 'react';
import api from '../services/api';

function VideoSourceForm() {
  const [videoSource, setVideoSource] = useState('');

  useEffect(() => {
    api
      .get('/user/video-source')
      .then((response) => {
        setVideoSource(response.data.videoSource || '');
      })
      .catch((error) => {
        console.error('Error fetching video source:', error);
      });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/user/video-source', { videoSource });
      alert('Video source updated successfully!');
    } catch (error) {
      console.error('Error updating video source:', error);
      alert('Failed to update video source.');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Video Source URL:
        <input
          type="text"
          value={videoSource}
          onChange={(e) => setVideoSource(e.target.value)}
        />
      </label>
      <button type="submit" className="move-right">Set Video Source</button>
    </form>
  );
}

export default VideoSourceForm;