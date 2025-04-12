// src/components/rescueMarker.jsx
import React from 'react';
import { Marker, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Create a custom icon for rescues.
const rescueIcon = new L.Icon({
  iconUrl: '../images/rescue.png', // Ensure this path is correct for your rescue image
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, 0],
});

const RescueMarker = ({ rescue, onRescueDragEnd }) => {
  return (
    <Marker
      position={[rescue.latitude, rescue.longitude + 0.2]}
      icon={rescueIcon}
      draggable={rescue.quantity > 0}
      eventHandlers={{
        dragend: (e) => onRescueDragEnd(rescue, e),
      }}
    >
      <Tooltip direction="bottom" offset={[0, 10]} permanent>
        {`${rescue.city}, ${rescue.county} – Rescue cars: ${rescue.quantity}`}
      </Tooltip>
    </Marker>
  );
};

export default RescueMarker;
