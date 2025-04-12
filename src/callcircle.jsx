// src/components/CallCircle.jsx
import React from 'react';
import { Circle, Tooltip, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

function CallCircle({ call }) {
  const timestamp = new Date(call.timestamp || Date.now()).toLocaleString();
  // Calculate total requested units; default to 0 if requests is empty.
  const totalQuantity = (call.requests || []).reduce((sum, req) => sum + (req.Quantity || 0), 0);
  const radius = Math.max(200, totalQuantity * 100);
  
  // Determine circle color based on request priority; if no active request, return green.
  const getPriorityColor = () => {
    const quantities = {
      fire: 0,
      police: 0,
      medical: 0,
    };
    (call.requests || []).forEach(req => {
      const type = req.Type ? req.Type.toLowerCase() : 'medical';
      if (quantities[type] !== undefined) {
        quantities[type] += req.Quantity || 0;
      }
    });
    if (quantities.fire > 0) return 'red';
    if (quantities.police > 0) return 'blue';
    if (quantities.medical > 0) return 'gold';
    return 'green';
  };

  // Return a default label if type is undefined.
  const getLabelByType = (type) => {
    if (!type) return 'Ambulance';
    switch (type.toLowerCase()) {
      case 'fire':
        return 'Firetruck';
      case 'police':
        return 'Police Car';
      case 'rescue':
        return 'Rescue Car';
      case 'utility':
        return 'Utility Car';
      case 'medical':
      default:
        return 'Ambulance';
    }
  };

  return (
    <Circle
      center={[call.latitude, call.longitude]}
      radius={radius}
      pathOptions={{
        color: getPriorityColor(),
        fillColor: getPriorityColor(),
        fillOpacity: totalQuantity > 0 ? 0.5 : 0.2
      }}
    >
      <Tooltip direction="top" offset={[0, -20]} opacity={1} permanent>
        <div><strong>{call.city}, {call.county}</strong></div>
        {(call.requests || []).map((req, idx) => (
          <div key={idx}>
            {getLabelByType(req.Type)}: {req.Quantity || 0}
          </div>
        ))}
      </Tooltip>
      <Popup>
        <h3>Emergency Call</h3>
        <p><strong>Location:</strong> {call.city}, {call.county}</p>
        {(call.requests || []).map((req, idx) => (
          <p key={idx}>
            <strong>{getLabelByType(req.Type)}:</strong> {req.Quantity || 0}
          </p>
        ))}
        <p><strong>Status:</strong> {totalQuantity > 0 ? 'Active' : 'Completed'}</p>
        <p><strong>Time received:</strong> {timestamp}</p>
        <p><strong>Coordinates:</strong> {call.latitude.toFixed(4)}, {call.longitude.toFixed(4)}</p>
        {call.initialQuantity && call.initialQuantity > totalQuantity && (
          <p><strong>Units dispatched:</strong> {call.initialQuantity - totalQuantity}</p>
        )}
      </Popup>
    </Circle>
  );
}

export default CallCircle;
