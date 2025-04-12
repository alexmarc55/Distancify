import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate(); // Hook for navigation

  const handleLogin = (e) => {
    e.preventDefault();
    // Add your login logic here (e.g., authentication, API call)
    console.log('Attempt login with:', email, password);

    // After successful login, redirect to another page (for example, "/home")
    navigate('/home');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-100 to-gray-100 px-4">
      <div className="w-full max-w-md bg-white p-8 rounded-2xl shadow-lg border border-red-200">
        <div className="flex items-center justify-center mb-6">
          <img
            src="../images/logo.png"
            alt="Ambulance Icon"
            className="h-12 w-12 mr-2"
          />
          <h2 className="text-2xl font-bold text-red-600">Emergency Login</h2>
        </div>

        <form onSubmit={handleLogin} className="flex flex-col space-y-4">
          <input
            type="email"
            placeholder="Email"
            className="px-4 py-3 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-500"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Password"
            className="px-4 py-3 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-500"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <button
            type="submit"
            className="bg-red-600 hover:bg-red-700 text-white font-semibold py-3 rounded-md transition duration-200"
          >
            Login & Call Help
          </button>
        </form>

        <p className="mt-4 text-sm text-center text-gray-500">
          Only authorized personnel may access this system.
        </p>
      </div>
    </div>
  );
}

export default Login;
