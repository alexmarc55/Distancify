// src/components/fireTrucksMarker.jsx
import React from 'react';
import { Marker, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Create a custom icon for fireTrucks.
const fireTrucksIcon = new L.Icon({
  iconUrl: '../images/fireTrucks.png', // Ensure this path is correct for your fireTrucks image
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, 0],
});

const fireTrucksMarker = ({ fireTrucks, onFireTrucksDragEnd }) => {
  return (
    <Marker
      position={[fireTrucks.latitude, fireTrucks.longitude + 0.1]}
      icon={fireTrucksIcon}
      draggable={fireTrucks.quantity > 0}
      eventHandlers={{
        dragend: (e) => onFireTrucksDragEnd(fireTrucks, e),
      }}
    >
      <Tooltip direction="bottom" offset={[0, 40]} permanent>
        {`${fireTrucks.city}, ${fireTrucks.county} – Fire trucks cars: ${fireTrucks.quantity}`}
      </Tooltip>
    </Marker>
  );
};

export default fireTrucksMarker;
