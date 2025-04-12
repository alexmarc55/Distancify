// src/components/utilityMarker.jsx
import React from 'react';
import { Marker, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Create a custom icon for utilitys.
const utilityIcon = new L.Icon({
  iconUrl: '../images/towing.png', // Ensure this path is correct for your utility image
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, 0],
});

const UtilityMarker = ({ utility, onUtilityDragEnd }) => {
  return (
    <Marker
      position={[utility.latitude, utility.longitude - 0.2]}
      icon={utilityIcon}
      draggable={utility.quantity > 0}
      eventHandlers={{
        dragend: (e) => onUtilityDragEnd(utility, e),
      }}
    >
      <Tooltip direction="bottom" offset={[0, 10]} permanent>
        {`${utility.city}, ${utility.county} – utility cars: ${utility.quantity}`}
      </Tooltip>
    </Marker>
  );
};

export default UtilityMarker;
