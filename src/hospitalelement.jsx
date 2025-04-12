// src/components/HospitalMarker.jsx
import React from 'react';
import { Marker, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Create a custom icon for hospitals.
const hospitalIcon = new L.Icon({
  iconUrl: '../images/hospital.png', // Ensure this path is correct for your hospital image
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, 0],
});

const HospitalMarker = ({ hospital, onAmbulanceDragEnd }) => {
  return (
    <Marker
      position={[hospital.latitude, hospital.longitude]}
      icon={hospitalIcon}
      draggable={hospital.quantity > 0}
      eventHandlers={{
        dragend: (e) => onAmbulanceDragEnd(hospital, e),
      }}
    >
      <Tooltip direction="bottom" offset={[0, 10]} permanent>
        {`${hospital.city}, ${hospital.county} – Ambulances: ${hospital.quantity}`}
      </Tooltip>
    </Marker>
  );
};

export default HospitalMarker;
