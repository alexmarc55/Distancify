import React from 'react';
import { Circle, Tooltip, Popup } from 'react-leaflet';

function CallCircle({ call }) {
  const quantity = call.requests?.[0]?.Quantity || 0;
  const type = call.requests?.[0]?.Type?.toLowerCase() || 'medical';
  const timestamp = new Date(call.timestamp).toLocaleString();

  const radius = Math.max(200, quantity * 100);

  // Color based on type and status
  const getCircleColor = () => {
    if (quantity <= 0) return 'green'; // Completed
    if (type === 'fire') return 'red';
    if (type === 'police') return 'blue';
    return 'gold'; // Medical default
  };

  const getServiceLabel = () => {
    switch (type) {
      case 'fire':
        return 'Firetruck';
      case 'police':
        return 'Police Car';
      case 'medical':
      default:
        return 'Ambulance';
    }
  };

  const opacity = quantity <= 0 ? 0.2 : 0.4;

  return (
    <Circle
      center={[call.latitude, call.longitude]}
      radius={radius}
      pathOptions={{
        color: getCircleColor(),
        fillColor: getCircleColor(),
        fillOpacity: opacity
      }}
    >
      <Tooltip direction="top" offset={[0, -20]} opacity={1} permanent>
        <div>{call.city}, {call.county}</div>
        <div>{getServiceLabel()}: {quantity > 0 ? quantity : 'Completed'}</div>
      </Tooltip>
      <Popup>
        <h3>Emergency Call</h3>
        <p><strong>Location:</strong> {call.city}, {call.county}</p>
        <p><strong>Service:</strong> {getServiceLabel()}</p>
        <p><strong>Required units:</strong> {quantity}</p>
        <p><strong>Status:</strong> {quantity > 0 ? 'Active' : 'Completed'}</p>
        <p><strong>Time received:</strong> {timestamp}</p>
        <p><strong>Coordinates:</strong> {call.latitude.toFixed(4)}, {call.longitude.toFixed(4)}</p>
        {call.initialQuantity && call.initialQuantity > quantity && (
          <p><strong>Units dispatched:</strong> {call.initialQuantity - quantity}</p>
        )}
      </Popup>
    </Circle>
  );
}

export default CallCircle;
