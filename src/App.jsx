import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './pages/login';
import Home from './pages/home'; // Your Home component
import LastCalls from './pages/LastCalls';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/home" element={<Home />} />
      <Route path="/lastcalls" element={<LastCalls />} />
    </Routes>
  );
}

export default App;
