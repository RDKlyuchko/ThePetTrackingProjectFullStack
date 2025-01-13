import React, { memo } from 'react';

const VideoFeed = memo(({ url }) => {
  console.log("Rendering VideoFeed");
  return (
    <img
      src={url}
      alt="Live Detection Feed"
      style={{ display: 'block', width: '100%', height: 'auto' }}
    />
  );
});

export default VideoFeed;
