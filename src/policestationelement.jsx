// src/components/policeMarker.jsx
import React from 'react';
import { Marker, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Create a custom icon for polices.
const policeIcon = new L.Icon({
  iconUrl: '../images/police.png', // Ensure this path is correct for your police image
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, 0],
});

const PoliceMarker = ({ police, onPoliceDragEnd }) => {
  return (
    <Marker
      position={[police.latitude, police.longitude]}
      icon={policeIcon}
      draggable={police.quantity > 0}
      eventHandlers={{
        dragend: (e) => onPoliceDragEnd(police, e),
      }}
    >
      <Tooltip direction="bottom" offset={[0, 10]} permanent>
        {`${police.city}, ${police.county} – Police cars: ${police.quantity}`}
      </Tooltip>
    </Marker>
  );
};

export default PoliceMarker;
