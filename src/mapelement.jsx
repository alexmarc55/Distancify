import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import CallCircle from './callcircle';
import HospitalMarker from './hospitalelement';

function RomaniaMap() {
  // Change to an array to store multiple emergency calls
  const [emergencyCalls, setEmergencyCalls] = useState([]);
  const [hospitals, setHospitals] = useState([
    {
      county: "Bucharest",
      city: "Bucharest",
      latitude: 44.4328,
      longitude: 26.1043,
      quantity: 5,
    },
    {
      county: "Cluj",
      city: "Cluj-Napoca",
      latitude: 46.7667,
      longitude: 23.5833,
      quantity: 3,
    },
    {
      county: "Iasi",
      city: "Iasi",
      latitude: 47.1585,
      longitude: 27.6014,
      quantity: 4,
    }
  ]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Hard-coded emergency call for testing
  const hardcodedEmergency = {
    city: "Brasov",
    county: "Brasov",
    latitude: 45.6427,
    longitude: 25.5887,
    requests: [
      {
        Type: "Medical",
        Quantity: 3
      }
    ],
    id: "hardcoded-emergency",
    timestamp: new Date().toISOString()
  };
  
  const emergencyEndpoint = 'http://172.16.10.166:8080/api/emergency';
  
  // Fetch emergency call every 10 seconds
  useEffect(() => {
    // Add the hardcoded emergency for testing if there are no calls yet
    if (emergencyCalls.length === 0) {
      setEmergencyCalls([hardcodedEmergency]);
    }
    
    const fetchEmergencyData = async () => {
      try {
        setLoading(true);
        const response = await fetch(emergencyEndpoint);
        
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Add a unique ID and timestamp for the call
        if (data && data.city && data.county) {
          const newCall = {
            ...data,
            id: `${data.city}-${data.county}-${Date.now()}`, // Adding timestamp to ensure uniqueness
            timestamp: new Date().toISOString(),
            initialQuantity: data.requests?.[0]?.Quantity || 0
          };
          
          // Add to the emergency calls array instead of replacing
          setEmergencyCalls(prevCalls => [...prevCalls, newCall]);
        }
        
        setError(null);
      } catch (error) {
        console.error('Error fetching emergency call:', error);
        setError('Failed to fetch emergency data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };
    
    const interval = setInterval(fetchEmergencyData, 10000);
    
    return () => clearInterval(interval);
  }, []);
  
  const romaniaCenter = [45.9432, 24.9668];
  const romaniaBounds = [
    [43.7, 20.1],
    [48.3, 29.7],
  ];
  
  const distanceBetween = (latlng1, latlng2) =>
    L.latLng(latlng1).distanceTo(L.latLng(latlng2));
  
  const onAmbulanceDragEnd = (hospital, e) => {
    // Check if there are any active emergency calls
    if (emergencyCalls.length === 0) {
      console.log("No active emergency calls.");
      return;
    }
    
    const marker = e.target;
    const dropPos = marker.getLatLng();
    const dropRadius = 200;
    
    // Find the closest emergency call to the dropped position
    let closestCall = null;
    let minDistance = Infinity;
    
    emergencyCalls.forEach(call => {
      // Skip calls that don't need ambulances anymore
      if (call.requests?.[0]?.Quantity <= 0) return;
      
      const callPosition = [call.latitude, call.longitude];
      const distance = distanceBetween(callPosition, dropPos);
      
      if (distance <= dropRadius && distance < minDistance) {
        closestCall = call;
        minDistance = distance;
      }
    });
    
    if (closestCall) {
      // Get the quantity needed from the emergency call
      const needed = closestCall.requests?.[0]?.Quantity || 0;
      
      if (needed <= 0) {
        console.log("No ambulances needed at this location.");
        marker.setLatLng([hospital.latitude, hospital.longitude]);
        return;
      }
      
      const available = hospital.quantity;
      const dispatched = Math.min(needed, available);
      
      if (dispatched <= 0) {
        console.log("No ambulances available to dispatch.");
        marker.setLatLng([hospital.latitude, hospital.longitude]);
        return;
      }
      
      // Update hospital ambulance quantity
      setHospitals(prev =>
        prev.map(h =>
          h.county === hospital.county && h.city === hospital.city
            ? { ...h, quantity: h.quantity - dispatched }
            : h
        )
      );
      
      // Update the emergency call's required quantity
      setEmergencyCalls(prevCalls =>
        prevCalls.map(call =>
          call.id === closestCall.id
            ? {
                ...call,
                requests: [
                  {
                    ...call.requests[0],
                    Quantity: call.requests[0].Quantity - dispatched
                  }
                ]
              }
            : call
        )
      );
      
      console.log(`Dispatched ${dispatched} ambulances to ${closestCall.city}, ${closestCall.county}`);
    }
    
    // Reset marker position
    marker.setLatLng([hospital.latitude, hospital.longitude]);
  };
  
  // Filter active calls (those still needing ambulances)
  const activeEmergencyCalls = emergencyCalls.filter(
    call => (call.requests?.[0]?.Quantity || 0) > 0
  );
  
  return (
    <div className="romania-map-container">
      {loading && emergencyCalls.length === 0 && (
        <div className="loading-overlay">Loading emergency data...</div>
      )}
      
      {error && (
        <div className="error-notification">
          {error}
        </div>
      )}
      
      <MapContainer
        center={romaniaCenter}
        zoom={7}
        minZoom={7}
        maxZoom={14}
        style={{ height: '100vh', width: '100%' }}
        maxBounds={romaniaBounds}
        maxBoundsViscosity={1.0}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
        />
        
        {/* Render all emergency calls */}
        {emergencyCalls.map(call => (
          <CallCircle key={call.id} call={call} />
        ))}
        
        {hospitals.map((hospital) => (
          <HospitalMarker
            key={`hospital-${hospital.city}-${hospital.county}`}
            hospital={hospital}
            onAmbulanceDragEnd={onAmbulanceDragEnd}
          />
        ))}
      </MapContainer>
      
      {/* Status panel with active and completed calls */}
      <div className="status-panel">
        <h3>Emergency Status</h3>
        {activeEmergencyCalls.length > 0 ? (
          <>
            <h4>Active Calls: {activeEmergencyCalls.length}</h4>
            <ul>
              {activeEmergencyCalls.map(call => (
                <li key={`status-${call.id}`}>
                  {call.city}, {call.county}: {call.requests?.[0]?.Quantity || 0} ambulances needed
                </li>
              ))}
            </ul>
          </>
        ) : (
          <p>No active emergency calls</p>
        )}
        
        {/* Show completed calls (where Quantity is 0) */}
        {emergencyCalls.filter(call => (call.requests?.[0]?.Quantity || 0) === 0).length > 0 && (
          <>
            <h4>Completed Calls: {emergencyCalls.filter(call => (call.requests?.[0]?.Quantity || 0) === 0).length}</h4>
            <ul>
              {emergencyCalls
                .filter(call => (call.requests?.[0]?.Quantity || 0) === 0)
                .map(call => (
                  <li key={`completed-${call.id}`}>
                    {call.city}, {call.county}: Completed
                  </li>
                ))}
            </ul>
          </>
        )}
        
        <h3>Hospitals</h3>
        <ul>
          {hospitals.map(hospital => (
            <li key={`status-${hospital.city}`}>
              {hospital.city}: {hospital.quantity} ambulances available
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default RomaniaMap;