import React, { useEffect, useRef } from 'react';
import Hls from 'hls.js';

function HlsPlayer({ userId }) {
  const videoRef = useRef(null);

  useEffect(() => {
    const hlsUrl = `http://localhost:5001/hls/${userId}/output.m3u8`;

    if (Hls.isSupported()) {
      const hls = new Hls();
      hls.loadSource(hlsUrl);
      hls.attachMedia(videoRef.current);
    } else if (videoRef.current.canPlayType('application/vnd.apple.mpegurl')) {
      videoRef.current.src = hlsUrl;
    }
  }, [userId]);

  return (
    <video
      ref={videoRef}
      controls
      autoPlay
      style={{ width: '100%', maxWidth: '640px' }}
    />
  );
}

export default HlsPlayer;
