// src/components/RomaniaMap.jsx
import React, { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import CallCircle from './callcircle';
import HospitalMarker from './hospitalelement';
import PoliceMarker from './policestationelement';
import FireTrucksMarker from './firestation'; // Ensure this component exists
import RescueMarker from './rescueelement'; // Ensure this component exists
import UtilityMarker from './utilityelement'; // Ensure this component exists

function RomaniaMap() {
  // Emergency calls (polled regularly)
  const [emergencyCalls, setEmergencyCalls] = useState([]);
  // Station data (fetched once)
  const [hospitals, setHospitals] = useState([]);
  const [policeStations, setPoliceStations] = useState([]);
  const [fireTrucks, setFireTrucks] = useState([]);
  const [rescueVehicles, setRescueVehicles] = useState([]);
  const [utilityVehicles, setUtilityVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fallback emergency call for testing.
  const hardcodedEmergency = {
    city: "Brasov",
    county: "Brasov",
    latitude: 45.6427,
    longitude: 25.5887,
    requests: [
      { Type: "Medical", Quantity: 3 },
      { Type: "Police", Quantity: 2 },
      { Type: "Fire", Quantity: 1 },
      { Type: "Rescue", Quantity: 1 },
      { Type: "Utility", Quantity: 1 }
    ],
    id: "hardcoded-emergency",
    timestamp: new Date().toISOString(),
    initialQuantity: 3
  };

  // Endpoints for data fetching.
  const emergencyEndpoint = 'http://172.16.10.166:8080/api/emergency';
  const hospitalEndpoint = 'http://172.16.10.166:8080/api/ambulances';
  const policeEndpoint = 'http://172.16.10.166:8080/api/police';
  const fireTrucksEndpoint = 'http://172.16.10.166:8080/api/firetrucks';
  const rescueEndpoint = 'http://172.16.10.166:8080/api/rescue';
  const utilityEndpoint = 'http://172.16.10.166:8080/api/utility';

  // Dispatch log endpoints.
  const dispatchEndpointAmbulances = 'http://172.16.10.166:8080/api/dispatch/ambulance';
  const dispatchEndpointPolice = 'http://172.16.10.166:8080/api/dispatch/police';
  const dispatchEndpointFire = 'http://172.16.10.166:8080/api/dispatch/fire';
  const dispatchEndpointRescue = 'http://172.16.10.166:8080/api/dispatch/rescue';
  const dispatchEndpointUtility = 'http://172.16.10.166:8080/api/dispatch/utility';

  // useRef for polling emergency calls.
  const emergencyIntervalRef = useRef(null);

  // confirmDispatch: Prompt for a password before dispatching.
  const confirmDispatch = () => {
    const password = window.prompt("Enter password to confirm dispatch:");
    if (password === "") {
      return true;
    } else {
      window.alert("Incorrect password. Dispatch cancelled.");
      return false;
    }
  };

  // Generic function to send a dispatch log.
  const updateDispatchLog = async (logData, endpoint) => {
    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(logData)
      });
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const result = await response.json();
      console.log('Dispatch log sent:', result);
    } catch (err) {
      console.error('Error sending dispatch log:', err);
    }
  };

  // Wrapper functions:
  const updateDispatchLogAmbulance = (logData) =>
    updateDispatchLog(logData, dispatchEndpointAmbulances);
  const updateDispatchLogPolice = (logData) =>
    updateDispatchLog(logData, dispatchEndpointPolice);
  const updateDispatchLogFire = (logData) =>
    updateDispatchLog(logData, dispatchEndpointFire);
  const updateDispatchLogRescue = (logData) =>
    updateDispatchLog(logData, dispatchEndpointRescue);
  const updateDispatchLogUtility = (logData) =>
    updateDispatchLog(logData, dispatchEndpointUtility);

  // Function to update the emergency call on the server via PATCH.
  const updateEmergencyCallOnServer = async (updatedCall) => {
    try {
      const response = await fetch(`${emergencyEndpoint}/${updatedCall.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedCall)
      });
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const result = await response.json();
      console.log('Updated emergency call on server:', result);
    } catch (err) {
      console.error('Error updating emergency call on server:', err);
    }
  };

  // Poll emergency calls every 10 seconds.
  useEffect(() => {
    const fetchEmergencyData = async () => {
      try {
        setLoading(true);
        const response = await fetch(emergencyEndpoint);
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Fetched emergency data:', data);
        if (data && data.message && data.message === "No emergency available") {
          console.log('No emergency available — stopping emergency fetch interval.');
          clearInterval(emergencyIntervalRef.current);
          emergencyIntervalRef.current = null;
          return;
        }
        const formatted = Array.isArray(data) ? data : [data];
        const processed = formatted
          .filter(call =>
            call.latitude !== 0 &&
            call.longitude !== 0 &&
            call.requests &&
            call.requests.some(req => req.Quantity > 0)
          )
          .map(call => ({
            ...call,
            id: `${call.city}-${call.county}-${Date.now()}-${Math.floor(Math.random() * 10000)}`,
            initialQuantity: call.requests[0].Quantity
          }));
        if (processed.length > 0) {
          setEmergencyCalls(prev => [...prev, ...processed]);
        }
        setError(null);
      } catch (err) {
        console.error('Error fetching emergency call:', err);
        setError('Failed to fetch emergency data. Please try again later.');
        setEmergencyCalls(prev => (prev.length === 0 ? [hardcodedEmergency] : prev));
      } finally {
        setLoading(false);
      }
    };

    fetchEmergencyData();
    emergencyIntervalRef.current = setInterval(fetchEmergencyData, 10000);
    return () => clearInterval(emergencyIntervalRef.current);
  }, [emergencyEndpoint]);

  // Fetch hospital data once.
  useEffect(() => {
    const fetchHospitalData = async () => {
      try {
        const response = await fetch(hospitalEndpoint);
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Fetched hospital data:', data);
        const formattedHospitals = Array.isArray(data) ? data : [data];
        setHospitals(formattedHospitals);
      } catch (err) {
        console.error('Error fetching hospital data:', err);
        setHospitals([]);
      }
    };
    fetchHospitalData();
  }, [hospitalEndpoint]);

  // Fetch police station data once.
  useEffect(() => {
    const fetchPoliceData = async () => {
      try {
        const response = await fetch(policeEndpoint);
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Fetched police station data:', data);
        const formattedPolice = Array.isArray(data) ? data : [data];
        setPoliceStations(formattedPolice);
      } catch (err) {
        console.error('Error fetching police station data:', err);
        setPoliceStations([]);
      }
    };
    fetchPoliceData();
  }, [policeEndpoint]);

  // Fetch fire trucks (fire station) data once.
  useEffect(() => {
    const fetchFireData = async () => {
      try {
        const response = await fetch(fireTrucksEndpoint);
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Fetched fire trucks data:', data);
        const formattedFire = Array.isArray(data) ? data : [data];
        setFireTrucks(formattedFire);
      } catch (err) {
        console.error('Error fetching fire trucks data:', err);
        setFireTrucks([]);
      }
    };
    fetchFireData();
  }, [fireTrucksEndpoint]);

  // Fetch rescue vehicles data once.
  useEffect(() => {
    const fetchRescueData = async () => {
      try {
        const response = await fetch('http://172.16.10.166:8080/api/rescue');
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Fetched rescue vehicles data:', data);
        const formattedRescue = Array.isArray(data) ? data : [data];
        setRescueVehicles(formattedRescue);
      } catch (err) {
        console.error('Error fetching rescue vehicles data:', err);
        setRescueVehicles([]);
      }
    };
    fetchRescueData();
  }, []);

  // Fetch utility vehicles data once.
  useEffect(() => {
    const fetchUtilityData = async () => {
      try {
        const response = await fetch('http://172.16.10.166:8080/api/utility');
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Fetched utility vehicles data:', data);
        const formattedUtility = Array.isArray(data) ? data : [data];
        setUtilityVehicles(formattedUtility);
      } catch (err) {
        console.error('Error fetching utility vehicles data:', err);
        setUtilityVehicles([]);
      }
    };
    fetchUtilityData();
  }, []);

  const romaniaCenter = [45.9432, 24.9668];
  const romaniaBounds = [
    [43.7, 20.1],
    [48.3, 29.7]
  ];

  // Utility: Compute distance between two lat/lng points.
  const distanceBetween = (latlng1, latlng2) =>
    L.latLng(latlng1).distanceTo(L.latLng(latlng2));

  // onAmbulanceDragEnd: Dispatch ambulances from a hospital.
  const onAmbulanceDragEnd = (hospital, e) => {
    if (!confirmDispatch()) {
      e.target.setLatLng([hospital.latitude, hospital.longitude]);
      return;
    }
    if (emergencyCalls.length === 0) {
      console.log("No active emergency calls.");
      return;
    }
    const marker = e.target;
    const dropPos = marker.getLatLng();
    const dropRadius = 200; // meters
    let closestCall = null;
    let minDistance = Infinity;
    emergencyCalls.forEach(call => {
      const medicalReq = call.requests.find(req => req.Type.toLowerCase() === "medical" && req.Quantity > 0);
      if (!medicalReq) return;
      const callPos = [call.latitude, call.longitude];
      const dist = distanceBetween(callPos, dropPos);
      if (dist <= dropRadius && dist < minDistance) {
        closestCall = call;
        minDistance = dist;
      }
    });
    if (closestCall) {
      const medicalReq = closestCall.requests.find(req => req.Type.toLowerCase() === "medical");
      const needed = medicalReq.Quantity;
      const available = hospital.quantity;
      const dispatched = Math.min(needed, available);
      if (dispatched <= 0) {
        console.log("No ambulances available to dispatch.");
        marker.setLatLng([hospital.latitude, hospital.longitude]);
        return;
      }
      const newQuantity = needed - dispatched;
      const updatedRequests = closestCall.requests.map(req =>
        req.Type.toLowerCase() === "medical" ? { ...req, Quantity: newQuantity } : req
      );
      const updatedCall = { ...closestCall, requests: updatedRequests };
      setEmergencyCalls(prev =>
        prev.map(call => call.id === closestCall.id 
          ? (updatedRequests.some(r => r.Quantity > 0) ? updatedCall : null)
          : call).filter(call => call !== null)
      );
      setHospitals(prev =>
        prev.map(h =>
          h.city === hospital.city && h.county === hospital.county
            ? { ...h, quantity: h.quantity - dispatched }
            : h
        )
      );
      console.log(`Dispatched ${dispatched} ambulances from ${hospital.city} to ${closestCall.city}, ${closestCall.county}`);
      updateEmergencyCallOnServer(updatedCall);
      const dispatchLogAmb = {
        sourceCounty: hospital.county,
        sourceCity: hospital.city,
        targetCounty: closestCall.county,
        targetCity: closestCall.city,
        quantity: dispatched
      };
      updateDispatchLogAmbulance(dispatchLogAmb);
    } else {
      console.log("No call found near drop location for ambulances.");
    }
    marker.setLatLng([hospital.latitude, hospital.longitude]);
  };

  // onPoliceDragEnd: Dispatch police vehicles from a police station.
  const onPoliceDragEnd = (police, e) => {
    if (!confirmDispatch()) {
      e.target.setLatLng([police.latitude, police.longitude]);
      return;
    }
    if (emergencyCalls.length === 0) {
      console.log("No active emergency calls.");
      return;
    }
    const marker = e.target;
    const dropPos = marker.getLatLng();
    const dropRadius = 200;
    let closestCall = null;
    let minDistance = Infinity;
    emergencyCalls.forEach(call => {
      const policeReq = call.requests.find(req => req.Type.toLowerCase() === "police" && req.Quantity > 0);
      if (!policeReq) return;
      const callPos = [call.latitude, call.longitude];
      const dist = distanceBetween(callPos, dropPos);
      if (dist <= dropRadius && dist < minDistance) {
        closestCall = call;
        minDistance = dist;
      }
    });
    if (closestCall) {
      const policeReq = closestCall.requests.find(req => req.Type.toLowerCase() === "police");
      const needed = policeReq.Quantity;
      const available = police.quantity;
      const dispatched = Math.min(needed, available);
      if (dispatched <= 0) {
        console.log("No police vehicles available to dispatch.");
        marker.setLatLng([police.latitude, police.longitude]);
        return;
      }
      const newQuantity = needed - dispatched;
      const updatedRequests = closestCall.requests.map(req =>
        req.Type.toLowerCase() === "police" ? { ...req, Quantity: newQuantity } : req
      );
      const updatedCall = { ...closestCall, requests: updatedRequests };
      setEmergencyCalls(prev =>
        prev.map(call => call.id === closestCall.id 
          ? (updatedRequests.some(r => r.Quantity > 0) ? updatedCall : null)
          : call).filter(call => call !== null)
      );
      setPoliceStations(prev =>
        prev.map(p =>
          p.city === police.city && p.county === police.county
            ? { ...p, quantity: p.quantity - dispatched }
            : p
        )
      );
      console.log(`Dispatched ${dispatched} police vehicles from ${police.city} to ${closestCall.city}, ${closestCall.county}`);
      updateEmergencyCallOnServer(updatedCall);
      const dispatchLogPol = {
        sourceCounty: police.county,
        sourceCity: police.city,
        targetCounty: closestCall.county,
        targetCity: closestCall.city,
        quantity: dispatched
      };
      updateDispatchLogPolice(dispatchLogPol);
    } else {
      console.log("No call found near drop location for police dispatch.");
    }
    marker.setLatLng([police.latitude, police.longitude]);
  };

  // onFireTrucksDragEnd: Dispatch fire trucks from a fire station.
  const onFireTrucksDragEnd = (fireStation, e) => {
    if (!confirmDispatch()) {
      e.target.setLatLng([fireStation.latitude, fireStation.longitude]);
      return;
    }
    if (emergencyCalls.length === 0) {
      console.log("No active emergency calls.");
      return;
    }
    const marker = e.target;
    const dropPos = marker.getLatLng();
    const dropRadius = 200;
    let closestCall = null;
    let minDistance = Infinity;
    emergencyCalls.forEach(call => {
      const fireReq = call.requests.find(req => req.Type.toLowerCase() === "fire" && req.Quantity > 0);
      if (!fireReq) return;
      const callPos = [call.latitude, call.longitude];
      const dist = distanceBetween(callPos, dropPos);
      if (dist <= dropRadius && dist < minDistance) {
        closestCall = call;
        minDistance = dist;
      }
    });
    if (closestCall) {
      const fireReq = closestCall.requests.find(req => req.Type.toLowerCase() === "fire");
      const needed = fireReq.Quantity;
      const available = fireStation.quantity;
      const dispatched = Math.min(needed, available);
      if (dispatched <= 0) {
        console.log("No fire trucks available to dispatch.");
        marker.setLatLng([fireStation.latitude, fireStation.longitude]);
        return;
      }
      const newQuantity = needed - dispatched;
      const updatedRequests = closestCall.requests.map(req =>
        req.Type.toLowerCase() === "fire" ? { ...req, Quantity: newQuantity } : req
      );
      const updatedCall = { ...closestCall, requests: updatedRequests };
      setEmergencyCalls(prev =>
        prev.map(call =>
          call.id === closestCall.id 
            ? (updatedRequests.some(r => r.Quantity > 0) ? updatedCall : null)
            : call
        ).filter(call => call !== null)
      );
      setFireTrucks(prev =>
        prev.map(f =>
          f.city === fireStation.city && f.county === fireStation.county
            ? { ...f, quantity: f.quantity - dispatched }
            : f
        )
      );
      console.log(`Dispatched ${dispatched} fire trucks from ${fireStation.city} to ${closestCall.city}, ${closestCall.county}`);
      updateEmergencyCallOnServer(updatedCall);
      const dispatchLogFire = {
        sourceCounty: fireStation.county,
        sourceCity: fireStation.city,
        targetCounty: closestCall.county,
        targetCity: closestCall.city,
        quantity: dispatched
      };
      updateDispatchLogFire(dispatchLogFire);
    } else {
      console.log("No call found near drop location for fire dispatch.");
    }
    marker.setLatLng([fireStation.latitude, fireStation.longitude]);
  };

  // onRescueDragEnd: Dispatch rescue vehicles from a rescue station.
  const onRescueDragEnd = (rescueStation, e) => {
    if (!confirmDispatch()) {
      e.target.setLatLng([rescueStation.latitude, rescueStation.longitude]);
      return;
    }
    if (emergencyCalls.length === 0) {
      console.log("No active emergency calls.");
      return;
    }
    const marker = e.target;
    const dropPos = marker.getLatLng();
    const dropRadius = 200;
    let closestCall = null;
    let minDistance = Infinity;
    emergencyCalls.forEach(call => {
      const rescueReq = call.requests.find(req => req.Type.toLowerCase() === "rescue" && req.Quantity > 0);
      if (!rescueReq) return;
      const callPos = [call.latitude, call.longitude];
      const dist = distanceBetween(callPos, dropPos);
      if (dist <= dropRadius && dist < minDistance) {
        closestCall = call;
        minDistance = dist;
      }
    });
    if (closestCall) {
      const rescueReq = closestCall.requests.find(req => req.Type.toLowerCase() === "rescue");
      const needed = rescueReq.Quantity;
      const available = rescueStation.quantity;
      const dispatched = Math.min(needed, available);
      if (dispatched <= 0) {
        console.log("No rescue vehicles available to dispatch.");
        marker.setLatLng([rescueStation.latitude, rescueStation.longitude]);
        return;
      }
      const newQuantity = needed - dispatched;
      const updatedRequests = closestCall.requests.map(req =>
        req.Type.toLowerCase() === "rescue" ? { ...req, Quantity: newQuantity } : req
      );
      const updatedCall = { ...closestCall, requests: updatedRequests };
      setEmergencyCalls(prev =>
        prev.map(call =>
          call.id === closestCall.id
            ? (updatedRequests.some(r => r.Quantity > 0) ? updatedCall : null)
            : call
        ).filter(call => call !== null)
      );
      setRescueVehicles(prev =>
        prev.map(r =>
          r.city === rescueStation.city && r.county === rescueStation.county
            ? { ...r, quantity: r.quantity - dispatched }
            : r
        )
      );
      console.log(`Dispatched ${dispatched} rescue vehicles from ${rescueStation.city} to ${closestCall.city}, ${closestCall.county}`);
      updateEmergencyCallOnServer(updatedCall);
      const dispatchLogRescue = {
        sourceCounty: rescueStation.county,
        sourceCity: rescueStation.city,
        targetCounty: closestCall.county,
        targetCity: closestCall.city,
        quantity: dispatched
      };
      updateDispatchLog(dispatchLogRescue, 'http://172.16.10.166:8080/api/dispatch/rescue');
    } else {
      console.log("No call found near drop location for rescue dispatch.");
    }
    marker.setLatLng([rescueStation.latitude, rescueStation.longitude]);
  };

  // onUtilityDragEnd: Dispatch utility vehicles from a utility station.
  const onUtilityDragEnd = (utilityStation, e) => {
    if (!confirmDispatch()) {
      e.target.setLatLng([utilityStation.latitude, utilityStation.longitude]);
      return;
    }
    if (emergencyCalls.length === 0) {
      console.log("No active emergency calls.");
      return;
    }
    const marker = e.target;
    const dropPos = marker.getLatLng();
    const dropRadius = 200;
    let closestCall = null;
    let minDistance = Infinity;
    emergencyCalls.forEach(call => {
      const utilityReq = call.requests.find(req => req.Type.toLowerCase() === "utility" && req.Quantity > 0);
      if (!utilityReq) return;
      const callPos = [call.latitude, call.longitude];
      const dist = distanceBetween(callPos, dropPos);
      if (dist <= dropRadius && dist < minDistance) {
        closestCall = call;
        minDistance = dist;
      }
    });
    if (closestCall) {
      const utilityReq = closestCall.requests.find(req => req.Type.toLowerCase() === "utility");
      const needed = utilityReq.Quantity;
      const available = utilityStation.quantity;
      const dispatched = Math.min(needed, available);
      if (dispatched <= 0) {
        console.log("No utility vehicles available to dispatch.");
        marker.setLatLng([utilityStation.latitude, utilityStation.longitude]);
        return;
      }
      const newQuantity = needed - dispatched;
      const updatedRequests = closestCall.requests.map(req =>
        req.Type.toLowerCase() === "utility" ? { ...req, Quantity: newQuantity } : req
      );
      const updatedCall = { ...closestCall, requests: updatedRequests };
      setEmergencyCalls(prev =>
        prev.map(call =>
          call.id === closestCall.id
            ? (updatedRequests.some(r => r.Quantity > 0) ? updatedCall : null)
            : call
        ).filter(call => call !== null)
      );
      setUtilityVehicles(prev =>
        prev.map(u =>
          u.city === utilityStation.city && u.county === utilityStation.county
            ? { ...u, quantity: u.quantity - dispatched }
            : u
        )
      );
      console.log(`Dispatched ${dispatched} utility vehicles from ${utilityStation.city} to ${closestCall.city}, ${closestCall.county}`);
      updateEmergencyCallOnServer(updatedCall);
      const dispatchLogUtility = {
        sourceCounty: utilityStation.county,
        sourceCity: utilityStation.city,
        targetCounty: closestCall.county,
        targetCity: closestCall.city,
        quantity: dispatched
      };
      updateDispatchLog(dispatchLogUtility, 'http://172.16.10.166:8080/api/dispatch/utility');
    } else {
      console.log("No call found near drop location for utility dispatch.");
    }
    marker.setLatLng([utilityStation.latitude, utilityStation.longitude]);
  };

  const activeEmergencyCalls = emergencyCalls.filter(
    call => call.requests && call.requests.some(req => req.Quantity > 0)
  );

  return (
    <div className="romania-map-container">
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
          attribution="&copy; OpenStreetMap contributors"
        />
        {emergencyCalls.map(call => (
          <CallCircle key={call.id} call={call} />
        ))}
        {hospitals.map(hospital => (
          <HospitalMarker
            key={`hospital-${hospital.city}-${hospital.county}`}
            hospital={hospital}
            onAmbulanceDragEnd={onAmbulanceDragEnd}
          />
        ))}
        {policeStations.map(police => (
          <PoliceMarker
            key={`police-${police.city}-${police.county}`}
            police={police}
            onPoliceDragEnd={onPoliceDragEnd}
          />
        ))}
        {fireTrucks.map(fireStation => (
          <FireTrucksMarker
            key={`fire-${fireStation.city}-${fireStation.county}`}
            fireTrucks={fireStation}
            onFireTrucksDragEnd={onFireTrucksDragEnd}
          />
        ))}
        {rescueVehicles.map(rescueStation => (
          <RescueMarker
            key={`rescue-${rescueStation.city}-${rescueStation.county}`}
            rescue={rescueStation}
            onRescueDragEnd={onRescueDragEnd}
          />
        ))}
        {utilityVehicles.map(utilityStation => (
          <UtilityMarker
            key={`utility-${utilityStation.city}-${utilityStation.county}`}
            utility={utilityStation}
            onUtilityDragEnd={onUtilityDragEnd}
          />
        ))}
      </MapContainer>
    </div>
  );
}

export default RomaniaMap;
